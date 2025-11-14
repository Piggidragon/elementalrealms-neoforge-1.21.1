package de.piggidragon.elementalrealms.client.particles.lodestone.custom;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.*;

public class DragonLaserParticle {

    /**
     * Spawns example particles at the given position
     *
     * @param level The world level
     * @param pos   The position to spawn particles
     */
    public static void spawnDragonLaserParticle(Level level, Vec3 pos) {
        Color startingColor = new Color(100, 0, 100);
        Color endingColor = new Color(0, 100, 200);
        WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setScaleData(GenericParticleData.create(0.1f, 0).build())
                .setTransparencyData(GenericParticleData.create(0.75f, 0.25f).build())
                .setColorData(ColorParticleData.create(startingColor, endingColor).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
                .setSpinData(SpinParticleData.create(0.2f, 0.4f).setSpinOffset((level.getGameTime() * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build())
                .setLifetime(2)
                .setMotion(0,0,0)
                .enableNoClip()
                .spawn(level, pos.x, pos.y, pos.z);
    }

    /**
     * Spawns a beam effect with multiple particles along the line
     * Creates a more dense beam effect with slight randomization
     *
     * @param level The world level
     * @param start The starting position
     * @param end   The ending position
     */
    public static void spawnBeamEffect(Level level, Vec3 start, Vec3 end) {
        if (!level.isClientSide) return;

        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        // Spawn particles every 0.1 blocks for a dense beam
        int particleCount = (int) (distance / 0.05);

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3 particlePos = start.add(direction.scale(t));

            // Add slight random offset for a more natural look
            double offsetX = (level.random.nextDouble() - 0.5) * 0.05;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.05;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.05;

            particlePos = particlePos.add(offsetX, offsetY, offsetZ);

            spawnDragonLaserParticle(level, particlePos);
        }
    }

}
