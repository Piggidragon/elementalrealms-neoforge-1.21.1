package de.piggidragon.elementalrealms.util.entities.portal;

import de.piggidragon.elementalrealms.registries.entities.custom.misc.PortalEntity;
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
 * Helpers for portal entities and worldgen validation.
 */
public final class PortalUtils {

    private static final int NETHER_MAX_Y = 128;
    private static final int END_MIN_Y = 50;

    private PortalUtils() {
    }

    public static boolean isSuitableForPortalBase(BlockState state) {
        if (state.isAir()) return false;
        return state.getFluidState().isEmpty();
    }

    public static PortalEntity findNearestPortal(ServerLevel level, Vec3 position, double searchRadius) {
        AABB searchArea = new AABB(
                position.x - searchRadius, position.y - searchRadius, position.z - searchRadius,
                position.x + searchRadius, position.y + searchRadius, position.z + searchRadius
        );

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

    public static boolean isVanilla(ResourceKey<Level> level) {
        return level == Level.OVERWORLD || level == Level.NETHER || level == Level.END;
    }

    public static boolean isValidDimensionForSpawn(WorldGenLevel level, BlockPos pos) {
        ResourceKey<Level> dimension = level.getLevel().dimension();
        if (dimension == Level.OVERWORLD) return true;
        if (dimension == Level.NETHER) return pos.getY() < NETHER_MAX_Y;
        if (dimension == Level.END) return pos.getY() > END_MIN_Y;
        return false;
    }
}
