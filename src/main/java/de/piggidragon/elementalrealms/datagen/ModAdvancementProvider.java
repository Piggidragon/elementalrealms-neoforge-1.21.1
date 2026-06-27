package de.piggidragon.elementalrealms.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Wraps a list of {@link AdvancementSubProvider} for the datagen system.
 */
public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries,
            List<AdvancementSubProvider> subProviders
    ) {
        super(output, registries, subProviders);
    }
}
