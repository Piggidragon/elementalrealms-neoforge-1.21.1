package de.piggidragon.elementalrealms.registries.sounds;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred register for mod sound events.
 */
public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            Registries.SOUND_EVENT,
            ElementalRealms.MODID
    );

    private ModSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
