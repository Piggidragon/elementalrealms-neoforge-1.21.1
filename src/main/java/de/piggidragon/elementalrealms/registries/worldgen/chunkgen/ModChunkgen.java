package de.piggidragon.elementalrealms.registries.worldgen.chunkgen;

import com.mojang.serialization.MapCodec;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.chunkgen.custom.BoundedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Chunk generator codecs for mod dimensions.
 */
public final class ModChunkgen {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, ElementalRealms.MODID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<BoundedChunkGenerator>> BOUNDED =
            CHUNK_GENERATORS.register("bounded_generator", () -> BoundedChunkGenerator.MAP_CODEC);

    private ModChunkgen() {
    }

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
    }
}
