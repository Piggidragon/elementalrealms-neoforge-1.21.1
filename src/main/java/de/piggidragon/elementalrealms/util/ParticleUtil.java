package de.piggidragon.elementalrealms.util;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import team.lodestar.lodestone.helpers.*;
import team.lodestar.lodestone.systems.particle.builder.*;
import java.util.Random;

/**
 * Utility class for enhanced particle effects with Lodestone
 */
public class ParticleUtil {

    /**
     * Creates a block outline with custom particle density and returns the builder for chaining
     * @param builder The WorldParticleBuilder to use
     * @param level The level/world to spawn particles in
     * @param pos The block position
     * @param state The block state for shape calculation
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
        return builder; // Builder zurückgeben für Chaining
    }

    /**
     * Spawns particles along a line with uniform spacing
     * @param builder The WorldParticleBuilder to use
     * @param level The level/world to spawn particles in
     * @param start Start position of the line
     * @param end End position of the line
     * @param particleCount Number of particles to spawn along the line
     */

    public static void spawnLineWithDensity(WorldParticleBuilder builder, Level level, Vec3 start, Vec3 end, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            // Calculate uniform position along the line (t ranges from 0.0 to 1.0)
            double t = particleCount > 1 ? (double) i / (particleCount - 1) : 0.5;

            // Linear interpolation for uniform distribution
            Vec3 pos = start.lerp(end, t);

            // Spawn the particle at the calculated position
            builder.spawn(level, pos.x, pos.y, pos.z);
        }
    }
}
