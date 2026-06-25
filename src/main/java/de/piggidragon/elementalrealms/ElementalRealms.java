package de.piggidragon.elementalrealms;

import com.mojang.logging.LogUtils;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.blocks.ModBlocks;
import de.piggidragon.elementalrealms.registries.creativetabs.ModCreativeTabs;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.guis.menus.ModMenus;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.AffinityItems;
import de.piggidragon.elementalrealms.registries.items.magic.misc.MiscItems;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import de.piggidragon.elementalrealms.registries.worldgen.chunkgen.ModChunkgen;
import de.piggidragon.elementalrealms.registries.worldgen.features.ModFeatures;
import de.piggidragon.elementalrealms.registries.worldgen.structures.ModStructurePlacements;
import de.piggidragon.elementalrealms.registries.worldgen.structures.ModStructures;
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
 * Main entry point for Elemental Realms.
 * Coordinates registration of all mod content with the NeoForge event bus.
 */
@Mod(ElementalRealms.MODID)
public class ElementalRealms {
    public static final String MODID = "elementalrealms";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElementalRealms(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        AffinityItems.register(modEventBus);
        MiscItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModStructurePlacements.register(modEventBus);
        ModStructures.register(modEventBus);
        ModChunkgen.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for {}", MODID);
    }
}
