package de.piggidragon.elementalrealms.client.rendering.tasks.tick;

import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.client.rendering.tasks.TickTask;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

    // Ender Dragon Colors (Neon Purple -> Magenta)
    private static final Color START_COLOR = new Color(180, 0, 255);
    private static final Color END_COLOR = new Color(255, 100, 255);
    // Core Colors (White -> Light Pink)
    private static final Color CORE_COLOR = new Color(255, 255, 255);
    private static final Color CORE_END_COLOR = new Color(255, 200, 255);

    private int currentTick = 0;

    public LaserBeamTask(Player player, Level level, float beamRange, int densityPerBlock, float damageAmount, int lifeTicks, int travelTime) {
        this.player = player;
        this.level = level;
        this.damageAmount = damageAmount;
        this.beamRange = beamRange;
        this.densityPerBlock = densityPerBlock;
        this.lifeTicks = lifeTicks;
        this.travelTime = travelTime;

        this.directionVec = player.getLookAngle();

        // Startposition leicht anpassen (Augenhöhe + etwas vorwärts)
        this.startPos = player.getEyePosition().add(directionVec.scale(0.5f));
        // Initialisieren, um NullPointer im ersten Render-Pass zu vermeiden
        this.endPos = this.startPos;
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

        Entity hitEntity = raycastEntityHit(level, player, startPos, endPos);
        if (hitEntity != null) {
            // Achtung: Das sendet jeden Tick ein Packet (20x pro Sekunde Schaden).
            // Ggf. hier noch einen Cooldown oder "InvulnerabilityFrame"-Check einbauen.
            PacketDistributor.sendToServer(
                    new ParticleHitEntityPacket(hitEntity.getId(), damageAmount)
            );
        }

        currentTick++;
    }

    public void update() {
        // Berechnung des Fortschritts (0.0 bis 1.0) basierend auf TravelTime
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

        // Sicherheitscheck: Wenn Distanz fast 0 ist, normalize() nicht aufrufen
        if (distance < 0.05) return;

        direction = direction.normalize();
        int particleCount = (int) (distance * density);

        // --- 1. Äußerer Glow (Aura) ---
        WorldParticleBuilder builder = WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setTransparencyData(GenericParticleData.create(0.75f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.45f, 0.0f).build()) // Dicke Aura
                .setColorData(ColorParticleData.create(START_COLOR, END_COLOR).build())
                .setLifetime(12) // Kurzlebig für Animations-Effekt
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .setMotion(0, 0, 0)
                .enableNoClip();

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 spawnPos = startPos.add(direction.scale(distance * progress));

            // Jitter (Zufallswackeln) für elektrischen Effekt
            double jitter = 0.06;
            double oX = (Math.random() - 0.5) * jitter;
            double oY = (Math.random() - 0.5) * jitter;
            double oZ = (Math.random() - 0.5) * jitter;

            builder.spawn(level, spawnPos.x + oX, spawnPos.y + oY, spawnPos.z + oZ);
        }

        // --- 2. Innerer Kern (Heller Strahl) ---
        WorldParticleBuilder coreBuilder = WorldParticleBuilder.create(LodestoneParticleTypes.SPARKLE_PARTICLE)
                .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                .setScaleData(GenericParticleData.create(0.15f, 0.0f).build()) // Dünner Kern
                .setColorData(ColorParticleData.create(CORE_COLOR, CORE_END_COLOR).build())
                .setLifetime(8)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .enableNoClip();

        // Der Kern braucht weniger Partikel
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