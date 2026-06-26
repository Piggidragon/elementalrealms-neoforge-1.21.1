package de.piggidragon.elementalrealms.registries.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;
import java.util.Map;

/**
 * Resource keys for custom dimensions. Must match dimension JSON files in the data pack.
 *
 * <p>Currently only the School dimension and a single test dimension are registered.
 * The 11 affinity-specific pocket templates will be added in Phase 4 (pocket dimensions).</p>
 */
public final class ModLevel {

    public static final ResourceKey<Level> SCHOOL_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "school")
    );

    public static final ResourceKey<Level> TEST_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test")
    );
    public static final ResourceKey<LevelStem> TEST_DIMENSION_STEM = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test")
    );

    private static final List<ResourceKey<Level>> LEVELS = List.of(
            SCHOOL_DIMENSION,
            TEST_DIMENSION
    );

    private static final Map<ResourceKey<Level>, ResourceKey<LevelStem>> LEVEL_STEMS = Map.of(
            TEST_DIMENSION, TEST_DIMENSION_STEM
    );

    private ModLevel() {
    }

    public static List<ResourceKey<Level>> getLevels() {
        return LEVELS;
    }

    public static ResourceKey<LevelStem> getStemForLevel(ResourceKey<Level> level) {
        return LEVEL_STEMS.get(level);
    }
}
