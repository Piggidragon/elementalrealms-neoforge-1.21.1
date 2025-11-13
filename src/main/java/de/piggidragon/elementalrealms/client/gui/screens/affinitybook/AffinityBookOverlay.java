package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

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

        // Render background texture
        graphics.blit(
                BACKGROUND,
                x - 2, y,           // Screen position
                1, 1,               // Texture UV offset
                WIDTH, HEIGHT,      // Size to render on screen
                256, 256            // Full texture size
        );

        // Render player affinities
        this.renderAffinities(graphics, x, y, this.player);
    }

    /**
     * Render all affinities with their completion percentages.
     *
     * @param graphics Graphics context
     * @param baseX    Base X position of the overlay
     * @param baseY    Base Y position of the overlay
     * @param player   The player whose affinities to render
     */
    private void renderAffinities(GuiGraphics graphics, int baseX, int baseY, Player player) {
        // Start position for first entry
        int startY = baseY + 12;
        int xOffset = baseX + 10;

        Map<Affinity, Integer> affinityCompletionMap = player.getData(ModAttachments.AFFINITIES.get());

        // Vertical spacing between entries
        int rowHeight = 12;

        // Render each affinity with its completion percentage
        for (Affinity affinity : affinityCompletionMap.keySet()) {
            int completion = affinityCompletionMap.get(affinity);
            boolean isCompleted = completion >= 100;

            renderAffinityEntry(graphics, affinity, completion, isCompleted, xOffset, startY);
            startY += rowHeight;
        }
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
        // Get display colors
        int affinityColor = getAffinityColor(affinity);
        int progressColor = getProgressColor(completion);

        // Render affinity name
        Component name = Component.translatable("affinity.elementalrealms." + affinity.getName());
        graphics.drawString(this.font, name, x, y, affinityColor, false);

        // Only render progress bar for incomplete affinities
        if (!isCompleted) {
            // Progress bar dimensions
            int barY = y + 9;
            int barWidth = 70;
            int barHeight = 2;

            // Render completion percentage (right-aligned)
            String percentText = completion + "%";
            int percentX = x + 87;
            graphics.drawString(this.font, percentText, percentX, y, progressColor, false);

            // Render progress bar background
            graphics.fill(x, barY, x + barWidth, barY + barHeight, 0xFF3C3C3C);

            // Render progress fill
            int progressWidth = (int) (barWidth * (completion / 100.0f));
            graphics.fill(x, barY, x + progressWidth, barY + barHeight, progressColor);

            // Render progress bar border
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
