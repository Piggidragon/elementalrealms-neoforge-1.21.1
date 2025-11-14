package de.piggidragon.elementalrealms.registries.worldgen.features;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.features.config.PortalConfiguration;
import de.piggidragon.elementalrealms.registries.worldgen.features.custom.PortalSpawnFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom worldgen features.
 * Defines placement behaviors for naturally occurring structures.
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ElementalRealms.MODID);

    /**
     * Portal spawn feature for naturally generating portals in world.
     */
    public static final Supplier<Feature<PortalConfiguration>> PORTAL_FEATURE =
            FEATURES.register("portal_feature", () ->
                    new PortalSpawnFeature(PortalConfiguration.CODEC));


    /**
     * Registers all features with the mod event bus.
     *
     * @param bus Mod event bus for registration
     */
    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
