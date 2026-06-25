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
 * Structure placement that restricts generation to the world spawn chunk (0, 0).
 */
public class SpawnChunkPlacement extends RandomSpreadStructurePlacement {

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

    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;

    public SpawnChunkPlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone,
            int spacing,
            int separation,
            RandomSpreadType spreadType
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);

        if (spacing <= separation) {
            throw new RuntimeException(
                    "Spacing must be greater than separation! Spacing: " + spacing + ", Separation: " + separation);
        }

        this.locateOffset = locateOffset;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = frequency;
        this.salt = salt;
        this.exclusionZone = exclusionZone;
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
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

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return x == 0 && z == 0;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacements.SPAWN_ONLY_STRUCTURE_PLACEMENT.get();
    }
}
