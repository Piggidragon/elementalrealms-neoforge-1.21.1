package de.piggidragon.elementalrealms.worldgen.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.piggidragon.elementalrealms.entities.variants.PortalVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for portal spawn feature.
 */
public record PortalConfiguration(PortalVariant portalVariant, ResourceKey<Level> targetDimension) implements FeatureConfiguration {

    /** Codec for serializing this configuration to/from JSON */
    public static final Codec<PortalConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PortalVariant.CODEC
                            .fieldOf("portal_variant")
                            .forGetter(config -> config.portalVariant),

                    ResourceLocation.CODEC
                            .fieldOf("target_dimension")
                            .forGetter(config -> config.targetDimension.location())

            ).apply(instance, PortalConfiguration::new)
    );

    /**
     * Constructor matching the Codec - targetDimension supplied as ResourceLocation.
     */
    public PortalConfiguration(PortalVariant portalVariant,
                               ResourceLocation targetDimension) {
        this(portalVariant, ResourceKey.create(Registries.DIMENSION, targetDimension));
    }

    /** Compact canonical constructor retained (no extra docs). */
    public PortalConfiguration {
    }
}
