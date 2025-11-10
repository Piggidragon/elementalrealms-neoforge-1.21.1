package de.piggidragon.elementalrealms.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Global storage for all generation centers across all dimensions.
 * Stored in the Overworld to persist across server restarts.
 */
public class GenerationCenterData extends SavedData {

    // Custom codec for ChunkPos (x, z coordinates)
    private static final Codec<ChunkPos> CHUNK_POS_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("x").forGetter(pos -> pos.x),
                    Codec.INT.fieldOf("z").forGetter(pos -> pos.z)
            ).apply(instance, ChunkPos::new)
    );

    // Codec for the entire data structure
    private static final Codec<GenerationCenterData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(
                            Level.RESOURCE_KEY_CODEC,
                            CHUNK_POS_CODEC  // Use custom ChunkPos codec
                    ).fieldOf("generationCenters").forGetter(data -> data.generationCenters),
                    Codec.INT.fieldOf("layer").forGetter(data -> data.layer)
            ).apply(instance, GenerationCenterData::new)
    );

    private static final String DATA_NAME = "generation_centers";

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
        return storage.computeIfAbsent(
                new Factory<>(
                        GenerationCenterData::new,  // Supplier for new data
                        GenerationCenterData::load, // Function to load from NBT
                        null                         // DataFixType (optional)
                ),
                DATA_NAME
        );
    }

    /**
     * Loads the data from NBT using the codec.
     *
     * @param tag            The NBT compound tag
     * @param lookupProvider Registry lookup provider
     * @return The loaded GenerationCenterData
     */
    public static GenerationCenterData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error -> {
                    // Log error if parsing fails
                    ElementalRealms.LOGGER.error("Failed to load GenerationCenterData: " + error);
                })
                .orElseGet(GenerationCenterData::new);
    }

    /**
     * Saves the data to NBT using the codec.
     *
     * @param tag            The NBT compound tag to write to
     * @param lookupProvider Registry lookup provider
     * @return The modified compound tag
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this)
                .resultOrPartial(error -> {
                    // Log error if encoding fails
                    ElementalRealms.LOGGER.error("Failed to save GenerationCenterData: " + error);
                })
                .ifPresent(encodedTag -> tag.merge((CompoundTag) encodedTag));
        return tag;
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
