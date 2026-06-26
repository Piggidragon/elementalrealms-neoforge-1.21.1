package de.piggidragon.elementalrealms.magic.affinities;

import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side manager for player affinities. Enforces tier rules and
 * triggers sound + client sync when affinities change.
 */
public final class ModAffinities {

    private ModAffinities() {
    }

    /**
     * Adds an affinity at full completion. Throws if the player already holds it,
     * already holds an eternal affinity, or is missing the required elemental base.
     */
    public static void addAffinity(ServerPlayer player, Affinity affinity) {
        Map<Affinity, Integer> current = getAffinities(player);
        int maxCompletion = AffinityConfig.maxCompletionPercent();

        if (current.getOrDefault(affinity, 0) >= maxCompletion) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }
        validateCanAdd(affinity, current);

        Map<Affinity, Integer> next = new HashMap<>(current);
        next.remove(Affinity.VOID);
        next.put(affinity, maxCompletion);

        player.playNotifySound(playAffinitySound(player, affinity), SoundSource.PLAYERS, 0.5f, 0.5f);
        player.setData(ModAttachments.AFFINITIES.get(), next);
    }

    /**
     * Increases completion by {@code increment} percent. Throws if the new total
     * would exceed {@code maxCompletionPercent} or the affinity fails tier validation.
     */
    public static void addIncrementAffinity(ServerPlayer player, Affinity affinity, int increment) {
        Map<Affinity, Integer> current = getAffinities(player);
        int maxCompletion = AffinityConfig.maxCompletionPercent();
        validateCanAdd(affinity, current);

        Map<Affinity, Integer> next = new HashMap<>(current);
        int newCompletion = next.getOrDefault(affinity, 0) + increment;

        if (newCompletion > maxCompletion) {
            throw new IllegalStateException("Already completed: " + affinity);
        }

        SoundEvent sound = newCompletion < maxCompletion
                ? SoundEvents.PLAYER_LEVELUP
                : playAffinitySound(player, affinity);
        player.playNotifySound(sound, SoundSource.PLAYERS, 0.5f, 0.5f);

        next.remove(Affinity.VOID);
        next.put(affinity, newCompletion);
        player.setData(ModAttachments.AFFINITIES.get(), next);
    }

    /**
     * Resets the player to a single VOID affinity at 0%. Throws if already void.
     */
    public static void clearAffinities(ServerPlayer player) {
        Map<Affinity, Integer> current = getAffinities(player);
        if (current.containsKey(Affinity.VOID)) {
            throw new IllegalStateException("Player has no affinities to clear.");
        }

        Map<Affinity, Integer> next = new HashMap<>(current);
        next.clear();
        next.put(Affinity.VOID, 0);
        player.setData(ModAttachments.AFFINITIES.get(), next);
    }

    public static Map<Affinity, Integer> getAffinities(ServerPlayer player) {
        return player.getData(ModAttachments.AFFINITIES.get());
    }

    public static boolean hasAffinity(ServerPlayer player, Affinity affinity) {
        return getAffinities(player).containsKey(affinity);
    }

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

    private static void validateCanAdd(Affinity affinity, Map<Affinity, Integer> current) {
        if (affinity.getType() == AffinityType.ETERNAL && hasEternalAffinity(current)) {
            throw new IllegalStateException("Player already has an eternal affinity");
        }
        if (affinity.getType() == AffinityType.DEVIANT && !hasBaseAffinity(current, affinity)) {
            throw new IllegalStateException(
                    "Player is missing base affinity for deviant: " + affinity.getElemental()
            );
        }
    }

    private static boolean hasEternalAffinity(Map<Affinity, Integer> affinities) {
        return affinities.keySet().stream()
                .anyMatch(a -> a.getType() == AffinityType.ETERNAL);
    }

    private static boolean hasBaseAffinity(Map<Affinity, Integer> affinities, Affinity deviant) {
        int maxCompletion = AffinityConfig.maxCompletionPercent();
        return affinities.entrySet().stream()
                .anyMatch(entry ->
                        entry.getKey().getDeviant() == deviant && entry.getValue() >= maxCompletion);
    }
}
