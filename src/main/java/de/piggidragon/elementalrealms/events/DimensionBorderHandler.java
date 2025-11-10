package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.worldgen.chunkgen.custom.BoundedChunkGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
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
        DynamicDimensionHandler.initialize(event.getServer());

        for (ResourceKey<Level> level : ModLevel.getLevels()) {
            ServerLevel serverLevel = event.getServer().getLevel(level);
            if (serverLevel != null) {
                // Get generation centers (now safe because we initialized above)
                for (ChunkPos generationCenter : DynamicDimensionHandler.getGenerationSavedData().getGenerationCenters().values()) {
                    setupWorldBorder(serverLevel, generationCenter);
                }
            }
        }
    }

    /**
     * Sets up the world border for a dimension at the given generation center.
     *
     * @param level            The server level to configure
     * @param generationCenter The center position for the world border
     */
    public static void setupWorldBorder(ServerLevel level, ChunkPos generationCenter) {
        WorldBorder border = level.getWorldBorder();
        border.setCenter(generationCenter.getMiddleBlockX(), generationCenter.getMiddleBlockZ());
        border.setSize(BoundedChunkGenerator.getTotalSize());
        border.setWarningBlocks(10);
        border.setDamagePerBlock(1.0);

        ElementalRealms.LOGGER.info("World border set for dimension {} - Center: [{}, {}], Size: {}x{} blocks",
                level.dimension().location(),
                generationCenter.getMiddleBlockX(),
                generationCenter.getMiddleBlockZ(),
                BoundedChunkGenerator.getTotalSize(),
                BoundedChunkGenerator.getTotalSize());
    }
}
