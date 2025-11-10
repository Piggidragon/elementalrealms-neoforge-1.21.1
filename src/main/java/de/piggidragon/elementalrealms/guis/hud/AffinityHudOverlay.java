package de.piggidragon.elementalrealms.guis.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HUD overlay that shows affinity stones on the right side of the screen
 * Can be toggled on/off and doesn't pause the game
 */
public class AffinityHudOverlay {

    private static final int ICON_SIZE = 32;
    private static final int ICON_SPACING = 8;
    private static final int MARGIN_RIGHT = 10; // Distance from right edge

    // Toggle state - controlled by keybind
    private static boolean visible = false;

    /**
     * Toggles the visibility of the affinity HUD
     */
    public static void toggle() {
        visible = !visible;
        ElementalRealms.LOGGER.info("Affinity HUD toggled: " + visible);
    }

    /**
     * Sets the visibility of the affinity HUD
     */
    public static void setVisible(boolean isVisible) {
        visible = isVisible;
    }

    /**
     * Checks if the affinity HUD is currently visible
     */
    public static boolean isVisible() {
        return visible;
    }

    /**
     * Renders the affinity HUD overlay
     * Called by RenderGuiLayerEvent
     */
    public static void render(GuiGraphics graphics) {
        if (!visible) {
            return; // Don't render if toggled off
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        Map<Affinity, Integer> affinityMap = mc.player.getData(ModAttachments.AFFINITIES.get());

        // Convert map to list for rendering
        List<AffinityData> affinities = new ArrayList<>();
        for (Map.Entry<Affinity, Integer> entry : affinityMap.entrySet()) {
            affinities.add(new AffinityData(entry.getKey(), entry.getValue()));
        }

        if (affinities.isEmpty()) {
            return;
        }

        // Calculate positions
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int startX = screenWidth - ICON_SIZE - MARGIN_RIGHT;
        int totalHeight = affinities.size() * (ICON_SIZE + ICON_SPACING) - ICON_SPACING;
        int startY = (screenHeight - totalHeight) / 2; // Centered vertically

        int currentY = startY;

        // Get mouse position for hover detection
        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight();

        // Render each affinity icon
        AffinityData hoveredAffinity = null;
        int hoveredMouseX = 0;
        int hoveredMouseY = 0;

        for (AffinityData data : affinities) {
            int x = startX;
            int y = currentY;

            // Check hover
            boolean isHovering = mouseX >= x && mouseX <= x + ICON_SIZE
                    && mouseY >= y && mouseY <= y + ICON_SIZE;

            renderAffinityIcon(graphics, data, x, y);

            if (isHovering) {
                hoveredAffinity = data;
                hoveredMouseX = (int) mouseX;
                hoveredMouseY = (int) mouseY;
            }

            currentY += ICON_SIZE + ICON_SPACING;
        }

        // Render tooltip last (on top of everything)
        if (hoveredAffinity != null) {
            renderTooltip(graphics, hoveredAffinity, hoveredMouseX, hoveredMouseY);
        }
    }

    /**
     * Renders a single affinity icon with fill-up effect
     */
    private static void renderAffinityIcon(GuiGraphics graphics, AffinityData data, int x, int y) {
        ResourceLocation texture = getAffinityTexture(data.affinity);

        int completion = data.completionPercent;
        int fillHeight = (int) (ICON_SIZE * (completion / 100.0f));
        int grayHeight = ICON_SIZE - fillHeight;

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Render GRAY top part
        if (grayHeight > 0) {
            graphics.setColor(0.3f, 0.3f, 0.3f, 1.0f);
            graphics.blit(
                    texture,
                    x, y,
                    0, 0,
                    ICON_SIZE, grayHeight,
                    ICON_SIZE, ICON_SIZE
            );
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        // 2. Render COLOR bottom part
        if (fillHeight > 0) {
            graphics.blit(
                    texture,
                    x, y + grayHeight,
                    0, grayHeight,
                    ICON_SIZE, fillHeight,
                    ICON_SIZE, ICON_SIZE
            );
        }

        RenderSystem.disableBlend();
    }

    /**
     * Renders tooltip for hovered affinity
     */
    private static void renderTooltip(GuiGraphics graphics, AffinityData data, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        Component name = Component.translatable(
                "affinity.elementalrealms." + data.affinity.name().toLowerCase()
        );

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.literal(name.getString()).withStyle(style -> style.withColor(getAffinityColor(data.affinity))));

        if (!data.isCompleted()) {
            int color = getProgressColor(data.completionPercent);
            tooltipLines.add(
                    Component.literal(data.completionPercent + "%")
                            .withStyle(style -> style.withColor(color))
            );
        }

        graphics.renderComponentTooltip(mc.font, tooltipLines, mouseX, mouseY);
    }

    /**
     * Gets affinity texture location
     */
    private static ResourceLocation getAffinityTexture(Affinity affinity) {
        String textureName = "affinity_stone_" + affinity.name().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(
                ElementalRealms.MODID,
                "textures/item/" + textureName + ".png"
        );
    }

    /**
     * Gets the color for a specific affinity type
     * Used for the affinity name display
     */
    private static int getAffinityColor(Affinity affinity) {
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

    /**
     * Calculates progress color gradient
     */
    private static int getProgressColor(int percent) {
        percent = Math.clamp(percent, 0, 100);

        if (percent < 50) {
            float ratio = percent / 50.0f;
            int red = 255;
            int green = (int) (0x88 * ratio);
            return 0xFF000000 | (red << 16) | (green << 8);
        } else {
            float ratio = (percent - 50) / 50.0f;
            int red = (int) (255 * (1 - ratio));
            int green = (int) (0x88 + (255 - 0x88) * ratio);
            return 0xFF000000 | (red << 16) | (green << 8);
        }
    }

    /**
     * Data class for affinity information
     */
    private static class AffinityData {
        final Affinity affinity;
        final int completionPercent;

        AffinityData(Affinity affinity, int completionPercent) {
            this.affinity = affinity;
            this.completionPercent = Math.clamp(completionPercent, 0, 100);
        }

        boolean isCompleted() {
            return completionPercent >= 100;
        }
    }
}
