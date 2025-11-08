package de.piggidragon.elementalrealms.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Global storage for all generation centers across all dimensions.
 * Stored in the Overworld to persist across server restarts.
 */
public class GenerationCenterData extends SavedData {

    // Codec for serialization/deserialization
    public static final Codec<GenerationCenterData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    // Serialize the list of ChunkPos
                    ChunkPos.CODEC.listOf().fieldOf("generationCenters").forGetter(data -> data.generationCenters)
            ).apply(instance, GenerationCenterData::new)
    );
    // SavedDataType with codec (similar to MapItemSavedData)
    private static final SavedDataType<GenerationCenterData> TYPE = new SavedDataType<>(
            "generation_centers",
            GenerationCenterData::new,
            CODEC,
            DataFixTypes.CHUNK
    );
    // Global list of all generation centers used by any dimension
    private final List<ChunkPos> generationCenters;

    /**
     * Default constructor (for new data)
     */
    public GenerationCenterData() {
        this.generationCenters = new ArrayList<>();
    }

    /**
     * Constructor for loading from disk (called by codec)
     *
     * @param generationCenters The list of generation centers loaded from disk
     */
    private GenerationCenterData(List<ChunkPos> generationCenters) {
        this.generationCenters = new ArrayList<>(generationCenters);
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
    public List<ChunkPos> getGenerationCenters() {
        return generationCenters;
    }

    /**
     * Adds a new generation center to the global list
     *
     * @param center The generation center to add
     * @return true if added, false if already exists
     */
    public boolean addGenerationCenter(ChunkPos center) {
        if (!generationCenters.contains(center)) {
            generationCenters.add(center);
            setDirty(); // Mark for saving
            return true;
        }
        return false;
    }

    /**
     * Removes a generation center from the global list
     *
     * @param center The generation center to remove
     * @return true if removed, false if not found
     */
    public boolean removeGenerationCenter(ChunkPos center) {
        if (generationCenters.remove(center)) {
            setDirty(); // Mark for saving
            return true;
        }
        return false;
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
