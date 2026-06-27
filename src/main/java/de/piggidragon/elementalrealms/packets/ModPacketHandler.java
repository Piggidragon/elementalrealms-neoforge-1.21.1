package de.piggidragon.elementalrealms.packets;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.packets.custom.affinities.AffinitiesSuccessPacket;
import de.piggidragon.elementalrealms.packets.custom.affinities.AffinitiesOpenBookPacket;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.guis.menus.custom.AffinityBookMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
