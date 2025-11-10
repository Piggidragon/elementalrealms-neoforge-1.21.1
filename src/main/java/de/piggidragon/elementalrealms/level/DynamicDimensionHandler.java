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

/**
 * Manager for creating and managing portal-specific dimension instances using Infiniverse API.
 * Each portal gets its own actual dimension with custom chunk generator.
 */
public class DynamicDimensionHandler {

    // Counter for unique dimension IDs
    private static int dimensionCounter = 0;
    private static GenerationCenterData generationCenters;

    /**
     * Initialize the handler with the server instance.
     * MUST be called during ServerStarting/ServerStarted event!
     *
     * @param server The Minecraft server instance
     */
    public static void initialize(MinecraftServer server) {
        generationCenters = GenerationCenterData.get(server);
        ElementalRealms.LOGGER.info("DynamicDimensionHandler initialized with {} existing generation centers",
                generationCenters.getGenerationCenterCount());
    }

    /**
     * Gets the generation center data storage.
     *
     * @return The generation center data instance
     */
    public static GenerationCenterData getGenerationCenterData() {
        return generationCenters;
    }

    /**
     * Creates a new dimension instance for a specific portal using Infiniverse API.
     *
     * @param server           The server instance
     * @param portal           The portal entity requesting the dimension
     * @param levelResourceKey The base level type to use for generation
     * @return The ResourceKey for the created dimension
     */
    public static ResourceKey<Level> createDimensionForPortal(MinecraftServer server, PortalEntity portal, ResourceKey<Level> levelResourceKey) {

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
            ChunkPos generationCenter = getOrCreateGenerationCenter(server);
            generationCenters.addGenerationCenter(dimensionKey, generationCenter);

            // Use Infiniverse to create dimension with custom chunk generator
            ServerLevel newLevel = InfiniverseAPI.get().getOrCreateLevel(
                    server,
                    dimensionKey,
                    () -> createCustomLevelStem(server, levelResourceKey, dimensionKey)
            );

            if (newLevel != null) {
                portal.setData(ModAttachments.PORTAL_TARGET_LEVEL, dimensionKey);

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
     * Each dimension gets its own generator with unique seeding.
     *
     * @param server           The server instance
     * @param levelResourceKey The level type key for template
     * @param level            The new dimension key for generation center lookup
     * @return A LevelStem with custom settings
     */
    private static LevelStem createCustomLevelStem(MinecraftServer server, ResourceKey<Level> levelResourceKey, ResourceKey<Level> level) {
        Registry<LevelStem> levelStemRegistry = server.registryAccess()
                .lookupOrThrow(Registries.LEVEL_STEM);

        Holder.Reference<LevelStem> templateStemHolder = levelStemRegistry.get(ModLevel.getStemForLevel(levelResourceKey))
                .orElseThrow(() -> new IllegalStateException("Test dimension template not found!"));

        LevelStem templateStem = templateStemHolder.value();

        if (!(templateStem.generator() instanceof NoiseBasedChunkGenerator templateGenerator)) {
            throw new IllegalStateException("Template generator is not NoiseBasedChunkGenerator!");
        }

        BiomeSource biomeSource = templateGenerator.getBiomeSource();
        Holder<NoiseGeneratorSettings> noiseSettings = templateGenerator.generatorSettings();

        BoundedChunkGenerator customGenerator = new BoundedChunkGenerator(
                biomeSource,
                noiseSettings,
                level
        );

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

            InfiniverseAPI.get().markDimensionForUnregistration(server, dimensionKey);

            ElementalRealms.LOGGER.info("Dimension {} marked for unregistration", dimensionKey.location());
        }
    }

    /**
     * Creates a new generation center in ring form around the origin.
     * Automatically saves to persistent storage.
     *
     * @param server The server instance
     * @return The newly created generation center position
     * @throws IllegalStateException if unable to create a new center
     */
    public static ChunkPos getOrCreateGenerationCenter(MinecraftServer server) throws IllegalStateException {

        if (generationCenters == null) {
            initialize(server);
        }

        ChunkPos generationCenter;

        // First center at origin
        if (dimensionCounter == 0) {
            generationCenter = new ChunkPos(0, 0);
            return generationCenter;
        }

        // Ring generation: iterate over the perimeter of the square at each layer
        int radius = BoundedChunkGenerator.getRadius() * 2 + 1;
        int maxAttempts = 10000;
        int attempts = 0;
        int currentLayer = generationCenters.getCurrentLayer();

        while (attempts < maxAttempts) {
            // Top and bottom sides (x varies, z fixed)
            for (int x = -currentLayer; x <= currentLayer; x++) {
                for (int z : new int[] { -currentLayer, currentLayer }) {
                    generationCenter = new ChunkPos(
                        x * radius,
                        z * radius
                    );
                    attempts++;
                    ElementalRealms.LOGGER.info("Tried generation center at: " + generationCenter);
                    if (!generationCenters.getGenerationCenters().containsValue(generationCenter)) {
                        return generationCenter;
                    }
                }
            }
            // Left and right sides (z varies, x fixed), skip corners to avoid duplicates
            for (int z = -currentLayer + 1; z <= currentLayer - 1; z++) {
                for (int x : new int[] { -currentLayer, currentLayer }) {
                    generationCenter = new ChunkPos(
                        x * radius,
                        z * radius
                    );
                    attempts++;
                    if (!generationCenters.getGenerationCenters().containsValue(generationCenter)) {
                        return generationCenter;
                    }
                }
            }
            generationCenters.incrementLayer();
        }

        throw new IllegalStateException("Failed to create new generation center");
    }
}
