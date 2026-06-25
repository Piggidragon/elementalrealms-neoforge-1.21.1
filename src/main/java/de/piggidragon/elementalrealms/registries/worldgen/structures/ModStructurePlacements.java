package de.piggidragon.elementalrealms.registries.worldgen.structures;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.structures.placements.SpawnChunkPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom structure placement types.
 */
public final class ModStructurePlacements {

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, ElementalRealms.MODID);
    /**
     * Placement type that restricts a structure to the world spawn chunk (0, 0).
     */
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<SpawnChunkPlacement>> SPAWN_ONLY_STRUCTURE_PLACEMENT =
            STRUCTURE_PLACEMENTS.register("spawn_only", () -> () -> SpawnChunkPlacement.CODEC);

    private ModStructurePlacements() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_PLACEMENTS.register(eventBus);
    }
}
