package de.piggidragon.elementalrealms.client.rendering.tasks.tick;

import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.client.rendering.tasks.TickTask;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

import java.awt.*;
import java.util.Optional;

/**
 * Laser beam task with frame-based particle spawning using Lodestone
 */
public class LaserBeamTask implements TickTask {

    private final Entity entity;
    private final Level level;

    private final float damageAmount;
    private final int densityPerBlock;

    private final Vec3 startPos;
    private final Vec3 directionVec;
    private Vec3 endPos;

    private final double beamRange;
    private final int travelTime;
    private final int lifeTicks;

    // Ender Dragon Colors (Neon Purple -> Magenta)
    private static final Color START_COLOR = new Color(180, 0, 255);
    private static final Color END_COLOR = new Color(255, 100, 255);
    // Core Colors (White -> Light Pink)
    private static final Color CORE_COLOR = new Color(255, 255, 255);
    private static final Color CORE_END_COLOR = new Color(255, 200, 255);

    private int currentTick = 0;

    public LaserBeamTask(Entity entity, Level level, double beamRange, int densityPerBlock, float damageAmount, int lifeTicks, int travelTime) {
        this(entity, level, entity.position(), entity.getViewVector(1.0f), beamRange, densityPerBlock, damageAmount, lifeTicks, travelTime);
    }

    public LaserBeamTask(Entity entity, Level level, Vec3 customStartPos, Vec3 directionVec, double beamRange, int densityPerBlock, float damageAmount, int lifeTicks, int travelTime) {
        this.entity = entity;
        this.level = level;
        this.damageAmount = damageAmount;
        this.densityPerBlock = densityPerBlock;
        this.lifeTicks = lifeTicks;
        this.travelTime = travelTime;
        this.directionVec = directionVec;
        this.startPos = customStartPos;
        this.endPos = startPos;
        this.beamRange = beamRange;
    }

    @Override
    public void tick() {
        if (currentTick > lifeTicks) {
            RenderManager.requestRemoveTickTask(this);
            return;
        }

        update();

        // Nur rendern, wenn der Strahl eine Länge hat
        if (startPos.distanceToSqr(endPos) > 0.01) {
            renderLaser(level, startPos, endPos, densityPerBlock);
        }

        Entity hitEntity = raycastEntityHit(level, entity, startPos, endPos);
        if (hitEntity != null) {
            PacketDistributor.sendToServer(
                    new ParticleHitEntityPacket(hitEntity.getId(), damageAmount)
            );
        }

        currentTick++;
    }

    public void update() {
        double progress = 1.0;
        if (travelTime > 0) {
            progress = Math.min(1.0, (double) currentTick / travelTime);
        }

        double currentLength = beamRange * progress;
        endPos = startPos.add(directionVec.scale(currentLength));
    }

    private static void renderLaser(Level level, Vec3 startPos, Vec3 targetPos, int density) {
        if (!level.isClientSide) return;

        Vec3 direction = targetPos.subtract(startPos);
        double distance = direction.length();

        if (distance < 0.05) return;

        direction = direction.normalize();
        int particleCount = (int) (distance * density);

        WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setTransparencyData(GenericParticleData.create(0.75f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.45f, 0.0f).build())
                .setColorData(ColorParticleData.create(START_COLOR, END_COLOR).build())
                .setLifetime(12)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .setMotion(0, 0, 0)
                .enableNoClip();

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 spawnPos = startPos.add(direction.scale(distance * progress));

            double jitter = 0.06;
            double oX = (Math.random() - 0.5) * jitter;
            double oY = (Math.random() - 0.5) * jitter;
            double oZ = (Math.random() - 0.5) * jitter;

            builder.spawn(level, spawnPos.x + oX, spawnPos.y + oY, spawnPos.z + oZ);
        }

        WorldParticleBuilder coreBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.15f, 0.0f).build())
                .setColorData(ColorParticleData.create(CORE_COLOR, CORE_END_COLOR).build())
                .setLifetime(8)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        int coreCount = (int) (distance * 2.5);
        for (int i = 0; i < coreCount; i++) {
            double progress = (double) i / coreCount;
            Vec3 corePos = startPos.add(direction.scale(distance * progress));
            coreBuilder.spawn(level, corePos.x, corePos.y, corePos.z);
        }
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

