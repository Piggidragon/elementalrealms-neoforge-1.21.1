package de.piggidragon.elementalrealms.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages player affinity data with validation rules.
 */
public class ModAffinities {

    /**
     * Adds affinity to player with validation.
     * Checks for duplicates, ETERNAL limit, and DEVIANT requirements.
     *
     * @param player   Target player
     * @param affinity Affinity to add
     * @throws IllegalArgumentException If validation fails
     */
    public static void addAffinity(ServerPlayer player, Affinity affinity) throws IllegalStateException {
        Map<Affinity, Integer> affinitiesImmutable = getAffinities(player);

        // Prevent duplicate
        if (affinitiesImmutable.containsKey(affinity) && affinitiesImmutable.get(affinity) >= 100) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }

        // Validate before adding
        validateAffinityCanBeAdded(affinity, affinitiesImmutable);

        Map<Affinity, Integer> affinitiesMutable = new HashMap<>(affinitiesImmutable);

        player.playNotifySound(
                playAffinitySound(player, affinity),
                SoundSource.PLAYERS,
                0.5f,
                0.5f
        );

        affinitiesMutable.remove(Affinity.VOID);
        // Add affinity with 100% completion
        affinitiesMutable.put(affinity, 100);

        // Save changes
        player.setData(ModAttachments.AFFINITIES.get(), affinitiesMutable);
    }

    public static void addIncrementAffinity(ServerPlayer player, Affinity affinity, int increment) throws IllegalStateException {
        Map<Affinity, Integer> affinitiesImmutable = getAffinities(player);

        // Validate if affinity can be added (checks ETERNAL/DEVIANT rules)
        validateAffinityCanBeAdded(affinity, affinitiesImmutable);

        // Create mutable copy
        Map<Affinity, Integer> affinitiesMutable = new HashMap<>(affinitiesImmutable);

        // Get current completion (0 if not present)
        int currentCompletion = affinitiesMutable.getOrDefault(affinity, 0);

        // Calculate new completion
        int newCompletion = currentCompletion + increment;

        if (newCompletion <= 100) {
            player.playNotifySound(
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    0.5f
            );
        }
        else {
            player.playNotifySound(
                    playAffinitySound(player, affinity),
                    SoundSource.PLAYERS,
                    0.5f,
                    0.5f
            );
        }
        // Check if would exceed 100%
        if (newCompletion > 100) {
            throw new IllegalStateException(
                    "Already completed: " + affinity
            );
        }

        // Remove VOID affinity if present (any elemental affinity removes void)
        affinitiesMutable.remove(Affinity.VOID);

        // Set new completion value
        affinitiesMutable.put(affinity, newCompletion);

        // Save changes (triggers sync to client)
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
     * Checks if player has the required base affinity for a deviant at 100% completion
     * A deviant affinity can only be started if its base elemental is fully mastered
     *
     * @param affinities The player's affinity map
     * @param deviant    The deviant affinity to check
     * @return True if player has the base elemental affinity at 100% completion
     */
    private static boolean hasBaseAffinity(Map<Affinity, Integer> affinities, Affinity deviant) {
        return affinities.entrySet().stream()
                .anyMatch(entry ->
                        entry.getKey().getDeviant() == deviant &&  // Check if this affinity is the base
                                entry.getValue() >= 100                     // Check if it's at 100% completion
                );
    }

    /**
     * Clears all affinities and sets to VOID.
     *
     * @param player Target player
     * @throws IllegalArgumentException If player already has no affinities
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
     * Play appropriate sound based on affinity type
     */
    public static SoundEvent playAffinitySound(ServerPlayer player, Affinity affinity) {
        return switch (affinity) {
            case FIRE -> SoundEvents.FIRE_AMBIENT;
            case WATER -> SoundEvents.BUCKET_FILL;
            case ICE -> SoundEvents.GLASS_BREAK;
            case LIGHTNING -> SoundEvents.LIGHTNING_BOLT_THUNDER;
            case WIND -> SoundEvents.ELYTRA_FLYING;
            case EARTH -> SoundEvents.GRAVEL_STEP;
            case SOUND -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case GRAVITY -> SoundEvents.ANVIL_LAND;
            case TIME -> SoundEvents.ITEM_PICKUP;
            case SPACE -> SoundEvents.ENDERMAN_TELEPORT;
            case LIFE -> SoundEvents.PLAYER_BREATH;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }
}