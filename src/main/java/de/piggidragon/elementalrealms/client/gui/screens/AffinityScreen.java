package de.piggidragon.elementalrealms.client.gui.screens;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.guis.menus.custom.AffinityBookMenu;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for displaying player affinities
 * Shows all affinities with completion bars
 */
public class AffinityScreen extends AbstractContainerScreen<AffinityBookMenu> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ElementalRealms.MODID,
            "textures/gui/affinity_screen.png"
    );

    /**
     * Creates a new affinity screen
     *
     * @param menu            The affinity menu containing the data
     * @param playerInventory Player's inventory (required by AbstractContainerScreen)
     * @param title           The screen title
     */
    public AffinityScreen(AffinityBookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // Set GUI dimensions (background texture size)
        this.imageWidth = 176;
        this.imageHeight = 190; // Increased height for affinity list

        // DON'T calculate titleLabelX here - this.font is not initialized yet!
        // We'll do it in init() instead

        // Hide player inventory label (we don't show player inventory)
        this.inventoryLabelY = 10000; // Move offscreen
    }

    @Override
    protected void init() {
        super.init();

        // NOW we can safely use this.font
        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        // leftPos and topPos are automatically calculated here
        // They represent the top-left corner of the GUI
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render the background first (dark overlay)
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render tooltips/hover effects (if any)
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // This method renders the background of the GUI
        // It's called automatically by render()

        // Render panel background
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xC0101010  // Semi-transparent dark gray (ARGB format)
        );

        // Render border around panel
        graphics.renderOutline(
                this.leftPos,
                this.topPos,
                this.imageWidth,
                this.imageHeight,
                0xFF8B8B8B  // Light gray
        );

        // Optional: Render custom texture background instead
        // graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // This method renders text and other elements on top of the background
        // Coordinates here are RELATIVE to leftPos/topPos (no need to add them)

        // Don't call super.renderLabels() because it would render the default labels
        // super.renderLabels(graphics, mouseX, mouseY);

        // Render title (already positioned by titleLabelX/Y)
        graphics.drawString(
                this.font,
                this.title,
                this.titleLabelX,
                this.titleLabelY,
                0xFFFFFF, // White
                false     // No shadow
        );

        // Render affinities list
        this.renderAffinities(graphics);
    }

    /**
     * Renders all affinities with completion bars
     * Coordinates are relative to leftPos/topPos
     *
     * @param graphics The graphics context
     */
    private void renderAffinities(GuiGraphics graphics) {
        int startY = 25; // Relative Y position
        int xOffset = 10; // Relative X offset

        // Get completed and incomplete affinities from menu
        var completed = this.menu.getCompletedAffinities();
        var incomplete = this.menu.getIncompleteAffinities();

        // Render "Completed" section
        if (!completed.isEmpty()) {
            graphics.drawString(
                    this.font,
                    Component.translatable("gui.elementalrealms.affinity.completed"),
                    xOffset,
                    startY,
                    0x55FF55,  // Green
                    false
            );
            startY += 12;

            // Render each completed affinity
            for (AffinityBookMenu.AffinityData data : completed) {
                renderAffinityEntry(graphics, data, xOffset, startY);
                startY += 18;  // Move down for next entry
            }

            startY += 5;  // Extra spacing
        }

        // Render "Incomplete" section
        if (!incomplete.isEmpty()) {
            graphics.drawString(
                    this.font,
                    Component.translatable("gui.elementalrealms.affinity.incomplete"),
                    xOffset,
                    startY,
                    0xFFAA00,  // Orange
                    false
            );
            startY += 12;

            // Render each incomplete affinity
            for (AffinityBookMenu.AffinityData data : incomplete) {
                renderAffinityEntry(graphics, data, xOffset, startY);
                startY += 18;
            }
        }
    }

    /**
     * Renders a single affinity entry with progress bar
     * Coordinates are relative to leftPos/topPos
     *
     * @param graphics The graphics context
     * @param data     The affinity data to render
     * @param x        Relative X position
     * @param y        Relative Y position
     */
    private void renderAffinityEntry(
            GuiGraphics graphics,
            AffinityBookMenu.AffinityData data,
            int barX,
            int y
    ) {
        // Get affinity-specific color for the name
        int affinityColor = getAffinityColor(data.affinity());
        boolean isCompleted = data.isCompleted();
        int completion = data.completionPercent();

        // Get color based on completion percentage (red → orange → green)
        int progressColor = getProgressColor(completion);

        // 1. Render affinity name with element color
        Component name = Component.translatable(
                "affinity.elementalrealms." + data.affinity().name().toLowerCase()
        );
        graphics.drawString(
                this.font,
                name,
                barX,
                y,
                affinityColor,
                isCompleted
        );

        // 2. Render completion percentage text with dynamic color
        String percentText = completion + "%";
        graphics.drawString(this.font, percentText, barX + 120, y, progressColor, false);

        // 3. Render progress bar ONLY if not completed
        if (!isCompleted) {
            int barY = y + 10;
            int barWidth = 150;
            int barHeight = 4;

            // Background bar (dark)
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF3C3C3C);

            // Progress fill with color gradient
            int progressWidth = (int) (barWidth * (completion / 100.0f));
            graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, progressColor);

            // Border
            graphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF8B8B8B);
        }
    }

    /**
     * Calculates a smooth color gradient from red to green based on percentage
     * 0% = Red (0xFFFF0000)
     * 50% = Orange (0xFFFF8800)
     * 100% = Green (0xFF00FF00)
     *
     * @param percent Completion percentage (0-100)
     * @return ARGB color integer
     */
    private int getProgressColor(int percent) {
        // Clamp to 0-100 range
        percent = Math.clamp(percent, 0, 100);

        if (percent < 50) {
            // Transition from RED to ORANGE (0% to 50%)
            // Red: 0xFF FF 00 00
            // Orange: 0xFF FF 88 00
            float ratio = percent / 50.0f; // 0.0 to 1.0

            int red = 255;
            int green = (int) (0x88 * ratio); // 0 → 136
            int blue = 0;

            return 0xFF000000 | (red << 16) | (green << 8) | blue;

        } else {
            // Transition from ORANGE to GREEN (50% to 100%)
            // Orange: 0xFF FF 88 00
            // Green: 0xFF 00 FF 00
            float ratio = (percent - 50) / 50.0f; // 0.0 to 1.0

            int red = (int) (255 * (1 - ratio)); // 255 → 0
            int green = (int) (0x88 + (255 - 0x88) * ratio); // 136 → 255
            int blue = 0;

            return 0xFF000000 | (red << 16) | (green << 8) | blue;
        }
    }

    /**
     * Gets the color for a specific affinity type
     * Used for the affinity name display
     */
    private int getAffinityColor(Affinity affinity) {
        return switch (affinity) {
            case VOID -> 0xFF000000;       // Black (void space)
            case FIRE -> 0xFFFF4500;      // Orange-Red (fire flames)
            case WATER -> 0xFF1E90FF;     // Dodger Blue (ocean water)
            case EARTH -> 0xFF8B4513;     // Saddle Brown (soil/earth)
            case WIND -> 0xFFE0FFFF;      // Light Cyan (sky/air)
            case LIGHTNING -> 0xFFFFFF00; // Yellow (lightning bolt)
            case ICE -> 0xFF87CEEB;       // Sky Blue (ice/frost)
            case SOUND -> 0xFFDA70D6;     // Orchid (sound waves)
            case GRAVITY -> 0xFF4B0082;   // Indigo (deep space/gravity)
            case TIME -> 0xFFFFD700;      // Gold (clockwork/time)
            case SPACE -> 0xFF191970;     // Midnight Blue (outer space)
            case LIFE -> 0xFF32CD32;      // Lime Green (nature/plants)
        };
    }

    @Override
    public boolean isPauseScreen() {
        // Don't pause game when GUI is open
        return false;
    }
}
