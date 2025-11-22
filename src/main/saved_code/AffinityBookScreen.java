package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.events.AffinityBookButtonHandler;
import de.piggidragon.elementalrealms.registries.guis.menus.custom.AffinityBookMenu;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for displaying player affinities with completion percentages.
 * Opens when the affinity book button is clicked.
 */
public class AffinityBookScreen extends AbstractContainerScreen<AffinityBookMenu> {

    /**
     * Background texture for the affinity book.
     */
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "textures/gui/affinity_book.png");

    /**
     * Create a new affinity book screen.
     *
     * @param menu            The affinity book menu containing affinity data
     * @param playerInventory Player's inventory (required by AbstractContainerScreen)
     * @param title           The screen title
     */
    public AffinityBookScreen(AffinityBookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // Set GUI dimensions to match recipe book size
        this.imageWidth = 147;
        this.imageHeight = 166;

        // Hide player inventory label (we don't show inventory)
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render dimmed background
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render tooltips (if any)
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Render the book background texture
        graphics.blit(
                BACKGROUND,
                this.leftPos, this.topPos,  // Position
                0, 0,                        // Texture UV offset
                this.imageWidth, this.imageHeight,  // Size
                this.imageWidth, this.imageHeight   // Texture dimensions
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Render title
        graphics.drawString(
                this.font,
                this.title,
                this.titleLabelX,
                this.titleLabelY,
                0xFFFFFF,  // White
                false
        );

        // Render affinities list
        this.renderAffinities(graphics);
    }

    /**
     * Render all affinities with their completion percentages.
     * Coordinates are relative to leftPos/topPos.
     *
     * @param graphics The graphics context
     */
    private void renderAffinities(GuiGraphics graphics) {
        int startY = 25;  // Start 25 pixels from top
        int xOffset = 10; // 10 pixels from left edge

        var affinities = this.menu.getAffinities();

        // Render each affinity with its completion percentage
        for (AffinityBookMenu.AffinityData data : affinities) {
            renderAffinityEntry(graphics, data, xOffset, startY);
            startY += (data.isCompleted() ? 14 : 24);  // Less space if completed (no progress bar)
        }
    }

    /**
     * Render a single affinity entry with completion percentage.
     *
     * @param graphics The graphics context
     * @param data     The affinity data
     * @param barX     X position (relative to leftPos)
     * @param y        Y position (relative to topPos)
     */
    private void renderAffinityEntry(
            GuiGraphics graphics,
            AffinityBookMenu.AffinityData data,
            int barX,
            int y
    ) {
        Affinity affinity = data.affinity();
        int completion = data.completionPercent();
        boolean isCompleted = data.isCompleted();

        // Get affinity color
        int affinityColor = getAffinityColor(affinity);

        // Get progress color (red → orange → green)
        int progressColor = getProgressColor(completion);

        // Render affinity name
        Component name = Component.translatable(
                "affinity.elementalrealms." + affinity.getName()
        );
        graphics.drawString(
                this.font,
                name,
                barX,
                y,
                affinityColor,
                false
        );

        // Render completion percentage
        String percentText = completion + "%";
        graphics.drawString(
                this.font,
                percentText,
                barX + 100,
                y,
                progressColor,
                false
        );

        // Render progress bar ONLY if not completed
        if (!isCompleted) {
            int barY = y + 10;
            int barWidth = 120;
            int barHeight = 3;

            // Background (dark gray)
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF3C3C3C);

            // Progress fill
            int progressWidth = (int) (barWidth * (completion / 100.0f));
            graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, progressColor);

            // Border
            graphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF8B8B8B);
        }
    }

    /**
     * Get color based on completion percentage.
     * 0% = Red, 50% = Orange, 100% = Green
     *
     * @param percent Completion percentage (0-100)
     * @return ARGB color
     */
    private int getProgressColor(int percent) {
        percent = Math.clamp(percent, 0, 100);

        if (percent < 50) {
            // Red to Orange (0% to 50%)
            float ratio = percent / 50.0f;
            int red = 255;
            int green = (int) (0x88 * ratio);
            return 0xFF000000 | (red << 16) | (green << 8);
        } else {
            // Orange to Green (50% to 100%)
            float ratio = (percent - 50) / 50.0f;
            int red = (int) (255 * (1 - ratio));
            int green = (int) (0x88 + (255 - 0x88) * ratio);
            return 0xFF000000 | (red << 16) | (green << 8);
        }
    }

    /**
     * Get the display color for each affinity type.
     *
     * @param affinity The affinity
     * @return ARGB color
     */
    private int getAffinityColor(Affinity affinity) {
        return switch (affinity) {
            case VOID -> 0xFF000000;       // Black
            case FIRE -> 0xFFFF4500;      // Orange-Red
            case WATER -> 0xFF1E90FF;     // Dodger Blue
            case EARTH -> 0xFF8B4513;     // Saddle Brown
            case WIND -> 0xFFE0FFFF;      // Light Cyan
            case LIGHTNING -> 0xFFFFFF00; // Yellow
            case ICE -> 0xFF87CEEB;       // Sky Blue
            case SOUND -> 0xFFDA70D6;     // Orchid
            case GRAVITY -> 0xFF4B0082;   // Indigo
            case TIME -> 0xFFFFD700;      // Gold
            case SPACE -> 0xFF191970;     // Midnight Blue
            case LIFE -> 0xFF32CD32;      // Lime Green
        };
    }

    @Override
    public boolean isPauseScreen() {
        // Don't pause the game when this screen is open
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();

        // Mark that affinity book was closed by the player
        AffinityBookButtonHandler.setShouldAffinityBookBeOpen(false);
    }

    @Override
    public void removed() {
        super.removed();

        // When switching to inventory, keep the book open
        // Only onClose() sets it to false (when player presses ESC)
    }
}
