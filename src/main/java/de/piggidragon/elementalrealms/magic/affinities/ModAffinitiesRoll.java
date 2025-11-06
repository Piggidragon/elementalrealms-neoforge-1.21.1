package de.piggidragon.elementalrealms.magic.affinities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Randomized affinity assignment for new players using weighted probability.
 */
public class ModAffinitiesRoll {

    /**
     * Checks if random event occurs based on probability percentage.
     *
     * @param random             Random source
     * @param probabilityPercent Chance 0-100
     * @return true if event occurs
     */
    private static boolean chance(RandomSource random, int probabilityPercent) {
        if (probabilityPercent <= 0) return false;
        if (probabilityPercent >= 100) return true;
        return random.nextInt(100) < probabilityPercent;
    }

    /**
     * Selects random elemental affinity player doesn't have.
     *
     * @param player             Target player
     * @param probabilityPercent Chance to select
     * @return Selected affinity or VOID if none available
     */
    private static Affinity randomElementalAffinity(ServerPlayer player, int probabilityPercent) {
        RandomSource random = player.getRandom();

        List<Affinity> available = Affinity.getAllElemental().stream()
                .filter(a -> !ModAffinities.hasAffinity(player, a))
                .toList();
        if (available.isEmpty()) return Affinity.VOID;

        if (chance(random, probabilityPercent)) {
            return available.get(random.nextInt(available.size()));
        } else {
            return Affinity.VOID;
        }
    }

    /**
     * Generates random affinities for new player.
     * Rolls 4 times with decreasing probability: 100%, 25%, 20%, 20%.
     * Each roll has 25% chance to also grant deviant variant.
     *
     * @param player Target player
     * @return List of affinities to add
     */
    public static List<Affinity> rollAffinities(ServerPlayer player) {
        RandomSource random = player.getRandom();
        List<Affinity> affinitiesToAdd = new ArrayList<>();

        // Roll with decreasing probability
        for (int x : new int[]{100, 25, 20, 20}) {
            Affinity newAffinity = randomElementalAffinity(player, x);

            if (newAffinity != Affinity.VOID) {
                affinitiesToAdd.add(newAffinity);

                // 25% chance for deviant variant
                if (chance(random, 25)) {
                    Affinity deviant = newAffinity.getDeviant();
                    affinitiesToAdd.add(deviant);
                }
            } else {
                break;
            }
        }
        return affinitiesToAdd;
    }
}
