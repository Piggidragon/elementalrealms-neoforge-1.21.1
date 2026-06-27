package de.piggidragon.elementalrealms.registries.items.magic.affinities.custom;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Consumable shard that adds 5% completion to its affinity per use.
 */
public class AffinityShard extends Item {

    private static final int AFFINITY_INCREMENT = 5;
    private static final int ERROR_MESSAGE_COLOR = 0xFF0000;

    private final Affinity affinity;

    public AffinityShard(Properties properties, Affinity affinity) {
        super(properties);
        this.affinity = affinity;
    }

    /**
     * Adds {@link #AFFINITY_INCREMENT} (5%) to this shard's affinity on the player. The
     * shard is consumed on success. Failure (already at max, missing base for a Deviant
     * shard, etc.) is reported via a red action-bar message and the shard is preserved.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(itemStack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemStack);
        }

        try {
            ModAffinities.addIncrementAffinity(serverPlayer, affinity, AFFINITY_INCREMENT);
            itemStack.shrink(1);
            return InteractionResultHolder.success(itemStack);
        } catch (IllegalStateException e) {
            serverPlayer.displayClientMessage(
                    Component.literal(e.getMessage()).withStyle(style -> style.withColor(ERROR_MESSAGE_COLOR)),
                    true
            );
            return InteractionResultHolder.fail(itemStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String key = "itemtooltip.elementalrealms.affinity_shard." + affinity.name().toLowerCase();
        tooltipComponents.add(Component.translatable(key));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
