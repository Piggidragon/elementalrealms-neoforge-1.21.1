package de.piggidragon.elementalrealms.packets;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.client.rendering.tasks.tick.LaserBeamTask;
import de.piggidragon.elementalrealms.datagen.ModDatapackProvider;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.packets.custom.affinities.AffinitiesSuccessPacket;
import de.piggidragon.elementalrealms.packets.custom.affinities.AffinitiesOpenBookPacket;
import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamHitEntityPacket;
import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamPacket;
import de.piggidragon.elementalrealms.packets.custom.enderdragon.EnderDragonLaserBeamDestroyBlockPacket;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.guis.menus.custom.AffinityBookMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registers mod network payloads and implements their handlers.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ModPacketHandler {

    private static final int AFFINITY_BOOK_PARTICLE_COUNT = 10;
    private static final double AFFINITY_BOOK_PARTICLE_SPREAD = 1.0;
    private static final double AFFINITY_BOOK_PARTICLE_Y_OFFSET = 0.8;
    private static final double AFFINITY_BOOK_PARTICLE_Y_BONUS = 1.2;
    private static final int LASER_BEAM_LIFE_TICKS = 110;
    private static final int LASER_BEAM_TRAVEL_TICKS = 2;
    private static final int LASER_BEAM_DENSITY_PER_BLOCK = 10;
    private static final int LASER_BEAM_HIT_EXPANSION = 2;

    private ModPacketHandler() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ElementalRealms.MODID);

        registrar.playToClient(
                AffinitiesSuccessPacket.TYPE,
                AffinitiesSuccessPacket.STREAM_CODEC,
                ModPacketHandler::handleAffinitySuccess
        );
        registrar.playToServer(
                AffinitiesOpenBookPacket.TYPE,
                AffinitiesOpenBookPacket.STREAM_CODEC,
                ModPacketHandler::handleOpenAffinityBook
        );
        registrar.playToServer(
                EnderDragonLaserBeamHitEntityPacket.TYPE,
                EnderDragonLaserBeamHitEntityPacket.STREAM_CODEC,
                ModPacketHandler::handleLaserBeamHitEntity
        );
        registrar.playToClient(
                EnderDragonLaserBeamPacket.TYPE,
                EnderDragonLaserBeamPacket.STREAM_CODEC,
                ModPacketHandler::handleDragonLaserBeam
        );
        registrar.playToServer(
                EnderDragonLaserBeamDestroyBlockPacket.TYPE,
                EnderDragonLaserBeamDestroyBlockPacket.STREAM_CODEC,
                ModPacketHandler::handleLaserBeamDestroyBlock
        );
    }

    private static void handleLaserBeamHitEntity(EnderDragonLaserBeamHitEntityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            Level level = serverPlayer.level();
            if (!(level.getEntity(packet.hitEntityID()) instanceof LivingEntity target)) return;

            Holder<DamageType> damageType = level.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(ModDatapackProvider.LASER);
            DamageSource source = new DamageSource(damageType, null, serverPlayer);

            target.hurt(source, packet.damageAmount());
        });
    }

    private static void handleAffinitySuccess(AffinitiesSuccessPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) return;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return;

            minecraft.gameRenderer.displayItemActivation(packet.itemStack());
            showClientParticles(minecraft.level, minecraft.player, packet.affinity());
        });
    }

    private static void handleOpenAffinityBook(AffinitiesOpenBookPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            Map<Affinity, Integer> completions = serverPlayer.getData(ModAttachments.AFFINITIES.get());
            List<AffinityBookMenu.AffinityData> affinities = new ArrayList<>(completions.size());
            for (Map.Entry<Affinity, Integer> entry : completions.entrySet()) {
                affinities.add(new AffinityBookMenu.AffinityData(entry.getKey(), entry.getValue()));
            }

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, player) ->
                            new AffinityBookMenu(containerId, affinities),
                    Component.translatable("gui.elementalrealms.affinity_book.title")
            ), buf -> {
                buf.writeInt(affinities.size());
                for (AffinityBookMenu.AffinityData data : affinities) {
                    buf.writeEnum(data.affinity());
                    buf.writeInt(data.completionPercent());
                }
            });
        });
    }

    private static void handleDragonLaserBeam(EnderDragonLaserBeamPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) return;
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            if (level == null) return;
            if (!(level.getEntity(packet.dragonId()) instanceof EnderDragon)) return;

            Vec3 direction = packet.endPos().subtract(packet.startPos()).normalize();
            double beamRange = packet.startPos().distanceTo(packet.endPos());

            RenderManager.addTickTask(new LaserBeamTask(
                    level.getEntity(packet.dragonId()),
                    level,
                    packet.startPos(),
                    direction,
                    beamRange,
                    LASER_BEAM_DENSITY_PER_BLOCK,
                    1f,
                    LASER_BEAM_LIFE_TICKS,
                    LASER_BEAM_TRAVEL_TICKS
            ));
        });
    }

    private static void handleLaserBeamDestroyBlock(EnderDragonLaserBeamDestroyBlockPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) return;

            Vec3 center = packet.center();
            float radius = packet.radius();
            int rCeil = (int) Math.ceil(radius);
            BlockPos centerPos = BlockPos.containing(center);

            for (int x = -rCeil; x <= rCeil; x++) {
                for (int y = -rCeil; y <= rCeil; y++) {
                    for (int z = -rCeil; z <= rCeil; z++) {
                        if (x * x + y * y + z * z > radius * radius) continue;

                        BlockPos targetPos = centerPos.offset(x, y, z);
                        BlockState state = serverLevel.getBlockState(targetPos);
                        if (state.isAir() || state.getDestroySpeed(serverLevel, targetPos) < 0) continue;

                        // true = drop items; false = no drops (vaporized). Currently drops loot.
                        serverLevel.destroyBlock(targetPos, true, context.player());
                    }
                }
            }
        });
    }

    private static void showClientParticles(Level level, Player player, Affinity affinity) {
        for (int i = 0; i < AFFINITY_BOOK_PARTICLE_COUNT; i++) {
            double offsetX = level.random.nextDouble() - 0.5;
            double offsetY = level.random.nextDouble() * AFFINITY_BOOK_PARTICLE_Y_BONUS;
            double offsetZ = level.random.nextDouble() - 0.5;

            switch (affinity) {
                case FIRE -> level.addParticle(ParticleTypes.FLAME,
                        player.getX() + offsetX,
                        player.getY() + AFFINITY_BOOK_PARTICLE_Y_OFFSET + offsetY,
                        player.getZ() + offsetZ,
                        0.0, 0.05, 0.0);
                case ICE -> level.addParticle(ParticleTypes.SNOWFLAKE,
                        player.getX() + offsetX,
                        player.getY() + AFFINITY_BOOK_PARTICLE_Y_OFFSET + offsetY,
                        player.getZ() + offsetZ,
                        offsetX * 0.02, -0.02, offsetZ * 0.02);
                default -> level.addParticle(ParticleTypes.ENCHANT,
                        player.getX() + offsetX,
                        player.getY() + AFFINITY_BOOK_PARTICLE_Y_OFFSET + offsetY,
                        player.getZ() + offsetZ,
                        offsetX * 0.05, offsetY * 0.02, offsetZ * 0.05);
            }
        }
    }
}
