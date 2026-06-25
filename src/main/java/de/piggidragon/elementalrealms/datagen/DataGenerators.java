package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.datagen.advancements.AdvancementGenerator;
import de.piggidragon.elementalrealms.datagen.recipes.AffinityRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Registers all data generators used by the mod.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class DataGenerators {

    private DataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new AffinityRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(
                packOutput,
                lookupProvider,
                List.of(new AdvancementGenerator())
        ));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                packOutput,
                lookupProvider,
                ModDatapackProvider.createBuilder(),
                Set.of(ElementalRealms.MODID)
        ));
        generator.addProvider(event.includeClient(), new ModSoundsProvider(packOutput, event.getExistingFileHelper()));
    }
}
