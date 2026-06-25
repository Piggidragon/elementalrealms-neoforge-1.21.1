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
                    Codec.INT.fieldOf("layer").forGetter(data -> data.layer)
            ).apply(instance, GenerationCenterData::new)
    );

    private final Map<ResourceKey<Level>, ChunkPos> generationCenters;
    private int layer = 1;

    public GenerationCenterData() {
        this.generationCenters = new HashMap<>();
    }

    private GenerationCenterData(Map<ResourceKey<Level>, ChunkPos> generationCenters, int layer) {
        this.generationCenters = new HashMap<>(generationCenters);
        this.layer = layer;
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

    public void addGenerationCenter(ResourceKey<Level> level, ChunkPos center) {
        if (generationCenters.containsKey(level)) return;
        generationCenters.put(level, center);
        this.setDirty();
    }

    public int getGenerationCenterCount() {
        return generationCenters.size();
    }
}
