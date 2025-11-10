package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.OpenAffinityGuiPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles client-side key input events
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class ClientKeyInputHandler {

    /**
     * Checks for key presses every client tick
     * @param event The client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Check if affinity GUI key was pressed
        while (ModKeyBindingsHandler.OPEN_AFFINITY_GUI.consumeClick()) {
            // Send packet to server to open the menu
            ElementalRealms.LOGGER.info("Sending Packet to open Affinity GUI");
            PacketDistributor.sendToServer(new OpenAffinityGuiPacket());
        }
    }
}
