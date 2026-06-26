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
     * Adds an affinity at full completion (100%, hardcoded by spec). Throws if the player
     * already holds it, already holds an eternal affinity, or is missing the required
     * elemental base.
     *
     * <p>The 100% is hardcoded \u2014 the player's anchor element (login roll) and Affinity Stone
     * grants are always full strength. The configurable {@code maxCompletionPercent} only caps
     * partial rolls (shard items, stage 2/3 login partials); see {@link #addIncrementAffinity}.</p>
     */
    public static void addAffinity(ServerPlayer player, Affinity affinity) {
        Map<Affinity, Integer> current = getAffinities(player);

        if (current.getOrDefault(affinity, 0) >= 100) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }
        validateCanAdd(affinity, current);

        Map<Affinity, Integer> next = new HashMap<>(current);
        next.remove(Affinity.VOID);
        next.put(affinity, 100);

        player.playNotifySound(playAffinitySound(player, affinity), SoundSource.PLAYERS, 0.5f, 0.5f);
        player.setData(ModAttachments.AFFINITIES.get(), next);
    }

    /**
     * Increases completion by {@code increment} percent. Throws if the new total
     * would exceed {@code maxCompletionPercent} or the affinity fails tier validation.
     */
    /**
     * Increases completion by {@code increment} percent. The new total is hard-capped at 100%
     * (so shards can carry a player from partial to full affinity); throws if the new total
     * exceeds 100% or the affinity fails tier validation. Shard items + login partials both
     * use this path; the per-path maximum is the caller's responsibility (login roll is
     * already capped via {@code deviantMaxCompletionPercent} / {@code elementalMaxCompletionPercent}
     * in {@link de.piggidragon.elementalrealms.magic.affinities.helper.AffinitiesRoll}).
     */
    public static void addIncrementAffinity(ServerPlayer player, Affinity affinity, int increment) {
        Map<Affinity, Integer> current = getAffinities(player);
        validateCanAdd(affinity, current);

        Map<Affinity, Integer> next = new HashMap<>(current);
        int newCompletion = next.getOrDefault(affinity, 0) + increment;

        if (newCompletion > 100) {
            throw new IllegalStateException("Already completed: " + affinity);
        }

        SoundEvent sound = newCompletion < 100
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

    /**
     * Sets the affinity to the given completion percent, overwriting any existing value.
     * Bounds-checked to {@code 0..100}; throws if outside. Tier validation is identical to
     * {@link #addAffinity} and {@link #addIncrementAffinity}: ETERNAL requires no other
     * eternal held, DEVIANT requires its matching Elemental at 100%.
     *
     * <p>{@code affinity == Affinity.VOID} is rejected — it isn't a real affinity to assign,
     * use {@link #clearAffinities} instead.</p>
     *
     * <p>{@code completion = 0} removes the affinity entry but preserves any other affinities
     * the player holds (does not clear the whole map). {@code completion = 100} sets the
     * anchor element / stone-grant state. Partial values are useful for testing and debugging
     * the login roll distribution without having to grind shards.</p>
     *
     * <p>Special handling for {@code completion = 100}: only throws "already completed" if
     * the player already holds this affinity at exactly 100%. Otherwise it overwrites a
     * partial entry to 100% (intentional — useful for the {@code affinities set
     * <aff> 100} command without first clearing).</p>
     *
     * @throws IllegalArgumentException if affinity is VOID, or {@code completion} is
     *         outside 0..100.
     * @throws IllegalStateException if the affinity fails tier validation or is already
     *         held at the requested completion (or at 100% when completion is 100).
     */
    public static void setAffinity(ServerPlayer player, Affinity affinity, int completion) {
        if (affinity == Affinity.VOID) {
            throw new IllegalArgumentException(
                    "Cannot set VOID affinity \u2014 use '/elementalrealms affinities clear' instead."
            );
        }
        if (completion < 0 || completion > 100) {
            throw new IllegalArgumentException("Completion must be 0..100, got " + completion);
        }

        Map<Affinity, Integer> current = getAffinities(player);
        int currentValue = current.getOrDefault(affinity, 0);

        // Already-completed guard: same exact value or already maxed.
        if (completion == 100 && currentValue >= 100) {
            throw new IllegalStateException("Player already has affinity: " + affinity);
        }
        if (completion > 0 && completion < 100 && currentValue >= completion) {
            throw new IllegalStateException(
                    "Player already at " + currentValue + "% " + affinity
                            + " (cannot set to lower " + completion + "%)"
            );
        }

        // Tier validation: skip for completion=0 (removal is always allowed).
        if (completion > 0) {
            validateCanAdd(affinity, current);
        }

        Map<Affinity, Integer> next = new HashMap<>(current);
        next.remove(Affinity.VOID);
        if (completion == 0) {
            next.remove(affinity); // remove this entry, keep other affinities intact
        } else {
            next.put(affinity, completion);
        }

        player.playNotifySound(playAffinitySound(player, affinity), SoundSource.PLAYERS, 0.5f, 0.5f);
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
        // Base Elemental must be at 100% (Stage-1 hardcoded value) to validate a Deviant.
        // The configurable caps don't apply here - a Deviant always needs its full-strength
        // base, by spec (the spec defines ELEMENTAL / DEVIANT / ETERNAL tiers explicitly).
        return affinities.entrySet().stream()
                .anyMatch(entry ->
                        entry.getKey().getDeviant() == deviant && entry.getValue() >= 100);
    }
}
