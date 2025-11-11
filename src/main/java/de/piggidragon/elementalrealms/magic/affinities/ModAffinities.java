package de.piggidragon.elementalrealms.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
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
        Map<Affinity, Integer> affinitiesImmutable = getAffinities(player);

        // Validate before adding
        validateAffinityCanBeAdded(affinity, affinitiesImmutable);

        Map<Affinity, Integer> affinitiesMutable = new HashMap<>(affinitiesImmutable);

        if (affinitiesMutable.containsKey(Affinity.VOID)) {
            affinitiesMutable.remove(Affinity.VOID);
        }
        // Add affinity with 100% completion
        affinitiesMutable.put(affinity, 100);

        // Save changes
        player.setData(ModAttachments.AFFINITIES.get(), affinitiesMutable);
    }

    public static void addIncrementAffinity(ServerPlayer player, Affinity affinity, int increment) throws IllegalStateException {
        Map<Affinity, Integer> affinitiesImmutable = getAffinities(player);

        validateAffinityCanBeAdded(affinity, affinitiesImmutable);

        Map<Affinity, Integer> affinitiesMutable = new HashMap<>(affinitiesImmutable);

        int current = affinitiesMutable.getOrDefault(affinity, 0);
        int newCompletion = Math.min(current + increment, 100);

        if (affinitiesMutable.containsKey(Affinity.VOID)) {
            affinitiesMutable.remove(Affinity.VOID);
        }
        // Add affinity with 0% completion
        affinitiesImmutable.put(affinity, newCompletion);

        // Save changes
        player.setData(ModAttachments.AFFINITIES.get(), affinitiesMutable);
    }

    /**
     * Validates if an affinity can be added to the player
     * Throws exception if validation fails
     */
    private static void validateAffinityCanBeAdded(
            Affinity affinity,
            Map<Affinity, Integer> currentAffinities
    ) {
        // Prevent duplicate
        if (currentAffinities.containsKey(affinity)) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }

        // Check ETERNAL limit
        if (affinity.getType() == AffinityType.ETERNAL && hasEternalAffinity(currentAffinities)) {
            throw new IllegalStateException("Player already has an eternal affinity");
        }

        // Check DEVIANT requires base elemental
        if (affinity.getType() == AffinityType.DEVIANT && !hasBaseAffinity(currentAffinities, affinity)) {
            throw new IllegalStateException(
                    "Player is missing base affinity for deviant: " + affinity.getElemental()
            );
        }
    }

    /**
     * Checks if player has an eternal affinity
     */
    private static boolean hasEternalAffinity(Map<Affinity, Integer> affinities) {
        return affinities.keySet().stream()
                .anyMatch(a -> a.getType() == AffinityType.ETERNAL);
    }

    /**
     * Checks if player has the required base affinity for a deviant
     */
    private static boolean hasBaseAffinity(Map<Affinity, Integer> affinities, Affinity deviant) {
        return affinities.keySet().stream()
                .anyMatch(a -> a.getDeviant() == deviant);
    }

    /**
     * Clears all affinities and sets to VOID.
     *
     * @param player Target player
     * @throws Exception If player already has no affinities
     */
    public static void clearAffinities(ServerPlayer player) throws IllegalStateException {
        Map<Affinity, Integer> affinitiesImmutable = getAffinities(player);

        if (affinitiesImmutable.containsKey(Affinity.VOID)) {
            throw new IllegalStateException("Player has no affinities to clear.");
        }

        Map<Affinity, Integer> affinitiesMutable = new HashMap<>(affinitiesImmutable);

        affinitiesMutable.clear();
        affinitiesMutable.put(Affinity.VOID, 0);
        player.setData(ModAttachments.AFFINITIES.get(), affinitiesMutable);
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
            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player).keySet()) {
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