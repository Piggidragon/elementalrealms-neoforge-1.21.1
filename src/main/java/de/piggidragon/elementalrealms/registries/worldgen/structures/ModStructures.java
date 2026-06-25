package de.piggidragon.elementalrealms.registries.worldgen.structures;

import com.mojang.serialization.MapCodec;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.structures.custom.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Structure types registered by the mod.
 */
public final class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ElementalRealms.MODID);
    public static final DeferredHolder<StructureType<?>, StructureType<Platform>> PLATFORM =
            STRUCTURE_TYPES.register("platform", () -> explicitStructureTypeTyping(Platform.CODEC));

    private ModStructures() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> codec) {
        return () -> codec;
    }
}
