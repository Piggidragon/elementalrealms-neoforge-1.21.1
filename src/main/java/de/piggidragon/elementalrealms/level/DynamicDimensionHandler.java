package de.piggidragon.elementalrealms.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.events.DimensionBorderHandler;
import de.piggidragon.elementalrealms.saveddata.GenerationCenterData;
import de.piggidragon.elementalrealms.worldgen.chunkgen.custom.BoundedChunkGenerator;
import net.commoble.infiniverse.api.InfiniverseAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Collection;

/**
 * Manager for creating and managing portal-specific dimension instances using Infiniverse API.
 * Each portal gets its own actual dimension with custom chunk generator.
 */
public class DynamicDimensionHandler {

    // Counter for unique dimension IDs (only used for new dimensions)
    private static int dimensionCounter = 0;
    private static int layer = 1;
    private static GenerationCenterData generationCenters;

    /**
     * Initialize the handler with the server instance.
     * MUST be called during ServerStarting/ServerStarted event!
     *
     * @param server The Minecraft server instance
     */
    public static void initialize(MinecraftServer server) {
        // Store server reference
        generationCenters = GenerationCenterData.get(server);
        ElementalRealms.LOGGER.info("DynamicDimensionHandler initialized with {} existing generation centers",
                generationCenters.getGenerationCenterCount());
    }

    public static Collection<ChunkPos> getUsedGenerationCenters() {
        return generationCenters.getGenerationCenters().values();
    }

    /**
     * Creates a new dimension instance for a specific portal using Infiniverse API.
     *
     * @param server The server instance
     * @param portal The portal entity requesting the dimension
     * @return The ResourceKey for the dimension
     */
    public static ResourceKey<Level> createDimensionForPortal(MinecraftServer server, PortalEntity portal) {

        if (generationCenters == null) {
            initialize(server);
        }

        // Check if portal already has a dimension
        ResourceKey<Level> portalTargetLevel = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);
        if (portalTargetLevel != Level.OVERWORLD) {
            return portalTargetLevel;
        }

        // Create unique dimension key with variant name
        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(
                        ElementalRealms.MODID,
                        "realm_" + portal.getVariant().getName() + "_" + dimensionCounter
                )
        );

        ElementalRealms.LOGGER.info("Creating new dimension {} for portal {}", dimensionKey.location(), portal);

        try {
            ChunkPos generationCenter = createNewGenerationCenter();

            // Use Infiniverse to create dimension with custom chunk generator
            ServerLevel newLevel = InfiniverseAPI.get().getOrCreateLevel(
                    server,
                    dimensionKey,
                    () -> createCustomLevelStem(server, dimensionKey, generationCenter)
            );

            generationCenters.addGenerationCenter(dimensionKey, generationCenter);
            portal.setSpawnChunk(generationCenter);

            if (newLevel != null) {
                portal.setData(ModAttachments.PORTAL_TARGET_LEVEL, dimensionKey);

                // Setup world border
                DimensionBorderHandler.setupWorldBorder(newLevel, generationCenter);

                ElementalRealms.LOGGER.info("Successfully created dimension {} with custom generator",
                        dimensionKey.location());
                dimensionCounter++;
                return dimensionKey;
            }
        } catch (Exception e) {
            ElementalRealms.LOGGER.error("Error creating dimension: ", e);
        }

        return null;
    }

    /**
     * Creates a custom LevelStem with a NEW chunk generator instance.
     * Each dimension gets its own generator - the unique dimension key
     * provides a unique seed automatically through Minecraft's internal seeding.
     *
     * @param server The server instance
     * @param dimensionKey The dimension key (provides unique seed automatically)
     * @param generationCenter The center position for chunk generation
     * @return A LevelStem with custom settings
     */
    private static LevelStem createCustomLevelStem(MinecraftServer server, ResourceKey<Level> dimensionKey, ChunkPos generationCenter) {
        Registry<LevelStem> levelStemRegistry = server.registryAccess()
                .lookupOrThrow(Registries.LEVEL_STEM);

        // Get your test dimension template
        Holder.Reference<LevelStem> templateStemHolder = levelStemRegistry.get(ModLevel.TEST_DIMENSION_STEM)
                .orElseThrow(() -> new IllegalStateException("Test dimension template not found!"));

        LevelStem templateStem = templateStemHolder.value();

        // Get generator from template
        if (!(templateStem.generator() instanceof NoiseBasedChunkGenerator templateGenerator)) {
            throw new IllegalStateException("Template generator is not NoiseBasedChunkGenerator!");
        }

        // Get components from template
        BiomeSource biomeSource = templateGenerator.getBiomeSource();
        Holder<NoiseGeneratorSettings> noiseSettings = templateGenerator.generatorSettings();

        BoundedChunkGenerator customGenerator = new BoundedChunkGenerator(
                biomeSource,
                noiseSettings,
                generationCenter
        );

        ElementalRealms.LOGGER.info("Created new BoundedChunkGenerator for dimension {}",
                dimensionKey.location());

        // Return new LevelStem with the new generator instance
        return new LevelStem(
                templateStem.type(),
                customGenerator
        );
    }

    /**
     * Removes the dimension associated with the given portal.
     *
     * @param server The server instance
     * @param portal The portal entity whose dimension to remove
     */
    public static void removeDimensionForPortal(MinecraftServer server, PortalEntity portal) {

        ResourceKey<Level> dimensionKey = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);

        if (dimensionKey != Level.OVERWORLD) {
            ElementalRealms.LOGGER.info("Removing dimension {} for portal {}", dimensionKey.location(), portal);

            portal.removeData(ModAttachments.PORTAL_TARGET_LEVEL);

            // Use Infiniverse API to unregister the dimension
            InfiniverseAPI.get().markDimensionForUnregistration(server, dimensionKey);


            ElementalRealms.LOGGER.info("Dimension {} marked for unregistration", dimensionKey.location());
        }
    }

    /**
     * Creates a new generation center in ring form around the origin.
     * Automatically saves to persistent storage.
     *
     * @return The newly created generation center position
     * @throws IllegalStateException if unable to create a new center
     */
    public static ChunkPos createNewGenerationCenter() throws IllegalStateException {

        if (generationCenters == null) {
            throw new IllegalStateException("DynamicDimensionHandler not initialized! Call initialize() first.");
        }

        ChunkPos generationCenter;

        // First center at origin
        if (dimensionCounter == 0) {
            generationCenter = new ChunkPos(0, 0);
            return generationCenter;
        }

        // Ring generation logic
        for (int x = -layer; x <= layer; x++) {
            for (int z = -layer; z <= layer; z++) {
                if (x == layer && z == layer) {
                    layer++;
                }

                generationCenter = new ChunkPos(
                        x * (BoundedChunkGenerator.getRadius() * 2 + 1),
                        z * (BoundedChunkGenerator.getRadius() * 2 + 1)
                );

                if (!generationCenters.getGenerationCenters().containsValue(generationCenter)) {
                    return generationCenter;
                }
            }
        }

        throw new IllegalStateException("Failed to create new generation center");
    }
}
