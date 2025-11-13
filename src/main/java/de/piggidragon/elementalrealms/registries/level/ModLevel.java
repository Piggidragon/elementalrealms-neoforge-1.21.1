package de.piggidragon.elementalrealms.registries.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
     * Development/testing dimension variant 2.
     */
    public static final ResourceKey<Level> TEST_DIMENSION2 = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test2")
    );

    public static final ResourceKey<LevelStem> TEST_DIMENSION2_STEM = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test2")
    );

    // List of all custom dimensions for batch operations
    private static final List<ResourceKey<Level>> LEVELS = List.of(
            SCHOOL_DIMENSION,
            TEST_DIMENSION,
            TEST_DIMENSION2
    );

    // Dimensions available for random portal generation
    private static final List<ResourceKey<Level>> LEVEL_RANDOM_SOURCE = List.of(
            TEST_DIMENSION,
            TEST_DIMENSION2
    );

    // Mapping of dimension keys to their level stem templates
    private static final Map<ResourceKey<Level>, ResourceKey<LevelStem>> LEVEL_STEMS = Map.of(
            TEST_DIMENSION, TEST_DIMENSION_STEM,
            TEST_DIMENSION2, TEST_DIMENSION2_STEM
    );

    /**
     * Gets all registered custom dimensions.
     *
     * @return List of all dimension keys
     */
    public static List<ResourceKey<Level>> getLevels() {
        return LEVELS;
    }

    /**
     * Gets dimensions available for random portal generation.
     *
     * @return List of randomizable dimension keys
     */
    public static List<ResourceKey<Level>> getLevelsRandomSource() {
        return LEVEL_RANDOM_SOURCE;
    }

    /**
     * Selects a random dimension from available sources.
     *
     * @return Random dimension key
     */
    public static ResourceKey<Level> getRandomLevel() {
        RandomSource randomSource = RandomSource.create();
        int index = randomSource.nextInt(LEVEL_RANDOM_SOURCE.size());
        return LEVEL_RANDOM_SOURCE.get(index);
    }

    /**
     * Gets the level stem template for a dimension.
     *
     * @param level The dimension key
     * @return The corresponding level stem key
     */
    public static ResourceKey<LevelStem> getStemForLevel(ResourceKey<Level> level) {
        return LEVEL_STEMS.get(level);
    }
}
