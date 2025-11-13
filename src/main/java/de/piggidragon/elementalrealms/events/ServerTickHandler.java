package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.items.magic.dimension.custom.SchoolStaff;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Handles server tick updates for animations and effects.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class ServerTickHandler {

    // Tick animations for active portal staffs every server tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        SchoolStaff.tickAnimations();
    }
}
