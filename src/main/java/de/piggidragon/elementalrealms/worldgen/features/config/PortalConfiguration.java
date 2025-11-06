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
 * Configuration for portal worldgen feature.
 * Specifies visual variant and target dimension for spawned portals.
 *
 * @param portalVariant   Visual appearance of portal
 * @param targetDimension Dimension this portal leads to
 */
public record PortalConfiguration(PortalVariant portalVariant,
                                  ResourceKey<Level> targetDimension) implements FeatureConfiguration {

    /**
     * Codec for JSON serialization of portal configuration.
     */
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
     * Constructor matching the Codec - converts ResourceLocation to ResourceKey.
     *
     * @param portalVariant   Visual variant for portal
     * @param targetDimension Target dimension as ResourceLocation
     */
    public PortalConfiguration(PortalVariant portalVariant,
                               ResourceLocation targetDimension) {
        this(portalVariant, ResourceKey.create(Registries.DIMENSION, targetDimension));
    }

    /**
     * Compact canonical constructor.
     */
    public PortalConfiguration {
    }
}
