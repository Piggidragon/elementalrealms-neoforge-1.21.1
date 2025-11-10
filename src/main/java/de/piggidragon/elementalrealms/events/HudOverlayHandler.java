package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.guis.hud.AffinityHudOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Registers custom GUI layers/overlays
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class HudOverlayHandler {

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Register affinity HUD overlay above hotbar
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,  // Render above hotbar
                ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_hud"),
                (GuiGraphics graphics, DeltaTracker deltaTracker) -> {
                    AffinityHudOverlay.render(graphics, deltaTracker);
                }
        );

        ElementalRealms.LOGGER.info("Registered Affinity HUD Overlay");
    }
}
