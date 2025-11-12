package de.piggidragon.elementalrealms.worldgen.chunkgen.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Custom chunk generator limiting world generation to a bounded area.
 * Chunks outside the defined bounds are generated as void (air-filled).
 */
public class BoundedChunkGenerator extends NoiseBasedChunkGenerator {

    /**
     * Codec for JSON serialization of this chunk generator type.
     */
    public static final MapCodec<BoundedChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings)
            ).apply(instance, BoundedChunkGenerator::new)
    );

    // Maximum chunk radius from generation center
    private static final int RADIUS = 10;
    ResourceKey<Level> level;

    /**
     * Creates a bounded chunk generator without level reference.
     *
     * @param biomeSource The biome source for biome placement
     * @param settings    Noise generation settings
     */
    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    /**
     * Creates a bounded chunk generator with level reference for generation center lookup.
     *
     * @param biomeSource The biome source for biome placement
     * @param settings    Noise generation settings
     * @param level       The dimension key for generation center lookup
     */
    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, ResourceKey<Level> level) {
        this(biomeSource, settings);
        this.level = level;
    }

    /**
     * Gets the chunk radius from generation center.
     *
     * @return Chunk radius
     */
    public static int getRadius() {
        return RADIUS;
    }

    /**
     * Returns the codec for serialization.
     */
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return MAP_CODEC;
    }

    /**
     * Generates chunk terrain using noise or creates void chunk if outside bounds.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager,
                                                        ChunkAccess chunkAccess) {
        if (level == null) {
            return super.fillFromNoise(blender, randomState, structureManager, chunkAccess);
        }

        ChunkPos pos = chunkAccess.getPos();

        if (!isWithinBounds(pos)) {
            generateVoidChunk(chunkAccess);
            return CompletableFuture.completedFuture(chunkAccess);
        }

        return super.fillFromNoise(blender, randomState, structureManager, chunkAccess);
    }

    /**
     * Applies surface rules to chunk if within bounds.
     */
    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunkAccess) {
        if (level == null) {
            super.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
            return;
        }

        ChunkPos pos = chunkAccess.getPos();
        if (isWithinBounds(pos)) {
            super.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        if (this.level == null) {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
            return;
        }

        ChunkPos pos = chunk.getPos();
        if (isWithinBounds(pos)) {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
        }
    }

    /**
     * Gets base height for position, returning minimum height for void areas.
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
                             LevelHeightAccessor levelHeightAccessor, RandomState randomState) {

        if (level == null) {
            return super.getBaseHeight(x, z, heightmapType, levelHeightAccessor, randomState);
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (!isWithinBounds(new ChunkPos(chunkX, chunkZ))) {
            return getMinY();
        }

        return super.getBaseHeight(x, z, heightmapType, levelHeightAccessor, randomState);
    }

    /**
     * Gets vertical block column at position, returning air column for void areas.
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor,
                                     RandomState randomState) {
        if (level == null) {
            return super.getBaseColumn(x, z, levelHeightAccessor, randomState);
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (!isWithinBounds(new ChunkPos(chunkX, chunkZ))) {
            BlockState[] states = new BlockState[getGenDepth()];
            Arrays.fill(states, Blocks.AIR.defaultBlockState());
            return new NoiseColumn(getMinY(), states);
        }

        return super.getBaseColumn(x, z, levelHeightAccessor, randomState);
    }

    /**
     * Adds debug screen information showing world bounds.
     */
    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        list.add("Bounds: " + (-RADIUS) + " to " + RADIUS + " chunks");
        super.addDebugScreenInfo(list, randomState, blockPos);
    }

    /**
     * Checks if chunk position is within the defined world bounds.
     *
     * @param pos Chunk position to validate
     * @return true if within bounds, false otherwise
     */
    private boolean isWithinBounds(ChunkPos pos) {

        ChunkPos generationCenter = DynamicDimensionHandler.getGenerationCenterData().getGenerationCenters().get(level);

        return pos.x >= -RADIUS + generationCenter.x && pos.x <= RADIUS + generationCenter.x &&
                pos.z >= -RADIUS + generationCenter.z && pos.z <= RADIUS + generationCenter.z;
    }

    /**
     * Fills entire chunk with air blocks to create void effect.
     *
     * @param chunkAccess Chunk to fill with air
     */
    private void generateVoidChunk(ChunkAccess chunkAccess) {
        BlockState air = Blocks.AIR.defaultBlockState();

        int minY = chunkAccess.getMinBuildHeight();
        int maxY = chunkAccess.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    chunkAccess.setBlockState(new BlockPos(x, y, z), air, true);
                }
            }
        }

        Heightmap.primeHeightmaps(chunkAccess, Set.of(Heightmap.Types.values()));
    }
}
