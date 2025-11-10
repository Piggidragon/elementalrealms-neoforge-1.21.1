package de.piggidragon.elementalrealms;

import com.mojang.logging.LogUtils;
import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.blocks.ModBlocks;
import de.piggidragon.elementalrealms.creativetabs.ModCreativeTabs;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.guis.menus.ModMenus;
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
    public static final String MODID = "elementalrealms";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mod constructor - initializes all deferred registries.
     *
     * @param modEventBus  Mod-specific event bus for registration
     * @param modContainer Mod metadata and configuration container
     */
    public ElementalRealms(IEventBus modEventBus, ModContainer modContainer) {
        // Register all mod content
        ModAttachments.register(modEventBus);
        AffinityItems.register(modEventBus);
        DimensionItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModStructurePlacements.register(modEventBus);
        ModStructures.register(modEventBus);
        ModChunkgen.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModMenus.register(modEventBus);

        // Client-only: register configuration screen
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        modEventBus.addListener(this::commonSetup);
    }

    /**
     * Common setup phase executed after registration finalization.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for {}", MODID);
    }
}
