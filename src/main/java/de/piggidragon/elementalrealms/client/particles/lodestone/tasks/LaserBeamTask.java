package de.piggidragon.elementalrealms.client.particles.lodestone.tasks;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.client.particles.lodestone.RenderManager;
import de.piggidragon.elementalrealms.client.particles.lodestone.RenderTask;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import de.piggidragon.elementalrealms.util.ParticleUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

import java.awt.*;
import java.util.Optional;

/**
 * Laser beam task with frame-based particle spawning for smooth rendering
 */
public class LaserBeamTask implements RenderTask {

    private final Player player;
    private final Level level;
    private final float damageAmount;
    private final int DENSITY_PER_BLOCK = 50;
    private final int beamTravelTicks;
    private final int beamLifeTicks;
    private final Vec3 startPos;
    private final Vec3 endPos;
    private int currentTick = 0;


    public LaserBeamTask(Player player, Level level, float beamRange, float damageAmount, int beamLifeTicks, int beamTravelTicks) {
        this.player = player;
        this.level = level;
        this.damageAmount = damageAmount;
        this.beamLifeTicks = beamLifeTicks;
        this.beamTravelTicks = beamTravelTicks;

        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(1.0f);
        this.startPos = eyePos.add(lookVec.scale(1.2));
        this.endPos = eyePos.add(lookVec.scale(beamRange));
    }

    public static boolean spawnBeam(Level level, Vec3 start, Vec3 end, int densityPerBlock, int travelTicks, int beamLifeTicks, int elapsedTicks) {
        if (!level.isClientSide) return false;

        return ParticleUtil.spawnLineWithAnimation(
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setScaleData(GenericParticleData.create(0.2f, 0.2f).setEasing(Easing.ELASTIC_IN).build())
                        .setTransparencyData(GenericParticleData.create(1f, 0f).setEasing(Easing.ELASTIC_IN).build())
                        .setColorData(ColorParticleData.create(new Color(50, 0, 50), Color.BLACK).setEasing(Easing.ELASTIC_IN).build())
                        .setLifetime(10)
                        .setMotion(0, 0, 0)
                        .setRandomOffset(0.3f)
                        .enableNoClip(),
                level,
                start,
                end,
                densityPerBlock,
                beamLifeTicks,
                travelTicks,
                elapsedTicks
        );
    }

    private static Entity raycastEntityHit(Level level, Entity entityOwner, Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0D);
        Entity hitEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : level.getEntities(entityOwner, searchBox, e -> e.isAlive() && e != entityOwner)) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> optional = entityBox.clip(start, end);
            if (optional.isPresent()) {
                double distance = start.distanceToSqr(optional.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    hitEntity = entity;
                }
            }
        }
        return hitEntity;
    }

    @Override
    public void render(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
    }

    @Override
    public void tick() {
        if (!spawnBeam(level, startPos, endPos, DENSITY_PER_BLOCK, beamTravelTicks, beamLifeTicks, currentTick)) {
            RenderManager.requestRemoveTask(this);
            return;
        }

        Entity hitEntity = raycastEntityHit(level, player, startPos, endPos);
        if (hitEntity != null) {
            PacketDistributor.sendToServer(
                    new ParticleHitEntityPacket(hitEntity.getId(), damageAmount)
            );
        }
        currentTick++;
    }
}
