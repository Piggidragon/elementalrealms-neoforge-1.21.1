package de.piggidragon.elementalrealms.registries.sounds;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom sound events in the mod.
 */
public class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            Registries.SOUND_EVENT,
            ElementalRealms.MODID
    );

    /**
     * Sound effect for dragon laser beam attack.
     * Variable range sound that attenuates with distance.
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_BEAM = SOUND_EVENTS.register(
            "laser_beam",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "laser_beam"))
    );

    /**
     * Registers all sound events with the mod event bus.
     *
     * @param eventBus Mod event bus for registration
     */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
