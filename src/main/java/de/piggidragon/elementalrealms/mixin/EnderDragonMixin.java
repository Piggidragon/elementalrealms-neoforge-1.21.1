package de.piggidragon.elementalrealms.mixin;

import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamPacket;
import de.piggidragon.elementalrealms.registries.configs.DragonConfig;
import de.piggidragon.elementalrealms.registries.sounds.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adds a stationary-player laser attack to the Ender Dragon.
 * Players who stay within {@link DragonConfig#laserCheckRadius()} blocks for
 * {@link DragonConfig#laserCheckIntervalTicks()} ticks get shot. All tunables
 * come from {@code dragon.json} so balancing does not require a recompile.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob {

    @Unique
    private final Map<UUID, Vec3> elementalrealms_neoforge_1_21_1$anchorPositions = new HashMap<>();
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$checkTimer = new HashMap<>();
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$playerLaserCooldown = new HashMap<>();

    @Final
    @Shadow
    public EnderDragonPart head;

    protected EnderDragonMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void elementalRealms$onAiStep(CallbackInfo ci) {
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level();

        double checkRadius = DragonConfig.laserCheckRadius();
        double detectionRange = DragonConfig.laserDetectionRange();
        int checkInterval = DragonConfig.laserCheckIntervalTicks();
        int cooldownTicks = DragonConfig.laserCooldownTicks();
        double checkRadiusSquared = checkRadius * checkRadius;
        int detectionRangeSquared = (int) (detectionRange * detectionRange);

        elementalrealms_neoforge_1_21_1$playerLaserCooldown.replaceAll((uuid, cooldown) -> Math.max(0, cooldown - 1));

        List<ServerPlayer> players = serverLevel.getPlayers(player ->
                player.distanceToSqr(this) < detectionRangeSquared
                        && player.isAlive()
                        && !player.isCreative()
                        && !player.isSpectator()
        );

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            Vec3 currentPos = player.position();

            if (!elementalrealms_neoforge_1_21_1$anchorPositions.containsKey(playerId)) {
                elementalrealms_neoforge_1_21_1$anchorPositions.put(playerId, currentPos);
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, 0);
                continue;
            }

            int timer = elementalrealms_neoforge_1_21_1$checkTimer.getOrDefault(playerId, 0) + 1;

            if (timer >= checkInterval) {
                Vec3 anchorPos = elementalrealms_neoforge_1_21_1$anchorPositions.get(playerId);

                if (currentPos.distanceToSqr(anchorPos) < checkRadiusSquared
                        && hasLineOfSight(player)
                        && elementalrealms_neoforge_1_21_1$playerLaserCooldown.getOrDefault(playerId, 0) == 0) {
                    startLaserBeam(serverLevel, player);
                    elementalrealms_neoforge_1_21_1$playerLaserCooldown.put(playerId, cooldownTicks);
                }

                elementalrealms_neoforge_1_21_1$anchorPositions.put(playerId, currentPos);
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, 0);
            } else {
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, timer);
            }
        }

        List<UUID> trackedIds = players.stream().map(Player::getUUID).collect(Collectors.toList());
        elementalrealms_neoforge_1_21_1$anchorPositions.keySet().retainAll(trackedIds);
        elementalrealms_neoforge_1_21_1$checkTimer.keySet().retainAll(trackedIds);
        elementalrealms_neoforge_1_21_1$playerLaserCooldown.keySet().retainAll(trackedIds);
    }

    /**
     * Fires a laser beam at {@code target} and notifies all nearby players with the beam sound,
     * positioned at the closest point on the beam to each player for accurate attenuation.
     */
    @Unique
    private void startLaserBeam(ServerLevel level, ServerPlayer target) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        Vec3 startPos = this.head.position();
        Vec3 endPos = target.position().add(0, target.getEyeHeight() / 2, 0);

        Vec3 beamDir = endPos.subtract(startPos);
        double beamLengthSqr = beamDir.lengthSqr();
        double hearingRangeSquared = DragonConfig.laserSoundHearingRange() * DragonConfig.laserSoundHearingRange();

        for (ServerPlayer player : level.players()) {
            Vec3 playerPos = player.position();

            double t = 0;
            if (beamLengthSqr > 0) {
                t = Mth.clamp(playerPos.subtract(startPos).dot(beamDir) / beamLengthSqr, 0, 1);
            }
            Vec3 closestPointOnBeam = startPos.add(beamDir.scale(t));

            if (playerPos.distanceToSqr(closestPointOnBeam) < hearingRangeSquared) {
                player.connection.send(new ClientboundSoundPacket(
                        Holder.direct(ModSounds.LASER_BEAM.get()),
                        SoundSource.HOSTILE,
                        closestPointOnBeam.x, closestPointOnBeam.y, closestPointOnBeam.z,
                        DragonConfig.laserSoundVolume(), DragonConfig.laserSoundPitch(),
                        level.getRandom().nextLong()
                ));
            }
        }

        PacketDistributor.sendToPlayer(target, new EnderDragonLaserBeamPacket(dragon.getId(), startPos, endPos));

        level.sendParticles(ParticleTypes.PORTAL, startPos.x, startPos.y, startPos.z,
                20, 0.5, 0.5, 0.5, 0.1);
    }
}
