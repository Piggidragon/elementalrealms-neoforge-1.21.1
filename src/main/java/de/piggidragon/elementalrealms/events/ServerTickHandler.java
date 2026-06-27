package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.items.magic.equipment.hand.custom.SchoolStaff;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Advances per-tick mod state on the server.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ServerTickHandler {

    private ServerTickHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        SchoolStaff.tickAnimations();
    }
}
