package de.piggidragon.elementalrealms.registries.items.magic.affinities.custom;

import de.piggidragon.elementalrealms.client.particles.vanilla.AffinityParticles;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.packets.custom.AffinitySuccessPacket;
import net.minecraft.network.chat.Component;
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
 * Consumable stone that grants an affinity on use. The VOID stone clears all affinities instead.
 */
public class AffinityStone extends Item {

    private static final float SOUND_VOLUME = 0.5F;
    private static final float VOID_SOUND_PITCH = 0.8F;
    private static final float BASE_STONE_VOLUME = 0.25F;
    private static final float BASE_STONE_PITCH = 0.25F;
    private static final float PITCH_INCREMENT = 0.1F;
    private static final int ERROR_MESSAGE_COLOR = 0xFF0000;

    private final Affinity affinity;

    public AffinityStone(Properties properties, Affinity affinity) {
        super(properties);
        this.affinity = affinity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemStack);
        }

        ItemStack originalStack = itemStack.copy();
        boolean consumed;

        try {
            if (affinity == Affinity.VOID) {
                ModAffinities.clearAffinities(serverPlayer);
            } else {
                ModAffinities.addAffinity(serverPlayer, affinity);
            }
            consumed = true;
        } catch (IllegalStateException e) {
            serverPlayer.displayClientMessage(
                    Component.literal(e.getMessage()).withStyle(style -> style.withColor(ERROR_MESSAGE_COLOR)),
                    true
            );
            return InteractionResultHolder.fail(itemStack);
        }

        itemStack.shrink(1);
        AffinityParticles.createCustomAffinityParticles(serverPlayer.serverLevel(), serverPlayer, affinity);

        if (affinity == Affinity.VOID) {
            player.playNotifySound(SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, SOUND_VOLUME, VOID_SOUND_PITCH);
        } else {
            float pitch = BASE_STONE_PITCH + affinity.ordinal() * PITCH_INCREMENT;
            player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, BASE_STONE_VOLUME, pitch);
        }

        PacketDistributor.sendToPlayer(serverPlayer, new AffinitySuccessPacket(originalStack, affinity));
        return consumed ? InteractionResultHolder.success(itemStack) : InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String key = "itemtooltip.elementalrealms.affinity_stone." + affinity.name().toLowerCase();
        tooltipComponents.add(Component.translatable(key));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
