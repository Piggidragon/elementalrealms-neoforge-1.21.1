package de.piggidragon.elementalrealms.registries.worldgen.structures.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.piggidragon.elementalrealms.registries.worldgen.structures.ModStructurePlacements;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * Structure placement restricted to world spawn chunk (0, 0).
 * Used for dimension entry platforms that must spawn at origin.
 */
public class SpawnChunkPlacement extends RandomSpreadStructurePlacement {

    /**
     * Codec for JSON serialization of placement configuration.
     */
    public static final MapCodec<SpawnChunkPlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(placement -> placement.locateOffset),
                    FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter(placement -> placement.frequencyReductionMethod),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(placement -> placement.frequency),
                    Codec.INT.fieldOf("salt").forGetter(placement -> placement.salt),
                    ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(placement -> placement.exclusionZone),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("spacing").forGetter(placement -> placement.spacing),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation").forGetter(placement -> placement.separation),
                    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(placement -> placement.spreadType)
            ).apply(instance, SpawnChunkPlacement::new));

    // Fields for codec getters
    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;

    /**
     * Creates spawn-only placement with standard RandomSpread parameters.
     *
     * @param locateOffset             Offset for /locate command
     * @param frequencyReductionMethod Frequency reduction strategy
     * @param frequency                Spawn probability (0.0-1.0)
     * @param salt                     Random seed modifier
     * @param exclusionZone            Optional overlap prevention zone
     * @param spacing                  Grid spacing in chunks
     * @param separation               Minimum distance between structures
     * @param spreadType               Distribution pattern
     * @throws RuntimeException if spacing <= separation
     */
    public SpawnChunkPlacement(Vec3i locateOffset,
                               FrequencyReductionMethod frequencyReductionMethod,
                               float frequency,
                               int salt,
                               Optional<ExclusionZone> exclusionZone,
                               int spacing,
                               int separation,
                               RandomSpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);

        this.locateOffset = locateOffset;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = frequency;
        this.salt = salt;
        this.exclusionZone = exclusionZone;
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;

        if (spacing <= separation) {
            throw new RuntimeException("Spacing must be greater than separation! Spacing: " + spacing + ", Separation: " + separation);
        }
    }

    @Override
    public Vec3i locateOffset() {
        return this.locateOffset;
    }

    @Override
    public FrequencyReductionMethod frequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    @Override
    public float frequency() {
        return this.frequency;
    }

    @Override
    public int salt() {
        return this.salt;
    }

    @Override
    public Optional<ExclusionZone> exclusionZone() {
        return this.exclusionZone;
    }

    @Override
    public int spacing() {
        return this.spacing;
    }

    @Override
    public int separation() {
        return this.separation;
    }

    @Override
    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    /**
     * Restricts structure generation to spawn chunk (0, 0) only.
     */
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return x == 0 && z == 0;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacements.SPAWN_ONLY_STRUCTURE_PLACEMENT.get();
    }
}
