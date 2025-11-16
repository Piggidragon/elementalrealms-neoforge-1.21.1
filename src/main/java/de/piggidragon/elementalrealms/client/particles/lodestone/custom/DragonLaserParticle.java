package de.piggidragon.elementalrealms.client.particles.lodestone.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.util.ParticleUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

import java.awt.*;

public class DragonLaserParticle {

    /**
     * Spawns a fully interpolated beam effect from start to end position
     *
     * @param level The world level
     * @param start The starting position
     * @param end   The ending position
     * @param buffer The buffer to draw to
     * @param ps     The pose stack to draw with
     */
    public static void spawnBeam(Level level, Vec3 start, Vec3 end, MultiBufferSource buffer, PoseStack ps) {
        if (!level.isClientSide) return;
        BlockPos startpos = new BlockPos((int) start.x,(int) start.y,(int) start.z);

        ParticleUtil.spawnLineWithDensity(
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setScaleData(GenericParticleData.create(0.2f, 0.2f).setEasing(Easing.ELASTIC_IN).build())
                        .setTransparencyData(GenericParticleData.create(1f, 0f).setEasing(Easing.ELASTIC_IN).build())
                        .setColorData(ColorParticleData.create(new Color(255, 0, 0), new Color(255, 0, 0)).setEasing(Easing.ELASTIC_IN).build())
                        .setLifetime(1)
                        .setMotion(0, 0, 0)
                        .enableNoClip(),
                level,
                start,
                end,
                100
        );
    }
}
