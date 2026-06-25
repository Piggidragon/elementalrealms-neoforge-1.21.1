package de.piggidragon.elementalrealms.magic.affinities;

import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.List;

/**
 * Magical affinity types a player can hold.
 *
 * <p>Tier hierarchy:</p>
 * <ul>
 *   <li>ELEMENTAL - basic affinities (Fire, Water, Wind, Earth)</li>
 *   <li>DEVIANT   - advanced, requires an ELEMENTAL base at 100% (Lightning, Ice, Sound, Gravity)</li>
 *   <li>ETERNAL   - ultimate, mutually exclusive (Life, Space, Time)</li>
 * </ul>
 *
 * <p>Deviant pairings:</p>
 * <ul>
 *   <li>FIRE -> LIGHTNING</li>
 *   <li>WATER -> ICE</li>
 *   <li>WIND -> SOUND</li>
 *   <li>EARTH -> GRAVITY</li>
 * </ul>
 */
public enum Affinity {
    VOID(AffinityType.NONE),

    FIRE(AffinityType.ELEMENTAL),
    WATER(AffinityType.ELEMENTAL),
    WIND(AffinityType.ELEMENTAL),
    EARTH(AffinityType.ELEMENTAL),

    LIGHTNING(AffinityType.DEVIANT),
    ICE(AffinityType.DEVIANT),
    SOUND(AffinityType.DEVIANT),
    GRAVITY(AffinityType.DEVIANT),

    LIFE(AffinityType.ETERNAL),
    SPACE(AffinityType.ETERNAL),
    TIME(AffinityType.ETERNAL);

    public static final Codec<Affinity> CODEC =
            Codec.STRING.xmap(Affinity::valueOf, Affinity::name);

    private final AffinityType type;

    Affinity(AffinityType type) {
        this.type = type;
    }

    public static List<Affinity> getAllElemental() {
        return Arrays.stream(values())
                .filter(a -> a.getType() == AffinityType.ELEMENTAL)
                .toList();
    }

    public AffinityType getType() {
        return type;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public Affinity getDeviant() {
        if (getType() != AffinityType.ELEMENTAL) return VOID;
        return switch (this) {
            case FIRE -> LIGHTNING;
            case WATER -> ICE;
            case WIND -> SOUND;
            case EARTH -> GRAVITY;
            default -> VOID;
        };
    }

    public Affinity getElemental() {
        if (getType() != AffinityType.DEVIANT) return VOID;
        return switch (this) {
            case LIGHTNING -> FIRE;
            case ICE -> WATER;
            case SOUND -> WIND;
            case GRAVITY -> EARTH;
            default -> VOID;
        };
    }
}
