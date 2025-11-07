package de.piggidragon.elementalrealms.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;

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
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test_stem")
    );

    /**
     * List of all custom dimensions for batch operations.
     */
    public static final List<ResourceKey<Level>> LEVELS = List.of(
            SCHOOL_DIMENSION,
            TEST_DIMENSION
    );
}
