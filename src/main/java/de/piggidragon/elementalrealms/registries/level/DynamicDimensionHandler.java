package de.piggidragon.elementalrealms.registries.level;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.entities.custom.misc.PortalEntity;
import de.piggidragon.elementalrealms.registries.worldgen.chunkgen.custom.BoundedChunkGenerator;
import de.piggidragon.elementalrealms.saveddata.GenerationCenterData;
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
 * Creates unique dimension instances per portal using Infiniverse and tracks their
 * generation centers. Each portal gets its own {@link BoundedChunkGenerator} that
 * only generates terrain inside a bounded square around the generation center.
 */
public final class DynamicDimensionHandler {

    private static final int MAX_GENERATION_ATTEMPTS = 10000;
    private static final int MAX_LAYERS = 100;

    private static int dimensionCounter = 0;
    private static GenerationCenterData generationCenters;

    private DynamicDimensionHandler() {
    }

    /**
     * Must be called during {@code ServerStarting} or {@code ServerStarted}.
     */
    public static void initialize(MinecraftServer server) {
        generationCenters = GenerationCenterData.get(server);
        ElementalRealms.LOGGER.info("DynamicDimensionHandler initialized with {} existing generation centers",
                generationCenters.getGenerationCenterCount());
    }

    public static GenerationCenterData getGenerationCenterData() {
        return generationCenters;
    }

    /**
     * Returns the unique dimension key for the portal's destination, creating it
     * via Infiniverse on first call. Returns null if creation fails.
     */
    public static ResourceKey<Level> createDimensionForPortal(
            MinecraftServer server,
            PortalEntity portal,
            ResourceKey<Level> levelResourceKey
    ) {
        ResourceKey<Level> portalTargetLevel = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);
        if (portalTargetLevel != Level.OVERWORLD) {
            return portalTargetLevel;
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(
                        ElementalRealms.MODID,
                        "realm_" + dimensionCounter
                )
        );

        ElementalRealms.LOGGER.info("Creating new dimension {} for portal {}", dimensionKey.location(), portal);

        try {
            ChunkPos generationCenter = getOrCreateGenerationCenter(server);
            generationCenters.addGenerationCenter(dimensionKey, generationCenter);

            ServerLevel newLevel = InfiniverseAPI.get().getOrCreateLevel(
                    server,
                    dimensionKey,
                    () -> createCustomLevelStem(server, levelResourceKey, dimensionKey)
            );

            if (newLevel != null) {
                portal.setData(ModAttachments.PORTAL_TARGET_LEVEL, dimensionKey);
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
     * Marks the portal's dynamically-created dimension for unregistration.
     */
    public static void removeDimensionForPortal(MinecraftServer server, PortalEntity portal) {
        ResourceKey<Level> dimensionKey = portal.getData(ModAttachments.PORTAL_TARGET_LEVEL);
        if (dimensionKey == Level.OVERWORLD) return;

        ElementalRealms.LOGGER.info("Removing dimension {} for portal {}", dimensionKey.location(), portal);
        portal.removeData(ModAttachments.PORTAL_TARGET_LEVEL);
        InfiniverseAPI.get().markDimensionForUnregistration(server, dimensionKey);
        ElementalRealms.LOGGER.info("Dimension {} marked for unregistration", dimensionKey.location());
    }

    /**
     * Returns the next free generation center on a concentric ring around origin.
     * The first portal always uses (0, 0); subsequent ones walk outward in square
     * rings whose size grows with the current layer.
     */
    public static ChunkPos getOrCreateGenerationCenter(MinecraftServer server) {
        if (generationCenters == null) {
            initialize(server);
        }

        if (dimensionCounter == 0) {
            return new ChunkPos(0, 0);
        }

        int radius = BoundedChunkGenerator.getRadius() * 2 + 1;
        int currentLayer = generationCenters.getCurrentLayer();

        while (currentLayer < MAX_LAYERS) {
            ChunkPos center = scanRing(currentLayer, radius);
            if (center != null) {
                return center;
            }
            generationCenters.incrementLayer();
            currentLayer = generationCenters.getCurrentLayer();
        }

        throw new IllegalStateException(
                "Failed to create new generation center after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    /**
     * Walks the perimeter of the square at {@code layer} and returns the first free center, or null.
     */
    private static ChunkPos scanRing(int layer, int radius) {
        for (int x = -layer; x <= layer; x++) {
            ChunkPos center = checkSide(layer, radius, x, -layer);
            if (center != null) return center;
            center = checkSide(layer, radius, x, layer);
            if (center != null) return center;
        }
        for (int z = -layer + 1; z <= layer - 1; z++) {
            ChunkPos center = checkSide(layer, radius, -layer, z);
            if (center != null) return center;
            center = checkSide(layer, radius, layer, z);
            if (center != null) return center;
        }
        return null;
    }

    private static ChunkPos checkSide(int layer, int radius, int x, int z) {
        ChunkPos center = new ChunkPos(x * radius, z * radius);
        if (generationCenters.getGenerationCenters().containsValue(center)) {
            return null;
        }
        return center;
    }

    private static LevelStem createCustomLevelStem(
            MinecraftServer server,
            ResourceKey<Level> levelResourceKey,
            ResourceKey<Level> level
    ) {
        Registry<LevelStem> levelStemRegistry = server.registryAccess()
                .registry(Registries.LEVEL_STEM)
                .orElseThrow();

        LevelStem templateStem = levelStemRegistry.get(ModLevel.getStemForLevel(levelResourceKey));

        if (!(templateStem.generator() instanceof NoiseBasedChunkGenerator templateGenerator)) {
            throw new IllegalStateException("Template generator is not NoiseBasedChunkGenerator!");
        }

        BiomeSource biomeSource = templateGenerator.getBiomeSource();
        Holder<NoiseGeneratorSettings> noiseSettings = templateGenerator.generatorSettings();

        BoundedChunkGenerator customGenerator = new BoundedChunkGenerator(biomeSource, noiseSettings, level);

        return new LevelStem(templateStem.type(), customGenerator);
    }
}
