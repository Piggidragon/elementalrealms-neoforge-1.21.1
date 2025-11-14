package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypTagsProvider extends DamageTypeTagsProvider {

    public ModDamageTypTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ElementalRealms.LOGGER.debug(ModDatapackProvider.LASER.toString());
        this.tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(ModDatapackProvider.LASER);
        this.tag(DamageTypeTags.BYPASSES_COOLDOWN)
                .add(ModDatapackProvider.LASER);
        this.tag(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                .add(ModDatapackProvider.LASER);
        this.tag(DamageTypeTags.BYPASSES_SHIELD)
                .add(ModDatapackProvider.LASER);
    }
}
