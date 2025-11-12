package de.piggidragon.elementalrealms.registries.items.magic.affinities.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.packets.AffinitySuccessPacket;
import de.piggidragon.elementalrealms.particles.AffinityParticles;
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
        ElementalRealms.LOGGER.debug("Using AffinityStone of type: " + this.affinity.name());
        ItemStack itemStack = player.getItemInHand(hand);

        // Only execute on server side
        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemStack);
        }

        ItemStack originalItemStack = itemStack.copy();
        boolean success = false;

        ElementalRealms.LOGGER.debug("Processing AffinityStone for player: " + serverPlayer.getName().getString());
        // Void stone clears all affinities
        if (this.affinity == Affinity.VOID) {
            try {
                ModAffinities.clearAffinities(serverPlayer);
                success = true;
                itemStack.shrink(1);
            } catch (IllegalStateException e) {
                serverPlayer.displayClientMessage(
                        Component.literal(e.getMessage()).withStyle(style -> style.withColor(0xFF0000)),
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
                        Component.literal(e.getMessage()).withStyle(style -> style.withColor(0xFF0000)),
                        true
                );
                return InteractionResultHolder.fail(itemStack);
            }
        }

        if (success) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Spawn colored particles
            AffinityParticles.createCustomAffinityParticles(serverLevel, serverPlayer, this.affinity);

            // Play sound with pitch varying by affinity
            float pitch = 0.25F + (this.affinity.ordinal() * 0.1F);
            serverLevel.playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvents.TOTEM_USE,
                    SoundSource.PLAYERS,
                    0.8F,
                    pitch
            );

            // Send packet to client for additional effects
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new AffinitySuccessPacket(originalItemStack, this.affinity)
            );

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.fail(itemStack);
    }

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
