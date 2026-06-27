package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.ModBiomeTags;
import de.piggidragon.elementalrealms.registries.worldgen.features.ModFeatures;
import de.piggidragon.elementalrealms.registries.worldgen.features.config.PortalConfiguration;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/**
 * Datapack entries for damage types, configured/placed features, and biome modifiers.
 */
public final class ModDatapackProvider {

    public static final ResourceKey<ConfiguredFeature<?, ?>> PORTAL_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_configured"));
    public static final ResourceKey<PlacedFeature> PORTAL_PLACED_SURFACE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_placed_surface"));
    public static final ResourceKey<PlacedFeature> PORTAL_PLACED_UNDER =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_placed_under"));
    private static final ResourceKey<BiomeModifier> ADD_PORTAL_MODIFIER =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "add_portal"));
    private static final int SURFACE_RARITY = 250;
    private static final int UNDERGROUND_RARITY = 500;
    private ModDatapackProvider() {
    }

    public static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, bootstrap ->
                        bootstrap.register(PORTAL_CONFIGURED,
                                new ConfiguredFeature<>(ModFeatures.PORTAL_FEATURE.get(),
                                        new PortalConfiguration(Level.OVERWORLD))
                        )
                )
                .add(Registries.PLACED_FEATURE, bootstrap -> {
                    HolderGetter<ConfiguredFeature<?, ?>> configured = bootstrap.lookup(Registries.CONFIGURED_FEATURE);

                    bootstrap.register(PORTAL_PLACED_SURFACE,
                            new PlacedFeature(configured.getOrThrow(PORTAL_CONFIGURED), List.of(
                                    RarityFilter.onAverageOnceEvery(SURFACE_RARITY),
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
                                    BiomeFilter.biome()
                            ))
                    );

                    bootstrap.register(PORTAL_PLACED_UNDER,
                            new PlacedFeature(configured.getOrThrow(PORTAL_CONFIGURED), List.of(
                                    RarityFilter.onAverageOnceEvery(UNDERGROUND_RARITY),
                                    InSquarePlacement.spread(),
                                    HeightRangePlacement.uniform(
                                            VerticalAnchor.absolute(-60),
                                            VerticalAnchor.absolute(40)
                                    ),
                                    BiomeFilter.biome()
                            ))
                    );
                })
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
                    HolderGetter<PlacedFeature> placed = bootstrap.lookup(Registries.PLACED_FEATURE);
                    HolderGetter<Biome> biomeGetter = bootstrap.lookup(Registries.BIOME);

                    bootstrap.register(ADD_PORTAL_MODIFIER,
                            new BiomeModifiers.AddFeaturesBiomeModifier(
                                    biomeGetter.getOrThrow(ModBiomeTags.ALL_SPAWNABLE_DIMENSIONS),
                                    HolderSet.direct(List.of(
                                            placed.getOrThrow(PORTAL_PLACED_SURFACE),
                                            placed.getOrThrow(PORTAL_PLACED_UNDER)
                                    )),
                                    GenerationStep.Decoration.SURFACE_STRUCTURES
                            )
                    );
                });
    }
}
