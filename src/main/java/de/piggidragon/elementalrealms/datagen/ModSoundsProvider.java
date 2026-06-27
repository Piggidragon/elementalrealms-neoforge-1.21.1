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

    public ModSoundsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ElementalRealms.MODID, helper);
    }

    @Override
    public void registerSounds() {
        // No sounds registered yet — re-add vanilla-rework or new sounds here when needed.
    }
}
