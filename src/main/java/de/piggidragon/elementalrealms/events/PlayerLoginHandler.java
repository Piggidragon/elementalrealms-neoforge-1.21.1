package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.helper.AffinitiesRoll;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;

/**
 * Assigns random affinities to first-time players on login.
 *
 * <p>Issue #23: login grants 1 guaranteed ELEMENTAL at 100% (hardcoded) plus 0..N additional
 * affinities at partial completion. The loop inside {@link AffinitiesRoll} decides how many
 * extras and which tier each lands at — see its Javadoc for the distribution spec.</p>
 *
 * <p>Each entry is dispatched based on its completion: {@link ModAffinities#addAffinity} for the
 * 100% guaranteed, {@link ModAffinities#addIncrementAffinity} for partials bounded by
 * {@code maxCompletionPercent}. The two paths together keep Stage 1 at hardcoded 100% even
 * when the user has lowered {@code maxCompletionPercent} in the config.</p>
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class PlayerLoginHandler {

    private PlayerLoginHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ModAffinities.getAffinities(player).isEmpty()) return;

        for (Map.Entry<Affinity, Integer> entry : AffinitiesRoll.rollAffinities(player).entrySet()) {
            Affinity affinity = entry.getKey();
            if (affinity == Affinity.VOID) continue;
            int completion = entry.getValue();
            try {
                if (completion >= AffinityConfig.maxCompletionPercent()) {
                    ModAffinities.addAffinity(player, affinity);
                } else {
                    ModAffinities.addIncrementAffinity(player, affinity, completion);
                }
            } catch (IllegalStateException e) {
                ElementalRealms.LOGGER.debug("Skipped affinity {} for {}: {}", affinity, player, e.getMessage());
            }
        }
    }
}