package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Generates sounds.json definitions for custom sound events.
 * Configures volume, pitch, and subtitle for each sound.
 */
public class ModSoundsProvider extends SoundDefinitionsProvider {

    /**
     * Creates the sound definitions provider.
     *
     * @param output Output location for generated files
     * @param helper Helper to verify referenced sound files exist
     */
    protected ModSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ElementalRealms.MODID, helper);
    }

    // Registers all mod sound definitions
    @Override
    public void registerSounds() {
        // Dragon laser beam attack sound
        add(ModSounds.LASER_BEAM, SoundDefinition.definition()
                .with(
                        sound("elementalrealms:laser_beam")
                                .volume(0.5f)
                                .pitch(1.0f)
                )
                .subtitle("sound.elementalrealms.laser_beam")
                .replace(true)
        );
    }
}
