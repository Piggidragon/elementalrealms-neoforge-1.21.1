package de.piggidragon.elementalrealms.magic.affinities;

/**
 * Affinity tier categories defining hierarchy and rarity.
 */
public enum AffinityType {
    /**
     * No affinity - represents void
     */
    NONE,
    /**
     * Basic tier - Fire, Water, Wind, Earth
     */
    ELEMENTAL,
    /**
     * Advanced tier requiring elemental base - Lightning, Ice, Sound, Gravity
     */
    DEVIANT,
    /**
     * Ultimate tier, mutually exclusive - Life, Space, Time
     */
    ETERNAL
}
