package de.piggidragon.elementalrealms.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.piggidragon.elementalrealms.ElementalRealms;
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
 * Persistent map of {@code ResourceKey<Level> -> ChunkPos} storing the chunk where each
 * dynamically-generated dimension starts its worldgen. Stored against the Overworld so
 * the data survives restarts.
 */
public class GenerationCenterData extends SavedData {

    private static final String DATA_NAME = "generation_centers";

    private static final Codec<ChunkPos> CHUNK_POS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("x").forGetter(pos -> pos.x),
                    Codec.INT.fieldOf("z").forGetter(pos -> pos.z)
            ).apply(instance, ChunkPos::new)
    );

    private static final Codec<GenerationCenterData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Level.RESOURCE_KEY_CODEC, CHUNK_POS_CODEC)
                            .fieldOf("generationCenters").forGetter(data -> data.generationCenters),
                    Codec.INT.fieldOf("layer").forGetter(data -> data.layer),
                    // Highest dimensionCounter value ever assigned. Persisted so server restarts
                    // keep allocating realm_<n> with a unique n (issue #21).
                    // optionalFieldOf with default 0 keeps backward-compat with saves written
                    // before this field existed.
                    Codec.INT.optionalFieldOf("currentMaxIndex", 0)
                            .forGetter(data -> data.currentMaxIndex)
            ).apply(instance, GenerationCenterData::new)
    );

    private final Map<ResourceKey<Level>, ChunkPos> generationCenters;
    private int layer = 1;
    private int currentMaxIndex = 0;

    public GenerationCenterData() {
        this.generationCenters = new HashMap<>();
    }

    private GenerationCenterData(Map<ResourceKey<Level>, ChunkPos> generationCenters, int layer) {
        this.generationCenters = new HashMap<>(generationCenters);
        this.layer = layer;
    }

    private GenerationCenterData(
            Map<ResourceKey<Level>, ChunkPos> generationCenters,
            int layer,
            int currentMaxIndex
    ) {
        this.generationCenters = new HashMap<>(generationCenters);
        this.layer = layer;
        this.currentMaxIndex = currentMaxIndex;
    }

    public static GenerationCenterData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(GenerationCenterData::new, GenerationCenterData::load, null),
                DATA_NAME
        );
    }

    public static GenerationCenterData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider lookupProvider) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error ->
                        ElementalRealms.LOGGER.error("Failed to load GenerationCenterData: {}", error))
                .orElseGet(GenerationCenterData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider lookupProvider) {
        CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this)
                .resultOrPartial(error ->
                        ElementalRealms.LOGGER.error("Failed to save GenerationCenterData: {}", error))
                .ifPresent(encoded -> tag.merge((CompoundTag) encoded));
        return tag;
    }

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
     * Highest {@code realm_<n>} index ever assigned by {@code DynamicDimensionHandler}.
     * Persisted across restarts so the next portal's realm name doesn't collide.
     */
    public int getCurrentMaxIndex() {
        return currentMaxIndex;
    }

    /**
     * Bump {@link #currentMaxIndex} to {@code newValue} if higher, and mark the data dirty.
     * Callers pass the index they just assigned — we never overwrite a higher value with
     * a lower one (defensive against out-of-order callers).
     */
    public void recordAssignedIndex(int newValue) {
        if (newValue > currentMaxIndex) {
            currentMaxIndex = newValue;
            this.setDirty();
        }
    }

    public void addGenerationCenter(ResourceKey<Level> level, ChunkPos center) {
        if (generationCenters.containsKey(level)) return;
        generationCenters.put(level, center);
        this.setDirty();
    }

    public int getGenerationCenterCount() {
        return generationCenters.size();
    }
}
