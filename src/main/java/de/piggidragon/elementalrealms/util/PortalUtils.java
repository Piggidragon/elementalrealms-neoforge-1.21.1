package de.piggidragon.elementalrealms.util;

import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Utility methods for portal entity operations and worldgen validation.
 */
public class PortalUtils {

    /**
     * Checks if a block position is suitable for portal base placement.
     * Rejects air blocks and fluid blocks (water, lava).
     *
     * @param level WorldGen level for block access
     * @param pos   Position to check
     * @param state Block state at position
     * @return true if solid non-fluid block
     */
    public static boolean isSuitableForPortalBase(WorldGenLevel level, BlockPos pos, BlockState state) {
        // Skip air blocks
        if (state.isAir()) return false;

        // Skip fluids (water, lava, custom fluids)
        return state.getFluidState().isEmpty();
    }

    /**
     * Finds the nearest portal entity within search radius.
     * Used by /portal locate command.
     *
     * @param level        Server level to search in
     * @param position     Center of search area
     * @param searchRadius Maximum distance in blocks
     * @return Nearest portal or null if none found
     */
    public static PortalEntity findNearestPortal(ServerLevel level, Vec3 position, double searchRadius) {

        AABB searchArea = new AABB(
                position.x - searchRadius, position.y - searchRadius, position.z - searchRadius,
                position.x + searchRadius, position.y + searchRadius, position.z + searchRadius
        );

        // Get all living portal entities in area
        List<PortalEntity> portals = level.getEntitiesOfClass(
                PortalEntity.class,
                searchArea,
                Entity::isAlive
        );

        PortalEntity nearestPortal = null;
        double nearestDistance = Double.MAX_VALUE;

        for (PortalEntity portal : portals) {
            double distance = portal.position().distanceTo(position);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPortal = portal;

            }
        }
        return nearestPortal;
    }

    /**
     * Checks if dimension key matches vanilla dimensions.
     *
     * @param level Dimension key to check
     * @return true if Overworld, Nether, or End
     */
    public static boolean isVanilla(ResourceKey<Level> level) {
        return level == Level.OVERWORLD || level == Level.NETHER || level == Level.END;
    }

    /**
     * Validates if position is safe for portal spawn in given dimension.
     * Prevents ceiling spawns in Nether and void spawns in End.
     *
     * @param level WorldGen level
     * @param pos   Portal spawn position
     * @return true if position is valid for portal
     */
    public static boolean isValidDimensionForSpawn(WorldGenLevel level, BlockPos pos) {

        if (level.getLevel().dimension() == Level.OVERWORLD) {
            return true;
        } else if (level.getLevel().dimension() == Level.NETHER) {
            return pos.getY() < 128; // Avoid ceiling spawning
        } else if (level.getLevel().dimension() == Level.END) {
            return pos.getY() > 50; // Avoid void spawning
        }
        return false;
    }
}
