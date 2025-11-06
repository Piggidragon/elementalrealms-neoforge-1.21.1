package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

/**
 * Generates biome tags for portal spawning.
 */
public class ModBiomeTagsProvider extends BiomeTagsProvider {

    /**
     * Tag for all biomes where portals can spawn.
     */
    public static final TagKey<Biome> ALL_SPAWNABLE_DIMENSIONS =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "all_spawnable_dimensions"));

    /**
     * Creates the biome tag provider.
     *
     * @param output   Pack output handler
     * @param provider Registry lookup provider
     */
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    /**
     * Adds vanilla dimension biomes to spawn tag.
     */
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ALL_SPAWNABLE_DIMENSIONS)
                .addTag(Tags.Biomes.IS_OVERWORLD)
                .addTag(Tags.Biomes.IS_NETHER)
                .addTag(Tags.Biomes.IS_END);
    }
}
