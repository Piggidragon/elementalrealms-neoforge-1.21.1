package de.piggidragon.elementalrealms.packets;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
        event.registrar("elementalrealms")
                .playToClient(
                        AffinitySuccessPacket.TYPE,
                        AffinitySuccessPacket.CODEC,
                        ModPacketHandler::handleAffinitySuccess
                );
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
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
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
