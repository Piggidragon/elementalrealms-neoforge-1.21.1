package de.piggidragon.elementalrealms.items.magic.dimension.custom;

import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.particles.DimensionStaffParticles;
import de.piggidragon.elementalrealms.particles.PortalParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Staff that creates temporary portals to School dimension via beam animation.
 */
public class SchoolStaff extends Item {

    // Active animations tracked by player UUID
    private static final Map<UUID, BeamAnimation> ACTIVE_ANIMATIONS = new HashMap<>();

    /**
     * Creates the school staff item.
     *
     * @param properties Item properties including durability
     */
    public SchoolStaff(Properties properties) {
        super(properties);
    }

    /**
     * Updates all active beam animations. Must be called from server tick event.
     */
    public static void tickAnimations() {
        ACTIVE_ANIMATIONS.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    /**
     * Spawns a portal entity at target position.
     *
     * @param level          The world level
     * @param player         Owner of the portal
     * @param targetPosition Position to spawn portal at
     */
    private static void spawnPortal(Level level, Player player, Vec3 targetPosition) {
        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                level,
                true,
                200,
                ModLevel.SCHOOL_DIMENSION,
                player.getUUID()
        );

        portal.setPos(targetPosition.x, targetPosition.y, targetPosition.z);
        portal.setYRot(player.getYRot());
        level.addFreshEntity(portal);
    }

    /**
     * Removes all portals owned by player with disappear effects.
     *
     * @param level  The world level
     * @param player Portal owner
     */
    private static void removeOldPortals(Level level, Player player) {
        List<PortalEntity> portals = level.getEntitiesOfClass(
                PortalEntity.class,
                player.getBoundingBox().inflate(1000),
                portal -> portal.getOwnerUUID() != null && portal.getOwnerUUID().equals(player.getUUID())
        );

        for (PortalEntity portal : portals) {
            PortalParticles.createPortalDisappearEffect((ServerLevel) level, portal.position());
            level.playSound(null, portal,
                    SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1, 0.7f);
            portal.discard();
        }
    }

    /**
     * Handles staff usage to create portal beam animation.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Restrict to vanilla dimensions
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER && level.dimension() != Level.END) {
            player.displayClientMessage(Component.literal("Can't use this here..."), true);
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Calculate beam start at staff tip (in front of player eyes)
            Vec3 staffTip = player.getEyePosition().add(
                    player.getLookAngle().scale(0.8)
            );

            // Calculate target position 2 blocks ahead at torso height
            Vec3 lookVec = player.getLookAngle();
            double distance = 2.0;
            Vec3 targetPos = new Vec3(
                    player.getX() + lookVec.x * distance,
                    player.getY() + 0.5,
                    player.getZ() + lookVec.z * distance
            );

            // Remove existing portals before creating new one
            removeOldPortals(level, player);

            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS,
                    0.7F, 1.5F);

            DimensionStaffParticles.addDurabilityEffects(serverLevel, player, player.getMainHandItem());

            // Start beam animation (prevents multiple casts by same player)
            ACTIVE_ANIMATIONS.put(player.getUUID(), new BeamAnimation(serverLevel, player, staffTip, targetPos));

            // Damage staff
            player.getMainHandItem().hurtAndBreak(1, serverLevel, player,
                    item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

            player.getCooldowns().addCooldown(player.getMainHandItem(), 0);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        // Show detailed tooltip when Shift is held, otherwise show hint
        if (flag.hasShiftDown()) {
            tooltipAdder.accept(Component.translatable("itemtooltip.elementalrealms.dimension_staff.line1"));
            tooltipAdder.accept(Component.translatable("itemtooltip.elementalrealms.dimension_staff.line2"));
        } else {
            tooltipAdder.accept(Component.translatable("itemtooltip.elementalrealms.shift"));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    /**
     * Handles spiraling beam particle animation from staff to portal spawn point.
     */
    private static class BeamAnimation {
        final ServerLevel level;
        final Player player;
        final Vec3 startPos;
        final Vec3 targetPos;
        final Vec3 direction;
        final double stepSize;
        final int totalTicks = 40; // 2 seconds
        int currentTick = 0;

        /**
         * Initializes beam animation parameters.
         *
         * @param level     Server level for particle spawning
         * @param player    Player who cast the beam
         * @param startPos  Start position at staff tip
         * @param targetPos End position where portal spawns
         */
        BeamAnimation(ServerLevel level, Player player, Vec3 startPos, Vec3 targetPos) {
            this.level = level;
            this.player = player;
            this.startPos = startPos;
            this.targetPos = targetPos;
            this.direction = targetPos.subtract(startPos).normalize();
            this.stepSize = startPos.distanceTo(targetPos) / totalTicks;
        }

        /**
         * Advances animation by one tick, spawning spiral particles along beam.
         *
         * @return true to continue animation, false when complete
         */
        boolean tick() {
            if (currentTick > totalTicks) {
                return false;
            }

            // Calculate current position along beam
            double currentDistance = currentTick * stepSize;
            Vec3 currentPos = startPos.add(direction.scale(currentDistance));

            // Create spiral with 3 rotating particles
            for (int i = 0; i < 3; i++) {
                double angle = (currentTick * 0.3 + i * (Math.PI * 2 / 3));
                double spiralRadius = 0.3;

                double offsetX = Math.cos(angle) * spiralRadius;
                double offsetZ = Math.sin(angle) * spiralRadius;

                // Spawn purple portal particles in spiral formation
                level.sendParticles(
                        ParticleTypes.PORTAL,
                        currentPos.x + offsetX,
                        currentPos.y,
                        currentPos.z + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.02
                );
            }

            // Add witch particles every 3 ticks for mystical effect
            if (currentTick % 3 == 0) {
                level.sendParticles(
                        ParticleTypes.WITCH,
                        currentPos.x, currentPos.y, currentPos.z,
                        2, 0.1, 0.1, 0.1, 0.01
                );
            }

            // Spawn portal when beam reaches target
            if (currentTick == totalTicks) {
                PortalParticles.createPortalArrivalEffect(level, targetPos);
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                        SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS,
                        0.4F, 0.6F);
                spawnPortal(level, player, targetPos);
                return false;
            }

            currentTick++;
            return true;
        }
    }
}
