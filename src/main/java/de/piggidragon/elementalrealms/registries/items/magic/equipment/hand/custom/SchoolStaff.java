package de.piggidragon.elementalrealms.registries.items.magic.equipment.hand.custom;

import de.piggidragon.elementalrealms.client.particles.vanilla.DimensionStaffParticles;
import de.piggidragon.elementalrealms.client.particles.vanilla.PortalParticles;
import de.piggidragon.elementalrealms.registries.configs.SchoolConfig;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.misc.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Staff that opens a temporary portal to the School dimension via a beam animation.
 */
public class SchoolStaff extends Item {

    private static final int BEAM_TICK_LIMIT = SchoolConfig.beamTotalTicks();

    /**
     * Active beam animations tracked by player UUID - thread-safe for server tick access.
     */
    private static final Map<UUID, BeamAnimation> ACTIVE_ANIMATIONS = new ConcurrentHashMap<>();

    public SchoolStaff(Properties properties) {
        super(properties);
    }

    /**
     * Advances all active beam animations. Must be called from the server tick event
     * once per tick; each animation either ticks itself or returns false to signal
     * completion (and is then removed from the map).
     */
    public static void tickAnimations() {
        ACTIVE_ANIMATIONS.entrySet().removeIf(entry -> !entry.getValue().tick());
    }

    /**
     * Spawns a portal at the given position pointing to the School dimension,
     * owned by {@code player} (used for later cleanup of duplicate staff portals).
     */
    private static void spawnPortal(Level level, Player player, Vec3 targetPosition) {
        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                level,
                true,
                SchoolConfig.portalDespawnTicks(),
                ModLevel.SCHOOL_DIMENSION,
                player.getUUID()
        );
        portal.setPos(targetPosition.x, targetPosition.y, targetPosition.z);
        portal.setYRot(player.getYRot());
        level.addFreshEntity(portal);
    }

    /**
     * Removes every portal owned by {@code player} within the configured search radius.
     * Called at the start of each staff use to prevent portal stacking when the player
     * spams right-click; the despawn particles + ender-eye sound give visual feedback.
     */
    private static void removeOldPortals(Level level, Player player) {
        List<PortalEntity> portals = level.getEntitiesOfClass(
                PortalEntity.class,
                player.getBoundingBox().inflate(SchoolConfig.portalSearchRadius()),
                portal -> portal.getOwnerUUID() != null && portal.getOwnerUUID().equals(player.getUUID())
        );

        for (PortalEntity portal : portals) {
            PortalParticles.createPortalDisappearEffect((ServerLevel) level, portal.position());
            level.playSound(null, portal, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1, 0.7f);
            portal.discard();
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER && level.dimension() != Level.END) {
            player.displayClientMessage(Component.literal("Can't use this here..."), true);
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.pass(itemStack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 look = player.getLookAngle();

        Vec3 staffTip = player.getEyePosition().add(look.scale(SchoolConfig.staffTipDistance()));
        Vec3 targetPos = new Vec3(
                player.getX() + look.x * SchoolConfig.portalSpawnDistance(),
                player.getY() + SchoolConfig.portalSpawnHeight(),
                player.getZ() + look.z * SchoolConfig.portalSpawnDistance()
        );

        removeOldPortals(level, player);

        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.5F);
        DimensionStaffParticles.addDurabilityEffects(serverLevel, player, player.getMainHandItem());

        ACTIVE_ANIMATIONS.put(player.getUUID(), new BeamAnimation(serverLevel, player, staffTip, targetPos));

        player.getMainHandItem().hurtAndBreak(1, serverLevel, player,
                item -> player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
        player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 0);
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("itemtooltip.elementalrealms.dimension_staff.line1"));
            tooltipComponents.add(Component.translatable("itemtooltip.elementalrealms.dimension_staff.line2"));
        } else {
            tooltipComponents.add(Component.translatable("itemtooltip.elementalrealms.shift"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    /**
     * Spiraling beam particle animation from staff tip to portal spawn point.
     */
    private static final class BeamAnimation {
        private final ServerLevel level;
        private final Player player;
        private final Vec3 startPos;
        private final Vec3 targetPos;
        private final Vec3 direction;
        private final double stepSize;
        private int currentTick = 0;

        BeamAnimation(ServerLevel level, Player player, Vec3 startPos, Vec3 targetPos) {
            this.level = level;
            this.player = player;
            this.startPos = startPos;
            this.targetPos = targetPos;
            this.direction = targetPos.subtract(startPos).normalize();
            this.stepSize = startPos.distanceTo(targetPos) / SchoolConfig.beamTotalTicks();
        }

        boolean tick() {
            if (currentTick > BEAM_TICK_LIMIT) {
                return false;
            }

            // Walk along the staff-tip -> target-pos line at a constant step size, then emit
            // three spiral arms (cos/sin, 120° apart) at each step. This produces the
            // twisting beam visual without locking the particle count to distance.
            Vec3 currentPos = startPos.add(direction.scale(currentTick * stepSize));

            for (int i = 0; i < 3; i++) {
                double angle = currentTick * 0.3 + i * (Math.PI * 2 / 3);
                double offsetX = Math.cos(angle) * 0.3;
                double offsetZ = Math.sin(angle) * 0.3;

                level.sendParticles(ParticleTypes.PORTAL,
                        currentPos.x + offsetX, currentPos.y, currentPos.z + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.02);
            }

            if (currentTick % 3 == 0) {
                level.sendParticles(ParticleTypes.WITCH,
                        currentPos.x, currentPos.y, currentPos.z,
                        2, 0.1, 0.1, 0.1, 0.01);
            }

            if (currentTick == SchoolConfig.beamTotalTicks()) {
                PortalParticles.createPortalArrivalEffect(level, targetPos);
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                        SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.4F, 0.6F);
                spawnPortal(level, player, targetPos);
                return false;
            }

            currentTick++;
            return true;
        }
    }
}
