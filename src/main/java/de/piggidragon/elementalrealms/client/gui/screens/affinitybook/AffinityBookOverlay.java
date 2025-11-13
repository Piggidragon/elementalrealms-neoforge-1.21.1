package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Overlay component that renders the affinity book inside the inventory screen.
 * Similar to RecipeBookComponent.
 */
public class AffinityBookOverlay {

    /**
     * Background texture for the affinity book.
     */
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "textures/gui/affinity_book.png");

    /**
     * Width of the affinity book panel.
     */
    private static final int WIDTH = 147;

    /**
     * Height of the affinity book panel.
     */
    private static final int HEIGHT = 166;

    /**
     * Whether the overlay is currently visible.
     */
    private boolean visible = false;

    /**
     * The player whose affinities are displayed.
     */
    private final Player player;

    /**
     * Minecraft instance.
     */
    private final Minecraft minecraft;

    /**
     * Font renderer.
     */
    private final Font font;

    /**
     * Create a new affinity book overlay.
     *
     * @param player The player whose affinities to display
     */
    public AffinityBookOverlay(Player player) {
        this.player = player;
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
    }

    /**
     * Toggle the visibility of the affinity book overlay.
     */
    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    /**
     * Set the visibility state.
     *
     * @param visible Whether the overlay should be visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Check if the overlay is currently visible.
     *
     * @return true if visible
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Render the affinity book overlay.
     *
     * @param graphics    Graphics context
     * @param x           X position (left edge of the overlay)
     * @param y           Y position (top edge of the overlay)
     * @param mouseX      Mouse X position
     * @param mouseY      Mouse Y position
     * @param partialTick Partial tick
     */
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        // Render background texture with correct size parameters
        graphics.blit(
                BACKGROUND,
                x, y,                         // Screen position
                0, 0,                         // Texture UV offset
                WIDTH, HEIGHT,                // Size to render on screen
                256, 256                      // Full texture size (standard PNG size)
        );

        // Render title
        Component title = Component.translatable("gui.elementalrealms.affinity_book");
        int titleX = x + (WIDTH - this.font.width(title)) / 2;
        int titleY = y + 6;
        graphics.drawString(
                this.font,
                title,
                titleX,
                titleY,
                0x404040,  // Dark gray (same as vanilla labels)
                false
        );

        // Render affinities
        this.renderAffinities(graphics, x, y);
    }

    /**
     * Render all affinities with their completion percentages.
     *
     * @param graphics Graphics context
     * @param baseX    Base X position of the overlay
     * @param baseY    Base Y position of the overlay
     */
    private void renderAffinities(GuiGraphics graphics, int baseX, int baseY) {
        int startY = baseY + 20;  // Start 20 pixels from top (adjusted for better spacing)
        int xOffset = baseX + 8;  // 8 pixels from left edge (adjusted)

        // Get all affinities
        Affinity[] affinities = Affinity.values();

        // Render each affinity with its completion percentage
        for (Affinity affinity : affinities) {
            // TODO: Get actual completion percentage from player capability/data
            int completion = getAffinityCompletion(affinity);
            boolean isCompleted = completion >= 100;

            renderAffinityEntry(graphics, affinity, completion, isCompleted, xOffset, startY);
            startY += (isCompleted ? 11 : 20);  // Adjusted spacing
        }
    }

    /**
     * Get affinity completion percentage for the player.
     * TODO: Replace with actual capability/data lookup
     *
     * @param affinity The affinity to check
     * @return Completion percentage (0-100)
     */
    private int getAffinityCompletion(Affinity affinity) {
        // TODO: Get from player capability
        // For now, return placeholder data
        return switch (affinity) {
            case FIRE -> 75;
            case WATER -> 50;
            case EARTH -> 100;
            case WIND -> 25;
            default -> 0;
        };
    }

    /**
     * Render a single affinity entry with completion percentage.
     *
     * @param graphics    The graphics context
     * @param affinity    The affinity type
     * @param completion  Completion percentage (0-100)
     * @param isCompleted Whether the affinity is completed
     * @param x           X position
     * @param y           Y position
     */
    private void renderAffinityEntry(
            GuiGraphics graphics,
            Affinity affinity,
            int completion,
            boolean isCompleted,
            int x,
            int y
    ) {
        // Get affinity color
        int affinityColor = getAffinityColor(affinity);

        // Get progress color (red → orange → green)
        int progressColor = getProgressColor(completion);

        // Render affinity name (smaller font by using scale)
        Component name = Component.translatable(
                "affinity.elementalrealms." + affinity.getName()
        );

        // Scale down text to fit better (0.8 = 80% size)
        graphics.pose().pushPose();
        graphics.pose().scale(0.8f, 0.8f, 1.0f);
        graphics.drawString(
                this.font,
                name,
                (int)(x / 0.8f),
                (int)(y / 0.8f),
                affinityColor,
                false
        );
        graphics.pose().popPose();

        // Render completion percentage
        String percentText = completion + "%";
        graphics.pose().pushPose();
        graphics.pose().scale(0.8f, 0.8f, 1.0f);
        graphics.drawString(
                this.font,
                percentText,
                (int)((x + 90) / 0.8f),  // Adjusted position
                (int)(y / 0.8f),
                progressColor,
                false
        );
        graphics.pose().popPose();

        // Render progress bar ONLY if not completed
        if (!isCompleted) {
            int barY = y + 8;  // Adjusted spacing
            int barWidth = 110;  // Adjusted width
            int barHeight = 2;   // Slightly smaller height

            // Background (dark gray)
            graphics.fill(x, barY, x + barWidth, barY + barHeight, 0xFF3C3C3C);

            // Progress fill
            int progressWidth = (int) (barWidth * (completion / 100.0f));
            graphics.fill(x, barY, x + progressWidth, barY + barHeight, progressColor);

            // Border
            graphics.renderOutline(x, barY, barWidth, barHeight, 0xFF8B8B8B);
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

    /**
     * Get the width of this overlay.
     *
     * @return Overlay width
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Get the height of this overlay.
     *
     * @return Overlay height
     */
    public int getHeight() {
        return HEIGHT;
    }
}
