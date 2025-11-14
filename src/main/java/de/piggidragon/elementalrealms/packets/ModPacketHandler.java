package de.piggidragon.elementalrealms.packets;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.custom.ParticleHitEntityPacket;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.guis.menus.custom.AffinityBookMenu;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.packets.custom.AffinitySuccessPacket;
import de.piggidragon.elementalrealms.packets.custom.OpenAffinityBookPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registers and handles custom network packets.
 * All packet handlers must enqueue work on main thread for thread safety.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class ModPacketHandler {

    /**
     * Registers all mod packets with network system.
     */
    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        // Get the registrar for this mod
        var registrar = event.registrar(ElementalRealms.MODID);

        // Register AffinitySuccessPacket (Server -> Client)
        registrar.playToClient(
                AffinitySuccessPacket.TYPE,
                AffinitySuccessPacket.STREAM_CODEC,
                ModPacketHandler::handleAffinitySuccess
        );

        // Register OpenAffinityBookPacket (Client -> Server)
        registrar.playToServer(
                OpenAffinityBookPacket.TYPE,
                OpenAffinityBookPacket.STREAM_CODEC,
                ModPacketHandler::handleOpenAffinityBook
        );

        registrar.playToServer(
                ParticleHitEntityPacket.TYPE,
                ParticleHitEntityPacket.STREAM_CODEC,
                ModPacketHandler::handleParticleHitEntity
        );
    }

    private static void handleParticleHitEntity(ParticleHitEntityPacket particleHitEntityPacket, IPayloadContext iPayloadContext) {
        iPayloadContext.enqueueWork(() -> {
            if (iPayloadContext.player() instanceof ServerPlayer serverPlayer) {
                Level level = serverPlayer.level();
                Entity hitEntity = level.getEntity(particleHitEntityPacket.hitEntityID());
                if (hitEntity instanceof LivingEntity targetEntity) {

                    Holder<DamageType> damageTypeHolder = serverPlayer.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.DRAGON_BREATH);

                    DamageSource damageSource = new DamageSource(damageTypeHolder, null, serverPlayer);

                    float newHealth = targetEntity.getHealth() - particleHitEntityPacket.damageAmount();
                    targetEntity.setHealth(Math.max(0, newHealth));
                    if (targetEntity.getHealth() <= 0) {
                        targetEntity.die(damageSource);
                    }

                }
            }
        });
    }

    /**
     * Handles affinity success packet on client side.
     * Triggers totem animation and spawns affinity-colored particles.
     *
     * @param packet  Received packet with item and affinity data
     * @param context Network context for thread-safe execution
     */
    private static void handleAffinitySuccess(AffinitySuccessPacket packet, IPayloadContext context) {
        // Execute on main thread to prevent concurrent modification
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.player != null) {
                    // Show totem pop animation with affinity stone
                    minecraft.gameRenderer.displayItemActivation(packet.itemStack());

                    // Spawn additional particles for visual feedback
                    showClientParticles(minecraft.level, minecraft.player, packet.affinity());
                }
            }
        });
    }

    /**
     * Handles opening the affinity book on server side.
     * Retrieves player affinity data and opens the menu.
     *
     * @param packet  Empty packet (no data needed)
     * @param context Network context for thread-safe execution
     */
    private static void handleOpenAffinityBook(OpenAffinityBookPacket packet, IPayloadContext context) {
        // Execute on main thread to prevent concurrent modification
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {

                Map<Affinity, Integer> affinityCompletionMap = serverPlayer.getData(ModAttachments.AFFINITIES.get());
                List<AffinityBookMenu.AffinityData> affinities = new ArrayList<>();

                for (Map.Entry<Affinity, Integer> entry : affinityCompletionMap.entrySet()) {
                    affinities.add(new AffinityBookMenu.AffinityData(entry.getKey(), entry.getValue()));
                }

                // Open the menu
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, player) ->
                                new AffinityBookMenu(containerId, affinities),
                        Component.translatable("gui.elementalrealms.affinity_book.title")
                ), buf -> {
                    // Write affinity data to buffer for client
                    buf.writeInt(affinities.size());
                    for (AffinityBookMenu.AffinityData data : affinities) {
                        buf.writeEnum(data.affinity());
                        buf.writeInt(data.completionPercent());
                    }
                });
            }
        });
    }

    /**
     * Spawns client-side particles matching affinity type.
     * Creates random pattern around player for visual appeal.
     *
     * @param level    Client level for particle spawning
     * @param player   Player at center of effect
     * @param affinity Affinity determining particle type
     */
    private static void showClientParticles(Level level, Player player, Affinity affinity) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5);
            double offsetY = level.random.nextDouble() * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5);

            switch (affinity) {
                case FIRE -> level.addParticle(ParticleTypes.FLAME,
                        player.getX() + offsetX,
                        player.getY() + 0.8 + offsetY,
                        player.getZ() + offsetZ,
                        0.0, 0.05, 0.0);

                case ICE -> level.addParticle(ParticleTypes.SNOWFLAKE,
                        player.getX() + offsetX,
                        player.getY() + 0.8 + offsetY,
                        player.getZ() + offsetZ,
                        offsetX * 0.02, -0.02, offsetZ * 0.02);

                default -> level.addParticle(ParticleTypes.ENCHANT,
                        player.getX() + offsetX,
                        player.getY() + 0.8 + offsetY,
                        player.getZ() + offsetZ,
                        offsetX * 0.05, offsetY * 0.02, offsetZ * 0.05);
            }
        }
    }
}
