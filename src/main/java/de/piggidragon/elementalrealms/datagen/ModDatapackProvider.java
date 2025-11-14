package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.registries.worldgen.ModBiomeTags;
import de.piggidragon.elementalrealms.registries.worldgen.features.ModFeatures;
import de.piggidragon.elementalrealms.registries.worldgen.features.config.PortalConfiguration;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/**
 * Provides configured and placed features for portal worldgen.
 */
public class ModDatapackProvider {

    public static final ResourceKey<DamageType> LASER =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "laser"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PORTAL_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_configured"));

    public static final ResourceKey<PlacedFeature> PORTAL_PLACED_SURFACE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_placed_surface"));

    public static final ResourceKey<PlacedFeature> PORTAL_PLACED_UNDER =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "portal_placed_under"));

    /**
     * Creates registry entries for portal features and biome modifiers.
     *
     * @return Registry builder with all feature configurations
     */
    public static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder()

                // Damage Types
                .add(Registries.DAMAGE_TYPE, bootstrap -> {
                    bootstrap.register(LASER,
                            new DamageType(
                                    "laser",
                                    0.0f
                            ));
                })

                // Features
                .add(Registries.CONFIGURED_FEATURE, bootstrap -> {
                    // Configure basic portal feature
                    bootstrap.register(PORTAL_CONFIGURED,
                            new ConfiguredFeature<>(
                                    ModFeatures.PORTAL_FEATURE.get(),
                                    new PortalConfiguration(Level.OVERWORLD)
                            )
                    );
                })

                .add(Registries.PLACED_FEATURE, bootstrap -> {
                    HolderGetter<ConfiguredFeature<?, ?>> configured =
                            bootstrap.lookup(Registries.CONFIGURED_FEATURE);

                    // Surface portal placement (rare)
                    bootstrap.register(PORTAL_PLACED_SURFACE,
                            new PlacedFeature(
                                    configured.getOrThrow(PORTAL_CONFIGURED),
                                    List.of(
                                            RarityFilter.onAverageOnceEvery(250),
                                            InSquarePlacement.spread(),
                                            HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
                                            BiomeFilter.biome()
                                    )
                            )
                    );

                    // Underground portal placement (very rare)
                    bootstrap.register(PORTAL_PLACED_UNDER,
                            new PlacedFeature(
                                    configured.getOrThrow(PORTAL_CONFIGURED),
                                    List.of(
                                            RarityFilter.onAverageOnceEvery(500),
                                            InSquarePlacement.spread(),
                                            HeightRangePlacement.uniform(
                                                    VerticalAnchor.absolute(-60),
                                                    VerticalAnchor.absolute(40)
                                            ),
                                            BiomeFilter.biome()
                                    )
                            )
                    );
                })
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
                    HolderGetter<PlacedFeature> placed = bootstrap.lookup(Registries.PLACED_FEATURE);
                    HolderGetter<Biome> biomeGetter = bootstrap.lookup(Registries.BIOME);

                    // Add portals to all spawnable dimensions
                    bootstrap.register(
                            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "add_portal")),
                            new BiomeModifiers.AddFeaturesBiomeModifier(
                                    biomeGetter.getOrThrow(ModBiomeTags.ALL_SPAWNABLE_DIMENSIONS),
                                    HolderSet.direct(
                                            List.of(
                                                    placed.getOrThrow(PORTAL_PLACED_SURFACE),
                                                    placed.getOrThrow(PORTAL_PLACED_UNDER)
                                            )),
                                    GenerationStep.Decoration.SURFACE_STRUCTURES
                            )
                    );
                });
    }
}
