package de.piggidragon.elementalrealms.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.events.DimensionBorderHandler;
import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * Manager for creating and managing portal-specific dimension instances using Infiniverse API.
 * Each portal gets its own actual dimension with custom chunk generator.
 */
public class DynamicDimensionHandler {

    // Counter for unique dimension IDs (only used for new dimensions)
    private static int dimensionCounter = 0;

    /**
     * Creates a new dimension instance for a specific portal using Infiniverse API.
     * Uses persistent storage to remember portal-dimension mappings across restarts.
     *
     * @param server The server instance
     * @param portal The portal entity requesting the dimension
     * @return The ResourceKey for the dimension
     */
    public static ResourceKey<Level> createDimensionForPortal(MinecraftServer server, PortalEntity portal) {

        // Check if portal already has a dimension (from previous session or current)
        ResourceKey<Level> portalTargetLevel = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);
        if (portalTargetLevel != Level.OVERWORLD) {
            return portalTargetLevel;
        }

        // Create unique dimension key
        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(
                        ElementalRealms.MODID,
                        "realm_" + +dimensionCounter++
                )
        );

        ElementalRealms.LOGGER.info("Creating new dimension {} for portal {}", dimensionKey.location(), portal);

        try {
            // Use Infiniverse to create dimension with custom chunk generator
            ServerLevel newLevel = InfiniverseAPI.get().getOrCreateLevel(
                    server,
                    dimensionKey,
                    () -> createCustomLevelStem(server)
            );

            if (newLevel != null) {
                portal.setData(ModAttachments.PORTAL_TARGET_LEVEL, dimensionKey);

                // Setup world border
                DimensionBorderHandler.setupWorldBorder(newLevel);

                ElementalRealms.LOGGER.info("Successfully created dimension {} with custom generator",
                        dimensionKey.location());
                return dimensionKey;
            }
        } catch (Exception e) {
            ElementalRealms.LOGGER.error("Error creating dimension: ", e);
        }

        return null;
    }

    /**
     * Creates a custom LevelStem by cloning the template.
     * Since your test dimension already uses BoundedChunkGenerator,
     * we can just reuse the same configuration.
     *
     * @param server The server instance
     * @return A LevelStem with custom settings
     */
    private static LevelStem createCustomLevelStem(MinecraftServer server) {
        Registry<LevelStem> levelStemRegistry = server.registryAccess()
                .lookupOrThrow(Registries.LEVEL_STEM);
        // Get your test dimension template
        Holder.Reference<LevelStem> templateStemHolder = levelStemRegistry.get(ModLevel.TEST_DIMENSION_STEM)
                .orElseThrow(() -> new IllegalStateException("Test dimension template not found!"));

        LevelStem templateStem = templateStemHolder.value();

        // Simply return a new LevelStem with the same configuration
        // Each portal instance will get the same generator type but its own dimension
        return new LevelStem(
                templateStem.type(),
                templateStem.generator()
        );
    }

    /**
     * Removes the dimension associated with the given portal.
     * Marks the dimension for unregistration using Infiniverse API.
     *
     * @param server The server instance
     * @param portal The portal entity whose dimension to remove
     */
    public static void removeDimensionForPortal(MinecraftServer server, PortalEntity portal) {

        ResourceKey<Level> dimensionKey = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);

        ElementalRealms.LOGGER.info("Removing dimension {} for portal {}", dimensionKey.location(), portal);

        portal.removeData(ModAttachments.PORTAL_TARGET_LEVEL);

        // Use Infiniverse API to unregister the dimension
        InfiniverseAPI.get().markDimensionForUnregistration(server, dimensionKey);

        ElementalRealms.LOGGER.info("Dimension {} marked for unregistration", dimensionKey.location());
    }
}
