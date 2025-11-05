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
 * Utility methods for portal generation and validation.
 */
public class PortalUtils {

    /**
     * Check if a block position is suitable for portal base placement.
     */
    public static boolean isSuitableForPortalBase(WorldGenLevel level, BlockPos pos, BlockState state) {
        // Skip air blocks
        if (state.isAir()) return false;

        // Skip fluids (water, lava, custom fluids)
        return state.getFluidState().isEmpty();
    }

    public static PortalEntity findNearestPortal(ServerLevel level, Vec3 position, double searchRadius) {
        AABB searchArea = new AABB(
                position.x - searchRadius, position.y - searchRadius, position.z - searchRadius,
                position.x + searchRadius, position.y + searchRadius, position.z + searchRadius
        );

        // Use getEntities with predicate for more control
        List<Entity> entities = level.getEntities(
                (Entity) null,  // Entity to exclude (null = none)
                searchArea,
                entity -> entity instanceof PortalEntity && entity.isAlive() && !entity.isRemoved()
        );

        PortalEntity nearestPortal = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (entity instanceof PortalEntity portal) {
                double distance = portal.position().distanceTo(position);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPortal = portal;
                }
            }
        }

        return nearestPortal;
    }

    public static boolean isVanilla(ResourceKey<Level> level) {
        if (level == Level.OVERWORLD) {
            return true;
        } else if (level == Level.NETHER) {
            return true;
        } else if (level == Level.END) {
            return true;
        }
        return false;
    }

    public static boolean isValidDimensionForSpawn(WorldGenLevel level, BlockPos pos) {

        if (level == Level.OVERWORLD) {
            return true;
        } else if (level == Level.NETHER) {
            return pos.getY() < 128; // Avoid ceiling spawning
        } else if (level == Level.END) {
            return pos.getY() > 50; // Avoid void spawning
        }
        return false;
    }
}
