package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.worldgen.chunkgen.custom.BoundedChunkGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Handles world border configuration for custom dimensions.
 *
 * <p>Automatically configures world borders for all mod dimensions when the server starts,
 * setting size, center position, warning distance, and damage parameters.</p>
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class DimensionBorderHandler {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Configure world borders for each mod dimension
        for (ResourceKey<Level> level : ModLevel.LEVELS) {
            ServerLevel serverLevel = event.getServer().getLevel(level);
            if (serverLevel != null) {
                setupWorldBorder(serverLevel);
            }
        }
    }

    public static void setupWorldBorder(ServerLevel level) {
        WorldBorder border = level.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(BoundedChunkGenerator.MAX_CHUNKS * 16 * 2);
        border.setWarningBlocks(10);
        border.setDamagePerBlock(1.0);

        ElementalRealms.LOGGER.info("World border set for dimension {} - Size: {}x{} blocks",
                level.dimension().location(), BoundedChunkGenerator.MAX_CHUNKS * 16 * 2, BoundedChunkGenerator.MAX_CHUNKS * 16 * 2);
    }
}
