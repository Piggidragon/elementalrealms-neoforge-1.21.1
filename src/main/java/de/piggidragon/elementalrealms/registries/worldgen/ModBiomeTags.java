package de.piggidragon.elementalrealms.registries.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Custom biome tags for the mod.
 */
public class ModBiomeTags {

    /**
     * Tag for all biomes where portals can spawn.
     */
    public static final TagKey<Biome> ALL_SPAWNABLE_DIMENSIONS =
            TagKey.create(Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath("elementalrealms", "all_spawnable_dimensions"));
}
