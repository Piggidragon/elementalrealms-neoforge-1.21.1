package de.piggidragon.elementalrealms;

import com.mojang.logging.LogUtils;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.blocks.ModBlocks;
import de.piggidragon.elementalrealms.creativetabs.ModCreativeTabs;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.items.magic.affinities.AffinityItems;
import de.piggidragon.elementalrealms.items.magic.dimension.DimensionItems;
import de.piggidragon.elementalrealms.structures.ModStructurePlacements;
import de.piggidragon.elementalrealms.structures.ModStructures;
import de.piggidragon.elementalrealms.worldgen.chunkgen.ModChunkgen;
import de.piggidragon.elementalrealms.worldgen.features.ModFeatures;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

/**
 * Main mod class for Elemental Realms.
 * Manages registration of all mod content: items, entities, dimensions, structures, and worldgen.
 */
@Mod(ElementalRealms.MODID)
public class ElementalRealms {
    /** The unique identifier for this mod used by NeoForge. */
    public static final String MODID = "elementalrealms";
    
    /** Logger instance for mod-wide logging and debugging. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mod constructor - initializes all deferred registries.
     *
     * @param modEventBus  Mod-specific event bus for registration
     * @param modContainer Mod metadata and configuration container
     */
    public ElementalRealms(IEventBus modEventBus, ModContainer modContainer) {
        // Register data attachments for player affinities and dimension data
        ModAttachments.register(modEventBus);
        
        // Register magic system items (affinity orbs and dimension keys)
        AffinityItems.register(modEventBus);
        DimensionItems.register(modEventBus);
        
        // Register custom entities and blocks
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        
        // Register creative mode tabs for mod content organization
        ModCreativeTabs.register(modEventBus);
        
        // Register world generation components (structures, chunk generators, features)
        ModStructurePlacements.register(modEventBus);
        ModStructures.register(modEventBus);
        ModChunkgen.register(modEventBus);
        ModFeatures.register(modEventBus);

        // Register configuration screen factory for client-side settings
        // This only runs on the physical client to avoid server-side issues
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        // Subscribe to common setup event for post-registration initialization
        modEventBus.addListener(this::commonSetup);
    }

    /**
     * Common setup phase executed after registration finalization.
     * Runs on both client and server after all registries are frozen.
     *
     * @param event The common setup event containing deferred work queue
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for {}", MODID);
        LOGGER.info("Elemental Realms mod setup complete.");
    }
}
