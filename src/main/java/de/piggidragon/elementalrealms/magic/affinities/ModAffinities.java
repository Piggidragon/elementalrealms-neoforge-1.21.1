package de.piggidragon.elementalrealms.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages player affinity data with validation rules.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class ModAffinities {

    /**
     * Adds affinity to player with validation.
     * Checks for duplicates, ETERNAL limit, and DEVIANT requirements.
     *
     * @param player   Target player
     * @param affinity Affinity to add
     * @throws Exception If validation fails
     */
    public static void addAffinity(ServerPlayer player, Affinity affinity) throws IllegalStateException {
        Map<Affinity, Integer> affinities = getAffinities(player);

        // Prevent duplicate
        if (affinities.containsKey(affinity)) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }

        // Check ETERNAL limit (only one per player)
        if (affinity.getType() == AffinityType.ETERNAL) {
            for (Affinity a : affinities.keySet()) {
                if (a.getType() == AffinityType.ETERNAL) {
                    throw new IllegalStateException("Player already has an eternal affinity: " + a);
                }
            }
        }

        // Check DEVIANT requires base elemental
        if (affinity.getType() == AffinityType.DEVIANT) {
            boolean hasBase = false;
            for (Affinity a : affinities.keySet()) {
                if (a.getDeviant() == affinity) {
                    hasBase = true;
                    break;
                }
            }
            if (!hasBase) {
                throw new IllegalStateException("Player is missing base affinity: " + affinity.getElemental());
            }
        }
        affinities.put(affinity, 100);
        player.setData(ModAttachments.AFFINITIES.get(), affinities);
    }

    /**
     * Clears all affinities and sets to VOID.
     *
     * @param player Target player
     * @throws Exception If player already has no affinities
     */
    public static void clearAffinities(ServerPlayer player) throws IllegalStateException {
        Map<Affinity, Integer> affinities = getAffinities(player);

        if (affinities.containsKey(Affinity.VOID)) {
            throw new IllegalStateException("Player has no affinities to clear.");
        }

        affinities.clear();
        affinities.put(Affinity.VOID, null);
        player.setData(ModAttachments.AFFINITIES.get(), affinities);
    }

    /**
     * Gets mutable list of player's affinities.
     * Changes are automatically saved via data attachments.
     *
     * @param player Target player
     * @return Mutable affinity list
     */
    public static Map<Affinity, Integer> getAffinities(ServerPlayer player) {
        return player.getData(ModAttachments.AFFINITIES.get());
    }

    /**
     * Checks if player has specific affinity.
     *
     * @param player   Target player
     * @param affinity Affinity to check
     * @return true if player has this affinity
     */
    public static boolean hasAffinity(ServerPlayer player, Affinity affinity) {
        return getAffinities(player).containsKey(affinity);
    }

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
            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player)) {
                if (affinity != Affinity.VOID) {
                    try {
                        addAffinity(player, affinity);
                    } catch (IllegalStateException ignored) {
                    }
                }
            }
        }
    }
}