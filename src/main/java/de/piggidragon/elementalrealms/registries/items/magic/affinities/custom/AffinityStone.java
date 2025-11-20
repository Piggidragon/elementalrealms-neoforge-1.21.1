package de.piggidragon.elementalrealms.registries.items.magic.affinities.custom;

import de.piggidragon.elementalrealms.client.particles.vanilla.AffinityParticles;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.packets.custom.AffinitySuccessPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Consumable item that grants or removes player affinities.
 * Regular stones add specific affinities, void stone clears all.
 */
public class AffinityStone extends Item {

    // Sound effect constants
    private static final float SOUND_VOLUME = 0.5F;
    private static final float VOID_SOUND_PITCH = 0.8F;
    private static final float BASE_STONE_VOLUME = 0.25F;
    private static final float BASE_STONE_PITCH = 0.25F;
    private static final float PITCH_INCREMENT = 0.1F; // Pitch increase per affinity ordinal
    private static final int ERROR_MESSAGE_COLOR = 0xFF0000; // Red color for error messages

    private final Affinity affinity;

    /**
     * Creates a new affinity stone.
     *
     * @param properties Item properties including rarity
     * @param affinity   The affinity this stone grants or manages
     */
    public AffinityStone(Properties properties, Affinity affinity) {
        super(properties);
        this.affinity = affinity;
    }

    /**
     * Called when the player right-clicks with the item in hand
     * Handles affinity addition/removal logic
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Only execute on server side
        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemStack);
        }

        ItemStack originalItemStack = itemStack.copy();
        boolean success;

        // Void stone clears all affinities
        if (this.affinity == Affinity.VOID) {
            try {
                ModAffinities.clearAffinities(serverPlayer);
                success = true;
                itemStack.shrink(1);
            } catch (IllegalStateException e) {
                serverPlayer.displayClientMessage(
                        Component.literal(e.getMessage()).withStyle(style -> style.withColor(ERROR_MESSAGE_COLOR)),
                        true
                );
                return InteractionResultHolder.fail(itemStack);
            }
        } else {
            // Regular stones add specific affinity
            try {
                ModAffinities.addAffinity(serverPlayer, this.affinity);
                success = true;
                itemStack.shrink(1);
            } catch (IllegalStateException e) {
                serverPlayer.displayClientMessage(
                        Component.literal(e.getMessage()).withStyle(style -> style.withColor(ERROR_MESSAGE_COLOR)),
                        true
                );
                return InteractionResultHolder.fail(itemStack);
            }
        }

        if (success) {
            ServerLevel serverLevel = serverPlayer.serverLevel();

            // Spawn colored particles
            AffinityParticles.createCustomAffinityParticles(serverLevel, serverPlayer, this.affinity);

            if (affinity == Affinity.VOID) {
                player.playNotifySound(
                        SoundEvents.AMETHYST_CLUSTER_BREAK,
                        SoundSource.PLAYERS,
                        SOUND_VOLUME,
                        VOID_SOUND_PITCH
                );
            } else {
                float pitch = BASE_STONE_PITCH + (this.affinity.ordinal() * PITCH_INCREMENT);
                player.playNotifySound(
                        SoundEvents.TOTEM_USE,
                        SoundSource.PLAYERS,
                        BASE_STONE_VOLUME,
                        pitch
                );
            }

            // Send packet to client for additional effects
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new AffinitySuccessPacket(originalItemStack, this.affinity)
            );

            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    // Adds tooltip showing affinity type and description
    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        // Add affinity-specific description
        String tooltipKey = "itemtooltip.elementalrealms.affinity_stone."
                + this.affinity.name().toLowerCase();
        tooltipComponents.add(Component.translatable(tooltipKey));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
