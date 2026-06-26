package de.piggidragon.elementalrealms.registries.creativetabs;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.blocks.ModBlocks;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.AffinityItems;
import de.piggidragon.elementalrealms.registries.items.magic.equipment.hand.HandEquipmentItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative mode tabs for mod content.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "elementalrealms");
    public static final Supplier<CreativeModeTab> AFFINITY_TAB = CREATIVE_MODE_TABS.register("affinity_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AffinityItems.AFFINITY_STONES.get(Affinity.SPACE).get()))
                    .title(Component.translatable("itemGroup.elementalrealms.affinity_tab"))
                    .displayItems((params, output) ->
                            AffinityItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build()
    );
    public static final Supplier<CreativeModeTab> ITEM_TAB = CREATIVE_MODE_TABS.register("item_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(HandEquipmentItems.DIMENSION_STAFF.get()))
                    .title(Component.translatable("itemGroup.elementalrealms.item_tab"))
                    .displayItems((params, output) ->
                            HandEquipmentItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build()
    );
    public static final Supplier<CreativeModeTab> BLOCK_TAB = CREATIVE_MODE_TABS.register("block_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.DIRT))
                    .title(Component.translatable("itemGroup.elementalrealms.block_tab"))
                    .displayItems((params, output) ->
                            ModBlocks.BLOCKS.getEntries().forEach(block -> output.accept(block.get())))
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
