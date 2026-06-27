package de.piggidragon.elementalrealms.client.rendering.tasks.tick;

import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.client.rendering.tasks.TickTask;
import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamDestroyBlockPacket;
import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamHitEntityPacket;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Per-tick laser beam task: extends toward the target, spawns Lodestone particles,
 * detects entity hits, and periodically requests block destruction.
 */
public class LaserBeamTask implements TickTask {

    private static final float BUBBLE_RADIUS = 1.5f;

    // Beam colors: Neon Purple -> Magenta.
    private static final Color START_COLOR = new Color(180, 0, 255);
    private static final Color END_COLOR = new Color(255, 100, 255);
    // Core colors: White -> Light Pink.
    private static final Color CORE_COLOR = new Color(255, 255, 255);
    private static final Color CORE_END_COLOR = new Color(255, 200, 255);

    private static final float BEAM_JITTER = 0.06f;
    private static final int BEAM_PARTICLE_LIFETIME = 12;
    private static final float BEAM_SCALE = 0.45f;
    private static final float BEAM_TRANSPARENCY = 0.75f;
    private static final float CORE_SCALE = 0.15f;
    private static final int CORE_PARTICLE_LIFETIME = 8;
    private static final float CORE_DENSITY_MULTIPLIER = 2.5f;

    private static final float BUBBLE_SCALE = 0.3f;
    private static final float BUBBLE_TRANSPARENCY = 0.6f;
    private static final int BUBBLE_PARTICLE_LIFETIME = 10;
    private static final int BUBBLE_PARTICLE_COUNT = 20;
    private static final float BUBBLE_NOISE_MIN = 0.9f;
    private static final float BUBBLE_NOISE_RANGE = 0.2f;

    private static final float BUBBLE_INNER_SCALE = 0.2f;
    private static final float BUBBLE_INNER_TRANSPARENCY = 0.8f;
    private static final int BUBBLE_INNER_LIFETIME = 5;
    private static final int BUBBLE_INNER_COUNT = 3;
    private static final float BUBBLE_INNER_RADIUS_MULTIPLIER = 0.8f;

    private static final double BEAM_PATH_EPSILON_SQUARED = 0.01;
    private static final int BLOCK_DESTRUCTION_INTERVAL = 5;
    private static final double BEAM_AABB_INFLATE = 0.3;
    private static final double BUBBLE_AABB_INFLATE = 2;

    private final Entity entity;
    private final Level level;
    private final float damageAmount;
    private final int densityPerBlock;
    private final Vec3 startPos;
    private final Vec3 directionVec;
    private final double beamRange;
    private final int travelTime;
    private final int lifeTicks;
    private Vec3 endPos;
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

    private static List<Entity> searchEntityBoxHit(Level level, Entity entityOwner, AABB box) {
        List<Entity> hits = new ArrayList<>();
        hits.addAll(level.getEntities(entityOwner, box, e -> e.isAlive() && e != entityOwner));
        return hits;
    }

    private static void renderLaser(Level level, Vec3 startPos, Vec3 targetPos, int density) {
        if (!level.isClientSide) return;

        Vec3 direction = targetPos.subtract(startPos);
        double distance = direction.length();
        if (distance < 0.05) return;
        direction = direction.normalize();

        WorldParticleBuilder beamBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setTransparencyData(GenericParticleData.create(BEAM_TRANSPARENCY, 0.0f).build())
                .setScaleData(GenericParticleData.create(BEAM_SCALE, 0.0f).build())
                .setColorData(ColorParticleData.create(START_COLOR, END_COLOR).build())
                .setLifetime(BEAM_PARTICLE_LIFETIME)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .setMotion(0, 0, 0)
                .enableNoClip();

        int beamParticleCount = (int) (distance * density);
        for (int i = 0; i < beamParticleCount; i++) {
            double progress = (double) i / beamParticleCount;
            Vec3 spawnPos = startPos.add(direction.scale(distance * progress));
            double offsetX = (Math.random() - 0.5) * BEAM_JITTER;
            double offsetY = (Math.random() - 0.5) * BEAM_JITTER;
            double offsetZ = (Math.random() - 0.5) * BEAM_JITTER;
            beamBuilder.spawn(level, spawnPos.x + offsetX, spawnPos.y + offsetY, spawnPos.z + offsetZ);
        }

        WorldParticleBuilder coreBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                .setScaleData(GenericParticleData.create(CORE_SCALE, 0.0f).build())
                .setColorData(ColorParticleData.create(CORE_COLOR, CORE_END_COLOR).build())
                .setLifetime(CORE_PARTICLE_LIFETIME)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        int coreCount = (int) (distance * CORE_DENSITY_MULTIPLIER);
        for (int i = 0; i < coreCount; i++) {
            double progress = (double) i / coreCount;
            Vec3 corePos = startPos.add(direction.scale(distance * progress));
            coreBuilder.spawn(level, corePos.x, corePos.y, corePos.z);
        }
    }

    /**
     * Hollow sphere of additive particles with a few inner sparkles for an "energy" look.
     */
    private static void renderHitSphere(Level level, Vec3 center, float radius) {
        if (!level.isClientSide) return;

        WorldParticleBuilder sphereBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setTransparencyData(GenericParticleData.create(BUBBLE_TRANSPARENCY, 0.0f).build())
                .setScaleData(GenericParticleData.create(BUBBLE_SCALE, 0.0f).build())
                .setColorData(ColorParticleData.create(START_COLOR, END_COLOR).build())
                .setLifetime(BUBBLE_PARTICLE_LIFETIME)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        for (int i = 0; i < BUBBLE_PARTICLE_COUNT; i++) {
            Vec3 dir = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize().scale(radius);
            float noise = BUBBLE_NOISE_MIN + (float) (Math.random() * BUBBLE_NOISE_RANGE);
            Vec3 spawnPos = center.add(dir.scale(noise));
            sphereBuilder.spawn(level, spawnPos.x, spawnPos.y, spawnPos.z);
        }

        WorldParticleBuilder coreBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setTransparencyData(GenericParticleData.create(BUBBLE_INNER_TRANSPARENCY, 0.0f).build())
                .setScaleData(GenericParticleData.create(BUBBLE_INNER_SCALE, 0.0f).build())
                .setColorData(ColorParticleData.create(CORE_COLOR, CORE_END_COLOR).build())
                .setLifetime(BUBBLE_INNER_LIFETIME)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        for (int i = 0; i < BUBBLE_INNER_COUNT; i++) {
            Vec3 inner = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5)
                    .normalize()
                    .scale(Math.random() * radius * BUBBLE_INNER_RADIUS_MULTIPLIER);
            Vec3 spawnPos = center.add(inner);
            coreBuilder.spawn(level, spawnPos.x, spawnPos.y, spawnPos.z);
        }
    }

    @Override
    public void tick() {
        if (currentTick > lifeTicks) {
            RenderManager.requestRemoveTickTask(this);
            return;
        }

        updatePositions();

        if (startPos.distanceToSqr(endPos) > BEAM_PATH_EPSILON_SQUARED) {
            renderLaser(level, startPos, endPos, densityPerBlock);
            renderHitSphere(level, endPos, BUBBLE_RADIUS);

            if (currentTick % BLOCK_DESTRUCTION_INTERVAL == 0) {
                requestBlockDestruction(endPos, BUBBLE_RADIUS);
            }
        }

        List<Entity> hitEntities = new ArrayList<>();
        hitEntities.addAll(searchEntityBoxHit(level, entity, new AABB(startPos, endPos).inflate(BEAM_AABB_INFLATE)));
        hitEntities.addAll(searchEntityBoxHit(level, entity, new AABB(endPos, endPos).inflate(BUBBLE_AABB_INFLATE)));

        for (Entity e : hitEntities) {
            PacketDistributor.sendToServer(new EnderDragonLaserBeamHitEntityPacket(e.getId(), damageAmount));
        }

        currentTick++;
    }

    /**
     * Extends the end position from start toward the target, scaled by travel time progress.
     */
    public void updatePositions() {
        double progress = travelTime > 0 ? Math.min(1.0, (double) currentTick / travelTime) : 1.0;
        endPos = startPos.add(directionVec.scale(beamRange * progress));
    }

    private void requestBlockDestruction(Vec3 pos, float radius) {
        PacketDistributor.sendToServer(new EnderDragonLaserBeamDestroyBlockPacket(pos, radius));
    }
}
