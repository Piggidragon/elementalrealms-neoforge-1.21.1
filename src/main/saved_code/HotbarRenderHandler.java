package de.piggidragon.elementalrealms.events;

import com.mojang.blaze3d.systems.RenderSystem;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.guis.hud.AffinityHotbarManager;
import de.piggidragon.elementalrealms.registries.guis.hud.AffinityHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Event handler for custom hotbar rendering.
 * Replaces the vanilla hotbar with a sliding custom hotbar with Affinity icons.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class HotbarRenderHandler {
    /**
     * Texture for the custom hotbar
     */
    private static final ResourceLocation CUSTOM_HOTBAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ElementalRealms.MODID, "hud/hotbar"
    );
    private static final ResourceLocation CUSTOM_HOTBAR_CURSER = ResourceLocation.fromNamespaceAndPath(
            ElementalRealms.MODID, "hud/hotbar_curser"
    );

    /**
     * Intercepts vanilla hotbar rendering and replaces it during animation.
     *
     * @param event Pre-render event for GUI layer
     */
    @SubscribeEvent
    public static void onRenderHotbarPre(RenderGuiLayerEvent.Pre event) {
        // Only intercept hotbar layer
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        // Replace vanilla hotbar when custom is active or during animation
        if (AffinityHotbarManager.isReplaced() || AffinityHotbarManager.isAnimating()) {
            event.setCanceled(true);
            renderSlideHotbar(event.getGuiGraphics());
        }
    }

    /**
     * Renders the custom hotbar with slide animation.
     *
     * @param graphics GuiGraphics for rendering
     */
    private static void renderSlideHotbar(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // === Calculate base position (screen center) ===
        int baseX = sw / 2 - 91;
        int baseY = sh - 22;

        // === Calculate slide animation ===
        float progress = AffinityHotbarManager.getSlideProgress();
        int slideDistance = 200; // Pixel distance for sliding effect
        int offsetX = (int) (slideDistance * (1 - progress));
        int posX = baseX + offsetX;

        // === Render hotbar background ===
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CUSTOM_HOTBAR_TEXTURE);
        RenderSystem._setShaderTexture(0, CUSTOM_HOTBAR_CURSER);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int i = graphics.guiWidth() / 2;
        graphics.blitSprite(CUSTOM_HOTBAR_TEXTURE, i - 91, graphics.guiHeight() - 22, 182, 22);
        graphics.blitSprite(CUSTOM_HOTBAR_CURSER, i - 91 - 1 + mc.player.getInventory().selected * 20, graphics.guiHeight() - 22 - 1, 24, 23);

        /*
        // === Render selection highlight ===
        if (!mc.player.isSpectator()) {
            int sel = mc.player.getInventory().selected;
            int highlightX = posX + sel * 20 - 1;
            int highlightY = baseY - 1;
            graphics.blit(CUSTOM_HOTBAR_TEXTURE, highlightX, highlightY, 0, 22, 24, 24);
        }
         */

        // Render Affinity icons above the hotbar
        AffinityHudOverlay.render(graphics, mc);
    }
}
