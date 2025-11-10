package de.piggidragon.elementalrealms.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Global storage for all generation centers across all dimensions.
 * Stored in the Overworld to persist across server restarts.
 */
public class GenerationCenterData extends SavedData {

    // Codec for ResourceKey<Level> to ChunkPos mapping
    private static final Codec<GenerationCenterData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(
                            Level.RESOURCE_KEY_CODEC,
                            ChunkPos.CODEC
                    ).fieldOf("generationCenters").forGetter(data -> data.generationCenters),
                    Codec.INT.fieldOf("layer").forGetter(data -> data.layer)
            ).apply(instance, GenerationCenterData::new)
    );

    private static final SavedDataType<GenerationCenterData> TYPE = new SavedDataType<>(
            "generation_centers",
            GenerationCenterData::new,
            CODEC
    );

    // Map of dimension to its generation center
    private final Map<ResourceKey<Level>, ChunkPos> generationCenters;
    private int layer = 1;

    /**
     * Default constructor for new data.
     */
    public GenerationCenterData() {
        this.generationCenters = new HashMap<>();
    }

    /**
     * Constructor for loading from disk (called by codec).
     *
     * @param generationCenters The generation centers loaded from disk
     * @param layer             Current ring layer for placement
     */
    private GenerationCenterData(Map<ResourceKey<Level>, ChunkPos> generationCenters, int layer) {
        this.generationCenters = new HashMap<>(generationCenters);
        this.layer = layer;
    }

    /**
     * Gets or creates the global generation center data from the Overworld.
     * This ensures the data persists globally across all dimensions.
     *
     * @param server The Minecraft server instance
     * @return The global generation center data
     */
    public static GenerationCenterData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    /**
     * Gets the global list of all generation centers.
     *
     * @return Map of dimension keys to their generation centers
     */
    public Map<ResourceKey<Level>, ChunkPos> getGenerationCenters() {
        return generationCenters;
    }

    /**
     * Gets the current ring layer for generation.
     *
     * @return Current layer number
     */
    public int getCurrentLayer() {
        return layer;
    }

    /**
     * Increments the ring layer for next generation.
     */
    public void incrementLayer() {
        layer++;
        this.setDirty();
    }

    /**
     * Adds a new generation center to the global list.
     *
     * @param level  The dimension key
     * @param center The generation center position
     */
    public void addGenerationCenter(ResourceKey<Level> level, ChunkPos center) {
        if (!generationCenters.containsKey(level)) {
            generationCenters.put(level, center);
            this.setDirty();
        }
    }

    /**
     * Gets the number of generation centers.
     *
     * @return The count of generation centers
     */
    public int getGenerationCenterCount() {
        return generationCenters.size();
    }
}
