package de.piggidragon.elementalrealms.registries.worldgen.structures;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.worldgen.structures.placements.SpawnChunkPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom structure placement types.
 */
public class ModStructurePlacements {

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, ElementalRealms.MODID);

    /**
     * Placement type restricting structures to spawn chunk (0, 0).
     */
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<SpawnChunkPlacement>> SPAWN_ONLY_STRUCTURE_PLACEMENT =
            STRUCTURE_PLACEMENTS.register("spawn_only", () -> () -> SpawnChunkPlacement.CODEC);

    /**
     * Registers all placement types with mod event bus.
     *
     * @param eventBus Mod event bus
     */
    public static void register(IEventBus eventBus) {
        STRUCTURE_PLACEMENTS.register(eventBus);
    }
}
