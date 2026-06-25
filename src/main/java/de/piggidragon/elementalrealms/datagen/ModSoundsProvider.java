package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Generates sounds.json definitions for mod sound events.
 */
public class ModSoundsProvider extends SoundDefinitionsProvider {

    private static final float LASER_VOLUME = 0.5f;
    private static final float LASER_PITCH = 1.0f;

    public ModSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ElementalRealms.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.LASER_BEAM, SoundDefinition.definition()
                .with(sound("elementalrealms:laser_beam").volume(LASER_VOLUME).pitch(LASER_PITCH))
                .subtitle("sound.elementalrealms.laser_beam")
                .replace(true)
        );
    }
}
