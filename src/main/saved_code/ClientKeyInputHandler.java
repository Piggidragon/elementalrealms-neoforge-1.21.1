package de.piggidragon.elementalrealms.client.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.events.ModKeyBindingsHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Handles client-side key input events
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class ClientKeyInputHandler {

    // Store reference to affinity book component
    private static AffinityBookComponent affinityBook = null;

    /**
     * Get or create the affinity book component
     *
     * @return The affinity book component
     */
    public static AffinityBookComponent getAffinityBook() {
        if (affinityBook == null) {
            affinityBook = new AffinityBookComponent();
            Minecraft mc = Minecraft.getInstance();
            affinityBook.init(mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight(),
                    mc);
        }
        return affinityBook;
    }

    /**
     * Checks for key presses every client tick
     *
     * @param event The client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Check if affinity GUI key was pressed
        while (ModKeyBindingsHandler.OPEN_AFFINITY_GUI.consumeClick()) {
            // Toggle the affinity book instead of opening menu
            AffinityBookComponent book = getAffinityBook();
            book.toggleVisibility();
        }
    }
}
