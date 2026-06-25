package de.piggidragon.elementalrealms;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side initializer. Currently logs a startup message; renderer
 * registration happens in {@link de.piggidragon.elementalrealms.client.events.ClientModEvents}.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public final class ElementalRealmsClient {

    private ElementalRealmsClient() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ElementalRealms.LOGGER.info("Client setup initialized");
    }
}
