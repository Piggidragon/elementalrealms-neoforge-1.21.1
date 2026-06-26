package de.piggidragon.elementalrealms.magic.affinities.helper;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weighted random affinity assignment for new players.
 */
public final class AffinitiesRoll {

    /**
     * Roll probabilities for each of the four pick slots.
     */
    private static final int[] ROLL_CHANCES = {100, 25, 20, 20};

    /**
     * Chance a successful roll also grants the matching deviant affinity.
     */
    private static final int DEVIANT_CHANCE_PERCENT = 25;

    private AffinitiesRoll() {
    }

    /**
     * Generates a map of affinities to add to a player. Up to four elemental
     * affinities are chosen with decreasing probability; each carries a chance
     * of also granting its deviant variant.
     */
    public static Map<Affinity, Integer> rollAffinities(ServerPlayer player) {
        RandomSource random = player.getRandom();
        Map<Affinity, Integer> result = new HashMap<>();

        for (int chance : ROLL_CHANCES) {
            Affinity rolled = rollElementalAffinity(player, random, chance);
            if (rolled == Affinity.VOID) {
                break;
            }
            result.put(rolled, 100);

            if (chance(random, DEVIANT_CHANCE_PERCENT)) {
                result.put(rolled.getDeviant(), 100);
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
