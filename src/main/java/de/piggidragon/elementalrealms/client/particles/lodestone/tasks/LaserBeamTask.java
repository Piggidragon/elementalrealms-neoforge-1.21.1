package de.piggidragon.elementalrealms.client.particles.lodestone.tasks;

import de.piggidragon.elementalrealms.client.particles.lodestone.RenderTask;
import de.piggidragon.elementalrealms.client.particles.lodestone.custom.DragonLaserParticle;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class LaserBeamTask implements RenderTask {

    private final Player player;
    private final Level level;
    private final float beamRange;
    private static final float damageAmount = 10.0f;

    public LaserBeamTask(Player player, Level level, float beamRange) {
        this.player = player;
        this.level = level;
        this.beamRange = beamRange;
    }

    @Override
    public void render(float partialTicks) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(partialTicks);
        Vec3 starPos = eyePos.add(lookVec.scale(1.2));
        Vec3 endPos = eyePos.add(lookVec.scale(beamRange));

        DragonLaserParticle.spawnBeamEffect(
                level,
                starPos,
                endPos
        );

        Entity hitEntity = raycastEntityHit(level, player, starPos, endPos);

        if (hitEntity != null) {
            PacketDistributor.sendToServer(
                    new ParticleHitEntityPacket(hitEntity.getId(), damageAmount)
            );
        }
    }

    private static Entity raycastEntityHit(Level level, Player player, Vec3 start, Vec3 end) {
        AABB searchBox = new AABB(start, end).inflate(1.0D);
        Entity hitEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : level.getEntities(player, searchBox, e -> e.isAlive() && e != player)) {
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
