package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.helper.AffinitiesRoll;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Assigns random affinities to first-time players on login.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class PlayerLoginHandler {

    private PlayerLoginHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ModAffinities.getAffinities(player).isEmpty()) return;

        for (Affinity affinity : AffinitiesRoll.rollAffinities(player).keySet()) {
            if (affinity == Affinity.VOID) continue;
            try {
                ModAffinities.addAffinity(player, affinity);
            } catch (IllegalStateException e) {
                ElementalRealms.LOGGER.debug("Skipped affinity {} for {}: {}", affinity, player, e.getMessage());
            }
        }
    }
}
