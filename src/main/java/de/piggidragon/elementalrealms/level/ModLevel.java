package de.piggidragon.elementalrealms.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;
import java.util.Map;

/**
 * Resource keys for custom dimensions.
 * Must match dimension JSON files in data pack.
 */
public class ModLevel {

    /**
     * Educational hub dimension for learning magic.
     */
    public static final ResourceKey<Level> SCHOOL_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "school")
    );

    /**
     * Development/testing dimension.
     */
    public static final ResourceKey<Level> TEST_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test")
    );

    public static final ResourceKey<LevelStem> TEST_DIMENSION_STEM = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test")
    );

    /**
     * Development/testing dimension.
     */
    public static final ResourceKey<Level> TEST_DIMENSION2 = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test2")
    );

    public static final ResourceKey<LevelStem> TEST_DIMENSION2_STEM = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test2")
    );

    /**
     * List of all custom dimensions for batch operations.
     */
    private static final List<ResourceKey<Level>> LEVELS = List.of(
            SCHOOL_DIMENSION,
            TEST_DIMENSION,
            TEST_DIMENSION2
    );
    private static final List<ResourceKey<Level>> LEVEL_RANDOM_SOURCE = List.of(
            TEST_DIMENSION,
            TEST_DIMENSION2
    );
    private static final Map<ResourceKey<Level>, ResourceKey<LevelStem>> LEVEL_STEMS = Map.of(
            TEST_DIMENSION,
            TEST_DIMENSION_STEM,

            TEST_DIMENSION2,
            TEST_DIMENSION2_STEM
    );

    public static List<ResourceKey<Level>> getLevels() {
        return LEVELS;
    }

    public static List<ResourceKey<Level>> getLevelsRandomSource() {
        return LEVEL_RANDOM_SOURCE;
    }

    public static ResourceKey<Level> getRandomLevel() {
        return LEVEL_RANDOM_SOURCE.get((int) (Math.random() * LEVELS.size()));
    }

    public static ResourceKey<LevelStem> getStemForLevel(ResourceKey<Level> level) {
        return LEVEL_STEMS.get(level);
    }
}
