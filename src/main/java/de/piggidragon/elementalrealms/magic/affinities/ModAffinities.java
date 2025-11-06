package de.piggidragon.elementalrealms.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

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
    public static void addAffinity(ServerPlayer player, Affinity affinity) throws Exception {
        List<Affinity> affinities = getAffinities(player);

        // Prevent duplicate
        if (affinities.contains(affinity)) {
            throw new Exception("Player already has affinity: " + affinity);
        }

        // Check ETERNAL limit (only one per player)
        if (affinity.getType() == AffinityType.ETERNAL) {
            for (Affinity a : affinities) {
                if (a.getType() == AffinityType.ETERNAL) {
                    throw new Exception("Player already has an eternal affinity: " + a);
                }
            }
        }

        // Check DEVIANT requires base elemental
        if (affinity.getType() == AffinityType.DEVIANT) {
            boolean hasBase = false;
            for (Affinity a : affinities) {
                if (a.getDeviant() == affinity) {
                    hasBase = true;
                    break;
                }
            }
            if (!hasBase) {
                throw new Exception("Player is missing base affinity: " + affinity.getElemental());
            }
        }

        // Remove VOID placeholder before adding real affinity
        affinities.remove(Affinity.VOID);
        affinities.add(affinity);
    }

    /**
     * Clears all affinities and sets to VOID.
     *
     * @param player Target player
     * @throws Exception If player already has no affinities
     */
    public static void clearAffinities(ServerPlayer player) throws Exception {
        List<Affinity> affinities = getAffinities(player);

        if (affinities.contains(Affinity.VOID)) {
            throw new Exception("Player has no affinities to clear.");
        }

        affinities.clear();
        affinities.add(Affinity.VOID);
    }

    /**
     * Gets mutable list of player's affinities.
     * Changes are automatically saved via data attachments.
     *
     * @param player Target player
     * @return Mutable affinity list
     */
    public static List<Affinity> getAffinities(ServerPlayer player) {
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
        return getAffinities(player).contains(affinity);
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
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}