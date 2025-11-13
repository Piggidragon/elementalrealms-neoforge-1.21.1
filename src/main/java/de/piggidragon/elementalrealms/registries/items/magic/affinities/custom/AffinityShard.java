package de.piggidragon.elementalrealms.registries.items.magic.affinities.custom;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
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

import java.util.List;

/**
 * Consumable item that grants or removes player affinities.
 * Regular stones add specific affinities, void stone clears all.
 */
public class AffinityShard extends Item {

    private final Affinity affinity;

    /**
     * Creates a new affinity stone.
     *
     * @param properties Item properties including rarity
     * @param affinity   The affinity this stone grants or manages
     */
    public AffinityShard(Properties properties, Affinity affinity) {
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

        // Regular shards add incremental affinity progress
        try {
            ModAffinities.addIncrementAffinity(serverPlayer, this.affinity, 5);
            itemStack.shrink(1);
            return InteractionResultHolder.success(itemStack);
        } catch (IllegalStateException e) {
            serverPlayer.displayClientMessage(
                    Component.literal(e.getMessage()).withStyle(style -> style.withColor(0xFF0000)),
                    true
            );
            return InteractionResultHolder.fail(itemStack);
        }
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
        String tooltipKey = "itemtooltip.elementalrealms.affinity_shard."
                + this.affinity.name().toLowerCase();
        tooltipComponents.add(Component.translatable(tooltipKey));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
