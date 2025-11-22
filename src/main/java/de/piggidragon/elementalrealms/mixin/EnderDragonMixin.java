package de.piggidragon.elementalrealms.mixin;

import de.piggidragon.elementalrealms.packets.custom.DragonLaserBeamPacket;
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
 * Mixin to add a custom laser beam attack to the Ender Dragon.
 * Enforces constant movement by tracking player positions over intervals.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob {

    /**
     * Accessor for the dragon's head part.
     */
    @Final
    @Shadow public EnderDragonPart head;

    /**
     * Stores the reference position of the player at the start of a check interval.
     */
    @Unique
    private final Map<UUID, Vec3> elementalrealms_neoforge_1_21_1$anchorPositions = new HashMap<>();

    /**
     * Counts the ticks since the last position check.
     */
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$checkTimer = new HashMap<>();

    /**
     * Cooldown timer for the laser attack per player.
     */
    @Unique
    private final Map<UUID, Integer> elementalrealms_neoforge_1_21_1$playerLaserCooldown = new HashMap<>();

    /**
     * The interval in ticks to check for player movement (60 ticks = 3 seconds).
     */
    @Unique
    private static final int CHECK_INTERVAL = 60;

    /**
     * The radius in blocks the player must move outside of within the interval.
     */
    @Unique
    private static final double CHECK_RADIUS = 3.0;

    /**
     * Constructor matching the super class.
     *
     * @param entityType The entity type
     * @param level      The level
     */
    protected EnderDragonMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Injects into the dragon's AI tick to handle the laser beam mechanic.
     * Checks if players have moved enough from their position x seconds ago.
     *
     * @param ci Callback info
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void elementalRealms$onAiStep(CallbackInfo ci) {
        if (this.level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) this.level();

        // Decrease cooldowns
        elementalrealms_neoforge_1_21_1$playerLaserCooldown.replaceAll((uuid, cooldown) -> Math.max(0, cooldown - 1));

        // Find valid targets nearby
        List<ServerPlayer> players = serverLevel.getPlayers(player ->
                player.distanceToSqr(this) < 100 * 100 &&
                        player.isAlive() &&
                        !player.isCreative() &&
                        !player.isSpectator()
        );

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();
            Vec3 currentPos = player.position();

            // Initialize tracking if new player
            if (!elementalrealms_neoforge_1_21_1$anchorPositions.containsKey(playerId)) {
                elementalrealms_neoforge_1_21_1$anchorPositions.put(playerId, currentPos);
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, 0);
                continue;
            }

            int timer = elementalrealms_neoforge_1_21_1$checkTimer.getOrDefault(playerId, 0);
            timer++;

            if (timer >= CHECK_INTERVAL) {
                // Interval reached, check distance to anchor position
                Vec3 anchorPos = elementalrealms_neoforge_1_21_1$anchorPositions.get(playerId);
                double distSqr = currentPos.distanceToSqr(anchorPos);

                // If player is still within the radius of the anchor position
                if (distSqr < CHECK_RADIUS * CHECK_RADIUS) {
                    if (hasLineOfSight(player) && elementalrealms_neoforge_1_21_1$playerLaserCooldown.getOrDefault(playerId, 0) == 0) {
                        startLaserBeam(serverLevel, player);
                        elementalrealms_neoforge_1_21_1$playerLaserCooldown.put(playerId, 100); // 5 seconds cooldown before next possible hit
                    }
                }

                // Reset the cycle: new anchor is current position, reset timer
                elementalrealms_neoforge_1_21_1$anchorPositions.put(playerId, currentPos);
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, 0);
            } else {
                elementalrealms_neoforge_1_21_1$checkTimer.put(playerId, timer);
            }
        }

        // Cleanup data for players no longer relevant
        List<UUID> playerIds = players.stream().map(Player::getUUID).collect(Collectors.toList());
        elementalrealms_neoforge_1_21_1$anchorPositions.keySet().retainAll(playerIds);
        elementalrealms_neoforge_1_21_1$checkTimer.keySet().retainAll(playerIds);
        elementalrealms_neoforge_1_21_1$playerLaserCooldown.keySet().retainAll(playerIds);
    }

    /**
     * Starts a new laser beam task targeting a player.
     * Calculates sound position relative to the beam for all nearby players to simulate a linear sound source.
     *
     * @param level  The server level
     * @param target The target player
     */
    @Unique
    private void startLaserBeam(ServerLevel level, ServerPlayer target) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        Vec3 startPos = this.head.position();
        Vec3 endPos = target.position().add(0, target.getEyeHeight() / 2, 0);

        // Calculate beam vector for sound projection
        Vec3 beamDir = endPos.subtract(startPos);
        double beamLengthSqr = beamDir.lengthSqr();

        // Iterate over all players in the level to play sound correctly relative to the beam
        for (ServerPlayer player : level.players()) {
            Vec3 playerPos = player.position();

            // Calculate the closest point on the beam segment to the player
            double t = 0;
            if (beamLengthSqr > 0) {
                // Project vector AP onto AB to find the scalar projection t
                t = (playerPos.subtract(startPos).dot(beamDir)) / beamLengthSqr;
                // Clamp t to the segment [0, 1]
                t = Mth.clamp(t, 0, 1);
            }
            Vec3 closestPointOnBeam = startPos.add(beamDir.scale(t));

            // Check if the player is within hearing range of the closest point on the beam
            // Volume 5.0F roughly corresponds to ~80 blocks. Using 100 blocks squared as a safe check.
            if (playerPos.distanceToSqr(closestPointOnBeam) < 100 * 100) {
                // Send a sound packet specifically to this player, positioning the sound at the closest point on the beam
                // This ensures the sound volume attenuates based on the distance to the beam itself, not just the source
                player.connection.send(new ClientboundSoundPacket(
                        Holder.direct(ModSounds.LASER_BEAM.get()),
                        SoundSource.HOSTILE,
                        closestPointOnBeam.x, closestPointOnBeam.y, closestPointOnBeam.z,
                        5.0F, 1.0F,
                        level.getRandom().nextLong()
                ));
            }
        }

        // Send the visual packet to the target (or all players if needed, but logic here targets specific player)
        // Note: DragonLaserBeamPacket likely handles rendering for the client
        PacketDistributor.sendToPlayer(target,
                new DragonLaserBeamPacket(dragon.getId(), startPos, endPos));

        level.sendParticles(ParticleTypes.PORTAL, startPos.x, startPos.y, startPos.z,
                20, 0.5, 0.5, 0.5, 0.1);
    }
}