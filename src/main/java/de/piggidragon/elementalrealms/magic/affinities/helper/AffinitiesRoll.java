package de.piggidragon.elementalrealms.magic.affinities.helper;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weighted random affinity assignment for new players. Pulls all slot + deviant
 * probabilities from {@link AffinityConfig} so they are tweakable on disk.
 */
public final class AffinitiesRoll {

    private AffinitiesRoll() {
    }

    /**
     * Generates a map of affinities to add to a player. Each of the configured slot
     * chances is rolled; on success the player receives an ELEMENTAL, plus a chance
     * of its matching DEVIANT variant as well.
     */
    public static Map<Affinity, Integer> rollAffinities(ServerPlayer player) {
        RandomSource random = player.getRandom();
        Map<Affinity, Integer> result = new HashMap<>();
        int maxCompletion = AffinityConfig.maxCompletionPercent();

        for (int chance : AffinityConfig.slotChances()) {
            Affinity rolled = rollElementalAffinity(player, random, chance);
            if (rolled == Affinity.VOID) {
                break;
            }
            result.put(rolled, maxCompletion);

            if (chance(random, AffinityConfig.deviantChancePercent())) {
                result.put(rolled.getDeviant(), maxCompletion);
            }
        }
        return result;
    }

    private static Affinity rollElementalAffinity(ServerPlayer player, RandomSource random, int probabilityPercent) {
        List<Affinity> available = Affinity.getAllElemental().stream()
                .filter(a -> !ModAffinities.hasAffinity(player, a))
                .toList();
        if (available.isEmpty()) {
            return Affinity.VOID;
        }
        return chance(random, probabilityPercent)
                ? available.get(random.nextInt(available.size()))
                : Affinity.VOID;
    }

    private static boolean chance(RandomSource random, int probabilityPercent) {
        return random.nextInt(100) < probabilityPercent;
    }
}
