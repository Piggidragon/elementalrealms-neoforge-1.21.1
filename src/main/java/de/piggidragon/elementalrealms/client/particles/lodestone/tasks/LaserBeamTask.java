package de.piggidragon.elementalrealms.client.particles.lodestone.tasks;

import de.piggidragon.elementalrealms.client.particles.lodestone.RenderManager;
import de.piggidragon.elementalrealms.client.particles.lodestone.RenderOrTickTask;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypes;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

import java.awt.*;
import java.util.Optional;

/**
 * Laser beam task with frame-based particle spawning for smooth rendering
 */
public class LaserBeamTask implements RenderOrTickTask {

    private final Player player;
    private final Level level;

    private final float damageAmount;

    private final int densityPerBlock;
    private final Vec3 startPos;
    private final Vec3 directionVec;
    private Vec3 endPos;

    private final float beamRange;
    private final int travelTime;
    private final int lifeTicks;

    private int currentTick = 0;

    public LaserBeamTask(Player player, Level level, float beamRange, int densityPerBlock, float damageAmount, int lifeTicks, int travelTime) {
        this.player = player;
        this.level = level;
        this.damageAmount = damageAmount;
        this.directionVec = player.getLookAngle();
        this.travelTime = travelTime;
        this.lifeTicks = lifeTicks;
        this.densityPerBlock = densityPerBlock;
        this.beamRange = beamRange;

        Vec3 directionVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        this.startPos = eyePos.add(directionVec.scale(1.2f));
    }

    @Override
    public void tick() {
        if (currentTick > lifeTicks) {
            RenderManager.requestRemoveTask(this);
        }
        update();
        spawnOuterBeam(level, startPos, endPos, densityPerBlock);
        spawnInnerBeam(level, startPos, endPos, densityPerBlock);

        Entity hitEntity = raycastEntityHit(level, player, startPos, endPos);
        if (hitEntity != null) {
            PacketDistributor.sendToServer(
                    new ParticleHitEntityPacket(hitEntity.getId(), damageAmount)
            );
        }

        currentTick++;
    }

    public void update() {
        if (currentTick > travelTime) {
            return;
        }

        double progress = (double) currentTick / travelTime;
        double currentLength = beamRange * progress;

        endPos = startPos.add(directionVec.scale(currentLength));
    }

    private void spawnOuterBeam(Level level, Vec3 start, Vec3 end, int densityPerBlock) {
        if (!level.isClientSide) return;

        spawnLine(
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setScaleData(GenericParticleData.create(1f).setEasing(Easing.ELASTIC_IN).build())
                        .setTransparencyData(GenericParticleData.create(0.1f).setEasing(Easing.ELASTIC_IN).build())
                        //.setRenderType(LodestoneWorldParticleRenderType.LUMITRANSPARENT)
                        .setColorData(ColorParticleData.create(255,0,0).setEasing(Easing.ELASTIC_IN).build())
                        .setLifetime(1)
                        .enableNoClip(),
                level,
                start,
                end,
                densityPerBlock,
                0.015
        );
    }

    private void spawnInnerBeam(Level level, Vec3 start, Vec3 end, int densityPerBlock) {
        if (!level.isClientSide) return;

        spawnLine(
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setScaleData(GenericParticleData.create(0.15f).setEasing(Easing.ELASTIC_IN).build())
                        .setRenderType(LodestoneWorldParticleRenderType.LUMITRANSPARENT)
                        .setRenderTarget(RenderHandler.LATE_DELAYED_RENDER)
                        .setColorData(ColorParticleData.create(new Color(255,0,0)).setEasing(Easing.ELASTIC_IN).build())
                        .setLifetime(1)
                        .enableNoClip(),
                level,
                start,
                end,
                densityPerBlock,
                0.025
        );
    }

    public static WorldParticleBuilder spawnLine(WorldParticleBuilder builder, Level level, Vec3 one, Vec3 two, int densityPerBlock, double intensityOffset) {
        Vec3 diff = two.subtract(one);
        double length = diff.length();

        int particleCount = (int) Math.ceil(length * densityPerBlock);

        for (int i = 0; i <= particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3 pos = one.lerp(two, t);

            double offsetX = (Math.random() * 2 - 1) * intensityOffset;
            double offsetY = (Math.random() * 2 - 1) * intensityOffset;
            double offsetZ = (Math.random() * 2 - 1) * intensityOffset;

            Vec3 posWithOffset = pos.add(offsetX, offsetY, offsetZ);

            level.addParticle(builder.getParticleOptions(), false, posWithOffset.x, posWithOffset.y, posWithOffset.z, 0, 0, 0);
        }

        return builder;
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
}
