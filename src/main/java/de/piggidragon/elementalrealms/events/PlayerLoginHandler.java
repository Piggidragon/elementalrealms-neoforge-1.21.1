package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinitiesRoll;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


@EventBusSubscriber(modid = ElementalRealms.MODID)
public class PlayerLoginHandler {
    /**
     * Assigns random affinities to new players on first login.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Skip if player already has affinities
            if (!ModAffinities.getAffinities(player).isEmpty()) {
                return;
            }

            // Roll and assign random affinities
            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player).keySet()) {
                if (affinity != Affinity.VOID) {
                    try {
                        ModAffinities.addAffinity(player, affinity);
                    } catch (IllegalStateException ignored) {
                    }
                }
            }
        }
    }
}
