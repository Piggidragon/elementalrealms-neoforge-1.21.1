package de.piggidragon.elementalrealms.worldgen.chunkgen;

import com.mojang.serialization.MapCodec;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.worldgen.chunkgen.custom.BoundedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom chunk generator types.
 * Registers bounded chunk generator for floating island dimensions.
 */
public class ModChunkgen {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, ElementalRealms.MODID);

    /**
     * Bounded chunk generator codec for limited-size dimensions.
     * Used in School dimension to create floating island effect.
     */
    public static final Supplier<MapCodec<BoundedChunkGenerator>> BOUNDED_GENERATOR =
            CHUNK_GENERATORS.register("bounded_generator", () -> BoundedChunkGenerator.MAP_CODEC);

    /**
     * Registers all chunk generators with the mod event bus.
     *
     * @param modEventBus Mod event bus for registration
     */
    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
    }
}
