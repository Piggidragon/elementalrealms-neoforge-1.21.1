package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundsProvider extends SoundDefinitionsProvider {

    protected ModSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ElementalRealms.MODID, helper);
    }

    @Override
    public void registerSounds() {
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
