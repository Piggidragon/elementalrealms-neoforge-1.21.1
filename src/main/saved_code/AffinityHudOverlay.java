package de.piggidragon.elementalrealms.registries.guis.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.AffinityType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * HUD overlay for Affinity icons in the hotbar.
 * Displays player affinities with progress indication and zoom animation.
 * Renders ABOVE the hotbar to cover item stack numbers.
 */
public class AffinityHudOverlay {

    // === Layout Constants ===
    private static final int ICON_SIZE = 16;
    private static final int HOTBAR_SLOT_SIZE = 20;
    private static final int MAX_SLOTS = 9;

    // === Animation Constants ===
    private static final float ANIMATION_DURATION = 400f;
    private static boolean isAnimating = false;

    /**
     * Main render method for the Affinity overlay.
     * Called every frame.
     *
     * @param graphics GuiGraphics for rendering
     * @param mc Minecraft instance
     */
    public static void render(GuiGraphics graphics, Minecraft mc) {
        // Get affinity data from player
        Map<Affinity, Integer> affinityMap = mc.player.getData(ModAttachments.AFFINITIES.get());

        // Don't display for VOID affinity
        if (affinityMap.containsKey(Affinity.VOID)) {
            return;
        }

        // Sort and prepare affinities
        List<AffinityData> affinities = getSortedAffinities(affinityMap);
        if (affinities.isEmpty()) {
            return;
        }

        // === Calculate zoom animation (ease-out) ===
        float progress = 1.0f;
        if (isAnimating) {
            long elapsed = System.currentTimeMillis();
            progress = Math.min(1.0f, elapsed / ANIMATION_DURATION);
            if (progress >= 1.0f) {
                isAnimating = false;
            }
        }
        float scale = (float) (1 - Math.pow(1 - progress, 3)); // Cubic ease-out

        // === Calculate hotbar position ===
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int hotbarX = screenWidth / 2 - 91;
        int hotbarY = screenHeight - 22;

        // Render icons with animation
        renderAffinityIcons(graphics, affinities, hotbarX, hotbarY, scale);

        // Render tooltips on hover
        renderHoverTooltip(graphics, affinities, hotbarX, hotbarY);
    }

    /**
     * Renders Affinity icons with zoom animation from center.
     *
     * @param graphics GuiGraphics for rendering
     * @param affinities List of affinities to display
     * @param hotbarX X position of hotbar
     * @param hotbarY Y position of hotbar
     * @param scale Animation scale factor (0.0 - 1.0)
     */
    public static void renderAffinityIcons(
            GuiGraphics graphics,
            List<AffinityData> affinities,
            int hotbarX,
            int hotbarY,
            float scale
    ) {
        for (int i = 0; i < Math.min(affinities.size(), MAX_SLOTS); i++) {
            AffinityData data = affinities.get(i);

            // Calculate slot center for zoom effect
            int slotX = hotbarX + i * HOTBAR_SLOT_SIZE + 3;
            int slotY = hotbarY + 3;
            int centerX = slotX + ICON_SIZE / 2;
            int centerY = slotY + ICON_SIZE / 2;

            renderZoomingIcon(graphics, data, centerX, centerY, scale);
        }
    }

    /**
     * Renders a single icon with zoom effect from center (0% → 100% size).
     * Shows progress through partial grayscale rendering.
     *
     * @param graphics GuiGraphics for rendering
     * @param data Affinity data (type & progress)
     * @param centerX X coordinate of icon center
     * @param centerY Y coordinate of icon center
     * @param scale Zoom factor (0.0 - 1.0)
     */
    private static void renderZoomingIcon(
            GuiGraphics graphics,
            AffinityData data,
            int centerX,
            int centerY,
            float scale
    ) {
        // Skip at minimal scale
        if (scale < 0.01f) return;

        ResourceLocation texture = getAffinityTexture(data.affinity);
        int completion = data.completionPercent;

        // === Calculate progress fill ===
        float fillRatio = completion / 100.0f;
        int fillPixelsOriginal = (int) (ICON_SIZE * fillRatio);
        int grayPixelsOriginal = ICON_SIZE - fillPixelsOriginal;

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.pose().pushPose();

        // === Transform for zoom effect ===
        graphics.pose().translate(centerX, centerY, 0.0F);              // Move to center
        graphics.pose().scale(scale, scale, 1f);                         // Scale
        graphics.pose().translate(-ICON_SIZE / 2f, -ICON_SIZE / 2f, 0.0F); // Back to top-left

        // Render gray (incomplete) part
        if (grayPixelsOriginal > 0) {
            graphics.setColor(0.3f, 0.3f, 0.3f, 1.0f);
            graphics.blit(
                    texture,
                    0, 0,
                    0, 0,
                    ICON_SIZE, grayPixelsOriginal,
                    ICON_SIZE, ICON_SIZE
            );
        }

        // Render colored (complete) part
        if (fillPixelsOriginal > 0) {
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            graphics.blit(
                    texture,
                    0, grayPixelsOriginal,
                    0, grayPixelsOriginal,
                    ICON_SIZE, fillPixelsOriginal,
                    ICON_SIZE, ICON_SIZE
            );
        }

        // Cleanup
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    /**
     * Renders tooltip when hovering over an Affinity icon.
     *
     * @param graphics GuiGraphics for rendering
     * @param affinities List of affinities
     * @param hotbarX X position of hotbar
     * @param hotbarY Y position of hotbar
     */
    public static void renderHoverTooltip(
            GuiGraphics graphics,
            List<AffinityData> affinities,
            int hotbarX,
            int hotbarY
    ) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Calculate mouse position relative to GUI
        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getScreenHeight();

        // Check hover for each slot
        for (int i = 0; i < Math.min(affinities.size(), MAX_SLOTS); i++) {
            AffinityData data = affinities.get(i);
            int slotX = hotbarX + i * HOTBAR_SLOT_SIZE + 3;
            int slotY = hotbarY + 3;

            // Collision check
            if (mouseX >= slotX && mouseX <= slotX + ICON_SIZE &&
                    mouseY >= slotY && mouseY <= slotY + ICON_SIZE) {
                renderTooltip(graphics, data, (int) mouseX, (int) mouseY);
                break;
            }
        }
    }

    /**
     * Renders the tooltip for an affinity with name and progress.
     */
    private static void renderTooltip(GuiGraphics graphics, AffinityData data, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        // Affinity name (translated)
        Component name = Component.translatable(
                "affinity.elementalrealms." + data.affinity.name().toLowerCase()
        );

        List<Component> lines = new ArrayList<>();

        // First line: Name in affinity color
        lines.add(Component.literal(name.getString())
                .withStyle(style -> style.withColor(getAffinityColor(data.affinity))));

        // Second line: Progress or "MASTERED"
        if (!data.isCompleted()) {
            lines.add(Component.literal(data.completionPercent + "%")
                    .withStyle(style -> style.withColor(getProgressColor(data.completionPercent))));
        } else {
            lines.add(Component.literal("✓ MASTERED")
                    .withStyle(style -> style.withColor(0x00FF00)));
        }

        graphics.renderComponentTooltip(mc.font, lines, mouseX, mouseY);
    }

    /**
     * Sorts affinities by type priority (ELEMENTAL → DEVIANT → ETERNAL).
     */
    private static List<AffinityData> getSortedAffinities(Map<Affinity, Integer> affinityMap) {
        List<AffinityData> affinities = new ArrayList<>();
        for (Map.Entry<Affinity, Integer> entry : affinityMap.entrySet()) {
            affinities.add(new AffinityData(entry.getKey(), entry.getValue()));
        }
        affinities.sort(Comparator.comparingInt(a -> getTypePriority(a.affinity.getType())));
        return affinities;
    }

    /**
     * Determines sort priority for affinity types.
     */
    private static int getTypePriority(AffinityType type) {
        return switch (type) {
            case NONE -> -1;
            case ELEMENTAL -> 0;
            case DEVIANT -> 1;
            case ETERNAL -> 2;
        };
    }

    /**
     * Returns the texture ResourceLocation for an affinity.
     */
    private static ResourceLocation getAffinityTexture(Affinity affinity) {
        return ResourceLocation.fromNamespaceAndPath(
                ElementalRealms.MODID,
                "textures/item/affinity_stone_" + affinity.name().toLowerCase() + ".png"
        );
    }

    /**
     * Returns the color for an affinity in tooltips.
     */
    private static int getAffinityColor(Affinity affinity) {
        return switch (affinity) {
            case VOID -> 0xFF000000;
            case FIRE -> 0xFFFF4500;
            case WATER -> 0xFF1E90FF;
            case EARTH -> 0xFF8B4513;
            case WIND -> 0xFFE0FFFF;
            case LIGHTNING -> 0xFFFFFF00;
            case ICE -> 0xFF87CEEB;
            case SOUND -> 0xFFDA70D6;
            case GRAVITY -> 0xFF4B0082;
            case TIME -> 0xFFFFD700;
            case SPACE -> 0xFF191970;
            case LIFE -> 0xFF32CD32;
        };
    }

    /**
     * Calculates color based on progress (0-100%).
     * Red (0%) → Orange (50%) → Green (100%)
     */
    private static int getProgressColor(int percent) {
        percent = Math.clamp(percent, 0, 100);

        if (percent < 50) {
            // 0-50%: Red → Orange
            float ratio = percent / 50.0f;
            return 0xFF000000 | (255 << 16) | ((int)(0x88 * ratio) << 8);
        } else {
            // 50-100%: Orange → Green
            float ratio = (percent - 50) / 50.0f;
            int red = (int)(255 * (1 - ratio));
            int green = (int)(0x88 + (255 - 0x88) * ratio);
            return 0xFF000000 | (red << 16) | (green << 8);
        }
    }

    /**
     * Data class for affinity with progress.
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
