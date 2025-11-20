package de.piggidragon.elementalrealms.util;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.lodestar.lodestone.helpers.BlockHelper;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;

/**
 * Utility class for enhanced particle effects with Lodestone
 */
public class ParticleUtil {

    /**
     * Creates a block outline with custom particle density and returns the builder for chaining
     *
     * @param builder          The WorldParticleBuilder to use
     * @param level            The level/world to spawn particles in
     * @param pos              The block position
     * @param state            The block state for shape calculation
     * @param particlesPerEdge Number of particles to spawn per edge (higher = denser outline)
     * @return The same builder instance for method chaining
     */

    public static WorldParticleBuilder createBlockOutlineWithDensity(WorldParticleBuilder builder, Level level, BlockPos pos, BlockState state, int particlesPerEdge) {
        VoxelShape voxelShape = state.getShape(level, pos);
        voxelShape.forAllBoxes(
                (x1, y1, z1, x2, y2, z2) -> {
                    Vec3 v = BlockHelper.fromBlockPos(pos);
                    Vec3 b = v.add(x1, y1, z1);
                    Vec3 e = v.add(x2, y2, z2);

                    // Bottom edges (4 edges)
                    spawnLineWithDensity(builder, level, b, v.add(x2, y1, z1), particlesPerEdge);
                    spawnLineWithDensity(builder, level, b, v.add(x1, y1, z2), particlesPerEdge);
                    spawnLineWithDensity(builder, level, v.add(x2, y1, z1), v.add(x2, y1, z2), particlesPerEdge);
                    spawnLineWithDensity(builder, level, v.add(x1, y1, z2), v.add(x2, y1, z2), particlesPerEdge);

                    // Top edges (4 edges)
                    spawnLineWithDensity(builder, level, v.add(x1, y2, z1), v.add(x2, y2, z1), particlesPerEdge);
                    spawnLineWithDensity(builder, level, v.add(x1, y2, z1), v.add(x1, y2, z2), particlesPerEdge);
                    spawnLineWithDensity(builder, level, e, v.add(x2, y2, z1), particlesPerEdge);
                    spawnLineWithDensity(builder, level, e, v.add(x1, y2, z2), particlesPerEdge);

                    // Vertical edges (4 edges)
                    spawnLineWithDensity(builder, level, b, v.add(x1, y2, z1), particlesPerEdge);
                    spawnLineWithDensity(builder, level, v.add(x2, y1, z1), v.add(x2, y2, z1), particlesPerEdge);
                    spawnLineWithDensity(builder, level, v.add(x1, y1, z2), v.add(x1, y2, z2), particlesPerEdge);
                    spawnLineWithDensity(builder, level, e, v.add(x2, y1, z2), particlesPerEdge);
                }
        );
        return builder;
    }

    public static boolean spawnLineWithAnimation(WorldParticleBuilder builder, Level level, Vec3 start, Vec3 end,
                                                 double densityPerBlock, int beamLifeTicks, int travelTicks,
                                                 int elapsedTicks) {
        if (elapsedTicks > beamLifeTicks) {
            return false;
        }

        Vec3 beamEnd;

        if (elapsedTicks < travelTicks) {
            ElementalRealms.LOGGER.debug("Traveling beam particles at tick " + elapsedTicks);
            double t = Math.min(1.0, (double) elapsedTicks / travelTicks);
            beamEnd = start.lerp(end, t);
        } else {
            beamEnd = end;
        }

        double currentDistance = start.distanceTo(beamEnd);
        int particlesToSpawn = Math.max(1, (int) Math.ceil(currentDistance * densityPerBlock));

        for (int i = 0; i < particlesToSpawn; i++) {
            double t = particlesToSpawn > 1 ? (double) i / (particlesToSpawn - 1) : 0.5;
            Vec3 pos = start.lerp(beamEnd, t);
            builder.spawn(level, pos.x, pos.y, pos.z);
        }
        return true;
    }

    /**
     * Spawns particles along a line with specified density per block (instant spawn).
     *
     * @param builder         The particle builder to use for spawning
     * @param level           The level/world to spawn particles in
     * @param start           Start position of the line
     * @param end             End position of the line
     * @param densityPerBlock Number of particles to spawn per block distance
     */
    public static void spawnLineWithDensity(WorldParticleBuilder builder, Level level, Vec3 start, Vec3 end,
                                            double densityPerBlock) {
        spawnLineWithAnimation(builder, level, start, end, densityPerBlock, 0, 0, 0);
    }
}
