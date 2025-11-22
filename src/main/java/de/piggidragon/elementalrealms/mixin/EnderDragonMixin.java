package de.piggidragon.elementalrealms.mixin;

import de.piggidragon.elementalrealms.packets.custom.DragonLaserBeamPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Mixin to add a custom laser beam attack to the Ender Dragon.
 * Tracks players who stand still and fires a beam at them.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob {

    @Final
    @Shadow public EnderDragonPart head;

    @Unique
    private final Map<UUID, Vec3> elementalrealms_neoforge_1_21_1$lastPlayerPositions = new HashMap<>();
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$playerStandStillTicks = new HashMap<>();
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$playerLaserCooldown = new HashMap<>();

    protected EnderDragonMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Injects into the dragon's AI tick to handle the laser beam mechanic.
     *
     * @param ci Callback info
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void elementalRealms$onAiStep(CallbackInfo ci) {
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level();

        elementalrealms_neoforge_1_21_1$playerLaserCooldown.replaceAll((uuid, cooldown) -> Math.max(0, cooldown - 1));

        List<ServerPlayer> players = serverLevel.getPlayers(player ->
                player.distanceToSqr(this) < 100 * 100 &&
                        player.isAlive() &&
                        !player.isCreative() &&
                        !player.isSpectator()
        );

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            Vec3 currentPos = player.position();
            Vec3 lastPos = elementalrealms_neoforge_1_21_1$lastPlayerPositions.get(playerId);
            boolean moved = lastPos != null && currentPos.distanceToSqr(lastPos) > 0.01;

            if (moved) {
                elementalrealms_neoforge_1_21_1$playerStandStillTicks.put(playerId, 0);
            } else {
                int ticks = elementalrealms_neoforge_1_21_1$playerStandStillTicks.getOrDefault(playerId, 0) + 1;
                elementalrealms_neoforge_1_21_1$playerStandStillTicks.put(playerId, ticks);

                if (ticks >= 100 && hasLineOfSight(player) && elementalrealms_neoforge_1_21_1$playerLaserCooldown.getOrDefault(playerId, 0) == 0) {
                    startLaserBeam(serverLevel, player);
                    elementalrealms_neoforge_1_21_1$playerStandStillTicks.put(playerId, 0);
                    elementalrealms_neoforge_1_21_1$playerLaserCooldown.put(playerId, 200);
                }
            }

            elementalrealms_neoforge_1_21_1$lastPlayerPositions.put(playerId, currentPos);
        }

        List<UUID> playerIds = players.stream().map(Player::getUUID).collect(Collectors.toList());
        elementalrealms_neoforge_1_21_1$lastPlayerPositions.keySet().retainAll(playerIds);
        elementalrealms_neoforge_1_21_1$playerStandStillTicks.keySet().retainAll(playerIds);
        elementalrealms_neoforge_1_21_1$playerLaserCooldown.keySet().retainAll(playerIds);
    }

    /**
     * Starts a new laser beam task targeting a player.
     *
     * @param level The server level
     * @param target The target player
     */
    @Unique
    private void startLaserBeam(ServerLevel level, ServerPlayer target) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        Vec3 startPos = this.head.position();
        Vec3 endPos = target.position().add(0, target.getEyeHeight() / 2, 0);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 5.0F, 0.8F);

        PacketDistributor.sendToPlayer(target,
                new DragonLaserBeamPacket(dragon.getId(), startPos, endPos));

        level.sendParticles(ParticleTypes.PORTAL, startPos.x, startPos.y, startPos.z,
                20, 0.5, 0.5, 0.5, 0.1);
    }
}

