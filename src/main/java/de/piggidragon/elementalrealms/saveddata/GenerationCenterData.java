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
                    // Codec for Map<ResourceKey<Level>, ChunkPos>
                    Codec.unboundedMap(
                            Level.RESOURCE_KEY_CODEC,  // Key codec: ResourceKey<Level>
                            ChunkPos.CODEC              // Value codec: ChunkPos
                    ).fieldOf("generationCenters").forGetter(data -> data.generationCenters),
                    Codec.INT.fieldOf("layer").forGetter(data -> data.layer)
            ).apply(instance, GenerationCenterData::new)
    );
    // SavedDataType with codec (similar to MapItemSavedData)
    private static final SavedDataType<GenerationCenterData> TYPE = new SavedDataType<>(
            "generation_centers",
            GenerationCenterData::new,
            CODEC
    );
    // Map of dimension to its generation center
    private final Map<ResourceKey<Level>, ChunkPos> generationCenters;
    private int layer = 1;

    /**
     * Default constructor (for new data)
     */
    public GenerationCenterData() {
        this.generationCenters = new HashMap<>();
    }

    /**
     * Constructor for loading from disk (called by codec)
     *
     * @param generationCenters The list of generation centers loaded from disk
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
        // Always attach to Overworld for global data storage
        DimensionDataStorage storage = server.overworld().getDataStorage();

        // Use computeIfAbsent with the SavedDataType
        return storage.computeIfAbsent(TYPE);
    }

    /**
     * Gets the global list of all generation centers
     *
     * @return The list of generation centers
     */
    public Map<ResourceKey<Level>, ChunkPos> getGenerationCenters() {
        return generationCenters;
    }

    public int getCurrentLayer() {
        return layer;
    }

    public void incrementLayer() {
        layer++;
        this.setDirty();
    }

    /**
     * Adds a new generation center to the global list
     *
     * @param center The generation center to add
     */
    public void addGenerationCenter(ResourceKey<Level> level, ChunkPos center) {
        if (!generationCenters.containsKey(level)) {
            generationCenters.put(level, center);
            this.setDirty();
        }
    }

    /**
     * Gets the number of generation centers
     *
     * @return The count of generation centers
     */
    public int getGenerationCenterCount() {
        return generationCenters.size();
    }
}
