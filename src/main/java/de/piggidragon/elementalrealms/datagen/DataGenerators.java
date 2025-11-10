package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.advancements.AdvancementGenerator;
import de.piggidragon.elementalrealms.datagen.recipes.AffinityRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Registers all data generators for automated JSON file creation.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)  // Add bus = MOD
public class DataGenerators {

    /**
     * Registers all data generators during the data generation phase.
     */
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Model provider (client-side)
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, event.getExistingFileHelper()));

        // Recipe provider (server-side)
        generator.addProvider(event.includeServer(), new AffinityRecipeProvider(packOutput, lookupProvider));

        // Advancement provider (server-side)
        generator.addProvider(
                event.includeServer(),  // Changed from true to event.includeServer()
                new ModAdvancementProvider(
                        packOutput,
                        lookupProvider,
                        List.of(new AdvancementGenerator())
                )
        );

        // Biome tags provider (server-side)
        generator.addProvider(
                event.includeServer(),  // Changed from true to event.includeServer()
                new ModBiomeTagsProvider(packOutput, lookupProvider)
        );

        event.createDatapackRegistryObjects(ModFeaturesProvider.createBuilder());
    }
}
