package de.piggidragon.elementalrealms.worldgen.chunkgen.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Custom chunk generator limiting world generation to a bounded area.
 * Chunks outside the defined bounds are generated as void (air-filled).
 * Used for School dimension to create floating island effect.
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

    /**
     * Maximum chunk radius from world origin (0,0).
     * Creates 16x16 chunk play area (256x256 blocks).
     */
    private static final int RADIUS = 10;
    private RegistryAccess registryAccess;
    private RandomState customRandomState;
    private long customSeed;
    private static ChunkPos generationCenter;
    /**
     * Constructs a bounded chunk generator.
     *
     * @param biomeSource Biome distribution source
     * @param settings    Noise generation settings for terrain
     */
    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, ChunkPos generationCenter) {
        this(biomeSource, settings);
        this.generationCenter = generationCenter;
    }

    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, RegistryAccess registryAccess, long seed) {
        this(biomeSource, settings);
        this.registryAccess = registryAccess;
        this.customSeed = seed;

        initRandomState(this.registryAccess);
    }

    private void initRandomState(RegistryAccess registryAccess) {
        if (this.customRandomState == null && registryAccess != null) {
            this.customRandomState = RandomState.create(
                    generatorSettings().value(),
                    registryAccess.lookupOrThrow(Registries.NOISE),
                    this.customSeed
            );
        }
    }

    public static int getTotalSize() {
        return (RADIUS * 2 + 1) * 16;
    }

    public static ChunkPos getGenerationCenter() {
        return generationCenter;
    }

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
        ChunkPos pos = chunkAccess.getPos();

        // Check if chunk is within bounds
        if (!isWithinBounds(pos)) {
            // Generate void chunk for out-of-bounds areas
            generateVoidChunk(chunkAccess);
            return CompletableFuture.completedFuture(chunkAccess);
        }

        // Use super for chunks within bounds (normal generation)
        return super.fillFromNoise(blender, randomState, structureManager, chunkAccess);
    }

    /**
     * Applies surface rules to chunk if within bounds.
     */
    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunkAccess) {
        ChunkPos pos = chunkAccess.getPos();
        if (isWithinBounds(pos)) {
            super.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
        }
    }

    /**
     * Applies terrain carvers (caves, canyons) to chunk if within bounds.
     */
    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, RandomState randomState,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunkAccess) {
        ChunkPos pos = chunkAccess.getPos();
        if (isWithinBounds(pos)) {
            super.applyCarvers(worldGenRegion, seed, randomState, biomeManager,
                    structureManager, chunkAccess);
        }
    }

    /**
     * Gets base height for position, returning minimum height for void areas.
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
                             LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (!isWithinBounds(new ChunkPos(chunkX, chunkZ))) {
            return getMinY(); // Return minimum height for void areas
        }

        return super.getBaseHeight(x, z, heightmapType, levelHeightAccessor, randomState);
    }

    /**
     * Gets vertical block column at position, returning air column for void areas.
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor,
                                     RandomState randomState) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        if (!isWithinBounds(new ChunkPos(chunkX, chunkZ))) {
            // Return air column for void areas
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

        int minY = chunkAccess.getMinY();
        int maxY = chunkAccess.getMaxY();

        // Fill entire chunk with air
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    chunkAccess.setBlockState(new BlockPos(x, y, z), air);
                }
            }
        }

        // Initialize heightmaps for void chunks
        Heightmap.primeHeightmaps(chunkAccess, Set.of(Heightmap.Types.values()));
    }


}
