package de.piggidragon.elementalrealms.registries.worldgen.chunkgen.custom;

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
 * Chunk generator that produces noise terrain only inside a square radius around
 * the dimension's generation center. Chunks outside the bounds are filled with air
 * (void). Without a {@code level} key set, behaves identically to the base generator.
 */
public class BoundedChunkGenerator extends NoiseBasedChunkGenerator {

    /**
     * Max chunk radius from the generation center.
     */
    public static final int RADIUS = 10;

    public static final MapCodec<BoundedChunkGenerator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings)
            ).apply(instance, BoundedChunkGenerator::new)
    );

    private ResourceKey<Level> level;

    public BoundedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    public BoundedChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            ResourceKey<Level> level
    ) {
        this(biomeSource, settings);
        this.level = level;
    }

    public static int getRadius() {
        return RADIUS;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return MAP_CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        if (level == null) {
            return super.fillFromNoise(blender, randomState, structureManager, chunk);
        }
        if (!isWithinBounds(chunk.getPos())) {
            generateVoidChunk(chunk);
            return CompletableFuture.completedFuture(chunk);
        }
        return super.fillFromNoise(blender, randomState, structureManager, chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        if (level == null) {
            super.buildSurface(region, structureManager, randomState, chunk);
            return;
        }
        if (isWithinBounds(chunk.getPos())) {
            super.buildSurface(region, structureManager, randomState, chunk);
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step) {
        if (level == null) {
            super.applyCarvers(region, seed, random, biomeManager, structureManager, chunk, step);
            return;
        }
        if (isWithinBounds(chunk.getPos())) {
            super.applyCarvers(region, seed, random, biomeManager, structureManager, chunk, step);
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor accessor, RandomState randomState) {
        if (level == null) return super.getBaseHeight(x, z, type, accessor, randomState);
        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        return isWithinBounds(chunkPos) ? super.getBaseHeight(x, z, type, accessor, randomState) : getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor accessor, RandomState randomState) {
        if (level == null) return super.getBaseColumn(x, z, accessor, randomState);
        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        if (!isWithinBounds(chunkPos)) {
            BlockState[] states = new BlockState[getGenDepth()];
            Arrays.fill(states, Blocks.AIR.defaultBlockState());
            return new NoiseColumn(getMinY(), states);
        }
        return super.getBaseColumn(x, z, accessor, randomState);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Bounds: " + (-RADIUS) + " to " + RADIUS + " chunks");
        super.addDebugScreenInfo(info, randomState, pos);
    }

    private boolean isWithinBounds(ChunkPos pos) {
        ChunkPos center = DynamicDimensionHandler.getGenerationCenterData()
                .getGenerationCenters()
                .get(level);
        return pos.x >= -RADIUS + center.x
                && pos.x <= RADIUS + center.x
                && pos.z >= -RADIUS + center.z
                && pos.z <= RADIUS + center.z;
    }

    private void generateVoidChunk(ChunkAccess chunk) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), air, true);
                }
            }
        }
        Heightmap.primeHeightmaps(chunk, Set.of(Heightmap.Types.values()));
    }
}
