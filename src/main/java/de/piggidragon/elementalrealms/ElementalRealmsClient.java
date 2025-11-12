package de.piggidragon.elementalrealms;

import de.piggidragon.elementalrealms.guis.menus.ModMenus;
import de.piggidragon.elementalrealms.client.gui.screens.AffinityScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side initialization for Elemental Realms.
 * Handles registration of renderers and other client-only components.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public final class ElementalRealmsClient {

    private ElementalRealmsClient() {
    }

    /**
     * Registers menu screens for client-side rendering
     */
    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.AFFINITY_MENU.get(), AffinityScreen::new);
        ElementalRealms.LOGGER.info("Registered AffinityScreen");
    }

    /**
     * Client setup phase for initialization after registration.
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ElementalRealms.LOGGER.info("Client setup initialized");
    }
}
