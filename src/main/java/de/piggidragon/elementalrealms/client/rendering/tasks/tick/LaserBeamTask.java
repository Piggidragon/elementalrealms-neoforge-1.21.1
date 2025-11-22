package de.piggidragon.elementalrealms.client.rendering.tasks.tick;

import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.client.rendering.tasks.TickTask;
import de.piggidragon.elementalrealms.packets.custom.LaserBeamDestroyBlockPacket;
import de.piggidragon.elementalrealms.packets.custom.LaserBeamHitEntityPacket;
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
 * Laser beam task with frame-based particle spawning using Lodestone.
 * <p>
 * Handles the visual rendering of a laser beam, checks for entity collisions,
 * and creates a destructive sphere at the impact point.
 */
public class LaserBeamTask implements TickTask {

    /**
     * The radius of the destructive bubble at the end of the laser.
     */
    private static final float BUBBLE_RADIUS = 1.5f;
    // Ender Dragon Colors (Neon Purple -> Magenta)
    private static final Color START_COLOR = new Color(180, 0, 255);
    private static final Color END_COLOR = new Color(255, 100, 255);
    // Core Colors (White -> Light Pink)
    private static final Color CORE_COLOR = new Color(255, 255, 255);
    private static final Color CORE_END_COLOR = new Color(255, 200, 255);
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

    /**
     * Constructs a new LaserBeamTask starting from the entity's eye position.
     *
     * @param entity          The entity casting the laser.
     * @param level           The level the laser is in.
     * @param beamRange       The maximum range of the laser.
     * @param densityPerBlock How many particles to spawn per block of length.
     * @param damageAmount    The damage dealt to entities hit.
     * @param lifeTicks       How long the laser task should persist.
     * @param travelTime      Ticks it takes for the laser to extend fully (0 for instant).
     */
    public LaserBeamTask(Entity entity, Level level, double beamRange, int densityPerBlock, float damageAmount, int lifeTicks, int travelTime) {
        this(entity, level, entity.position(), entity.getViewVector(1.0f), beamRange, densityPerBlock, damageAmount, lifeTicks, travelTime);
    }

    /**
     * Constructs a new LaserBeamTask with a custom start position and direction.
     *
     * @param entity          The entity casting the laser (owner).
     * @param level           The level the laser is in.
     * @param customStartPos  The exact starting coordinate of the beam.
     * @param directionVec    The normalized direction vector.
     * @param beamRange       The maximum range of the laser.
     * @param densityPerBlock How many particles to spawn per block of length.
     * @param damageAmount    The damage dealt to entities hit.
     * @param lifeTicks       How long the laser task should persist.
     * @param travelTime      Ticks it takes for the laser to extend fully.
     */
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

    /**
     * Renders the main laser beam particles.
     *
     * @param level     The level to render in.
     * @param startPos  The start of the segment.
     * @param targetPos The end of the segment.
     * @param density   The particle density.
     */
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

    /**
     * Renders a hollow sphere of particles at the laser's hit position.
     * Uses the same color scheme as the beam.
     *
     * @param level  The level to spawn particles in.
     * @param center The center position of the sphere.
     * @param radius The radius of the sphere.
     */
    private static void renderHitSphere(Level level, Vec3 center, float radius) {
        if (!level.isClientSide) return;

        int particleCount = 20; // Particles per tick for the sphere

        WorldParticleBuilder sphereBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setTransparencyData(GenericParticleData.create(0.6f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.3f, 0.0f).build())
                .setColorData(ColorParticleData.create(START_COLOR, END_COLOR).build())
                .setLifetime(10)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        for (int i = 0; i < particleCount; i++) {
            // Generate random point on the surface of the sphere (hollow bubble)
            // Using Gaussian distribution for uniform spherical distribution
            double x = Math.random() - 0.5;
            double y = Math.random() - 0.5;
            double z = Math.random() - 0.5;

            Vec3 dir = new Vec3(x, y, z).normalize().scale(radius);

            // Add some noise so it's not a perfect shell
            double noise = 0.9 + (Math.random() * 0.2);
            Vec3 spawnPos = center.add(dir.scale(noise));

            sphereBuilder.spawn(level, spawnPos.x, spawnPos.y, spawnPos.z);
        }

        // Add a few core particles for the "energy" look inside
        WorldParticleBuilder coreBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.2f, 0.0f).build())
                .setColorData(ColorParticleData.create(CORE_COLOR, CORE_END_COLOR).build())
                .setLifetime(5)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        for (int i = 0; i < 3; i++) {
            Vec3 randomInner = new Vec3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5)
                    .normalize().scale(Math.random() * radius * 0.8);
            Vec3 spawnPos = center.add(randomInner);
            coreBuilder.spawn(level, spawnPos.x, spawnPos.y, spawnPos.z);
        }
    }

    /**
     * Searches for entities within a given bounding box that are hit by the laser.
     *
     * @param level       The level to search in.
     * @param entityOwner The owner of the laser (to ignore self-hits).
     * @param box         The bounding box to search.
     * @return A list of entities hit.
     */
    private static List<Entity> searchEntityBoxHit(Level level, Entity entityOwner, AABB box) {
        List<Entity> entityHitList = new ArrayList<>();
        entityHitList.addAll(level.getEntities(entityOwner, box, e -> e.isAlive() && e != entityOwner));
        return entityHitList;
    }

    /**
     * Executes the per-tick logic for the laser beam.
     * <p>
     * Updates positions, renders particles, handles entity hits, and requests block destruction.
     */
    @Override
    public void tick() {
        if (currentTick > lifeTicks) {
            RenderManager.requestRemoveTickTask(this);
            return;
        }

        updatePositions();

        if (startPos.distanceToSqr(endPos) > 0.01) {
            renderLaser(level, startPos, endPos, densityPerBlock);
            // Render the bubble at the end
            renderHitSphere(level, endPos, BUBBLE_RADIUS);

            // Periodically destroy blocks inside the bubble
            // We do this every 5 ticks to reduce packet spam
            if (currentTick % 5 == 0) {
                requestBlockDestruction(endPos, BUBBLE_RADIUS);
            }
        }

        List<Entity> hitEntity = searchEntityBoxHit(level, entity, new AABB(startPos, endPos).inflate(0.3));
        hitEntity.addAll(searchEntityBoxHit(level, entity, new AABB(endPos, endPos).inflate(2)));

        for (Entity e : hitEntity) {
            if (e != null) {
                PacketDistributor.sendToServer(
                        new LaserBeamHitEntityPacket(e.getId(), damageAmount)
                );
            }
        }

        currentTick++;
    }

    /**
     * Updates the end position of the laser based on travel time and range.
     */
    public void updatePositions() {
        double progress = 1.0;
        if (travelTime > 0) {
            progress = Math.min(1.0, (double) currentTick / travelTime);
        }

        double currentLength = beamRange * progress;
        endPos = startPos.add(directionVec.scale(currentLength));
    }

    /**
     * Sends a packet to the server requesting block destruction at the given location.
     *
     * @param pos    The center position of the destruction.
     * @param radius The radius of destruction.
     */
    private void requestBlockDestruction(Vec3 pos, float radius) {
        PacketDistributor.sendToServer(new LaserBeamDestroyBlockPacket(pos, radius));
    }
}