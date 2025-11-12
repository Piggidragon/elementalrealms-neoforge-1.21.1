package de.piggidragon.elementalrealms.client.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.client.EmptyPortalRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side event handlers for entity renderers.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    /**
     * Registers entity renderers for custom entities.
     *
     * @param event the renderer registration event
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register empty renderer for portal (actual rendering via Lodestone particles)
        event.registerEntityRenderer(ModEntities.PORTAL_ENTITY.get(), EmptyPortalRenderer::new);
    }
}
