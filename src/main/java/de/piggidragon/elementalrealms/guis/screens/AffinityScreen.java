package de.piggidragon.elementalrealms.guis.screens;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.guis.menus.custom.AffinityMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for displaying player affinities
 * Shows all affinities with completion bars
 */
public class AffinityScreen extends AbstractContainerScreen<AffinityMenu> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ElementalRealms.MODID,
            "textures/gui/affinity_screen.png"
    );

    /**
     * Creates a new affinity screen
     * @param menu The affinity menu containing the data
     * @param playerInventory Player's inventory (required by AbstractContainerScreen)
     * @param title The screen title
     */
    public AffinityScreen(AffinityMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        ElementalRealms.LOGGER.info("=== AFFINITY SCREEN CONSTRUCTOR CALLED ===");

        // Set GUI dimensions (background texture size)
        this.imageWidth = 176;
        this.imageHeight = 190; // Increased height for affinity list

        // DON'T calculate titleLabelX here - this.font is not initialized yet!
        // We'll do it in init() instead

        // Hide player inventory label (we don't show player inventory)
        this.inventoryLabelY = 10000; // Move offscreen

        ElementalRealms.LOGGER.info("=== SCREEN CONSTRUCTOR FINISHED ===");
    }

    @Override
    protected void init() {
        super.init();

        ElementalRealms.LOGGER.info("=== SCREEN INIT CALLED ===");

        // NOW we can safely use this.font
        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        ElementalRealms.LOGGER.info("=== SCREEN INIT FINISHED ===");
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
        this.renderAffinities(graphics, mouseX, mouseY);
    }

    /**
     * Renders all affinities with completion bars
     * Coordinates are relative to leftPos/topPos
     * @param graphics The graphics context
     * @param mouseX Mouse X position (screen coordinates)
     * @param mouseY Mouse Y position (screen coordinates)
     */
    private void renderAffinities(GuiGraphics graphics, int mouseX, int mouseY) {
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
            for (AffinityMenu.AffinityData data : completed) {
                renderAffinityEntry(graphics, data, xOffset, startY, mouseX, mouseY);
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
            for (AffinityMenu.AffinityData data : incomplete) {
                renderAffinityEntry(graphics, data, xOffset, startY, mouseX, mouseY);
                startY += 18;
            }
        }
    }

    /**
     * Renders a single affinity entry with progress bar
     * Coordinates are relative to leftPos/topPos
     * @param graphics The graphics context
     * @param data The affinity data to render
     * @param x Relative X position
     * @param y Relative Y position
     * @param mouseX Mouse X position (screen coordinates)
     * @param mouseY Mouse Y position (screen coordinates)
     */
    private void renderAffinityEntry(
            GuiGraphics graphics,
            AffinityMenu.AffinityData data,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        // 1. Render affinity name
        Component name = Component.translatable(
                "affinity.elementalrealms." + data.getAffinity().name().toLowerCase()
        );
        graphics.drawString(this.font, name, x, y, 0xFFFFFF, false);

        // 2. Render completion percentage text
        String percentText = data.getCompletionPercent() + "%";
        int textColor = data.isCompleted() ? 0x55FF55 : 0xFFFFFF;
        graphics.drawString(this.font, percentText, x + 120, y, textColor, false);

        // 3. Render progress bar
        int barX = x;
        int barY = y + 10;
        int barWidth = 150;
        int barHeight = 4;

        // Background bar (dark)
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF3C3C3C);

        // Progress fill (colored)
        int progressWidth = (int) (barWidth * (data.getCompletionPercent() / 100.0f));
        int barColor = data.isCompleted() ? 0xFF55FF55 : 0xFFFFAA00;
        graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, barColor);

        // Border
        graphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF8B8B8B);
    }

    @Override
    public boolean isPauseScreen() {
        // Don't pause game when GUI is open
        return false;
    }
}
