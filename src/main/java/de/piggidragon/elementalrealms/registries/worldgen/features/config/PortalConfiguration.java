package de.piggidragon.elementalrealms.registries.worldgen.features.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Configuration for portal worldgen: the dimension the placed portal links to.
 */
public record PortalConfiguration(ResourceKey<Level> targetDimension) implements FeatureConfiguration {

    public static final Codec<PortalConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("target_dimension")
                            .forGetter(config -> config.targetDimension.location())
            ).apply(instance, PortalConfiguration::new)
    );

    public PortalConfiguration(ResourceLocation targetDimension) {
        this(ResourceKey.create(Registries.DIMENSION, targetDimension));
    }
}
