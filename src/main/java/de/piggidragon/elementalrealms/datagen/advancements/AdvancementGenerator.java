package de.piggidragon.elementalrealms.datagen.advancements;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.AffinityItems;
import de.piggidragon.elementalrealms.registries.items.magic.equipment.hand.HandEquipmentItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generates the mod's advancement tree.
 */
public class AdvancementGenerator implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
        AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                .display(
                        new ItemStack(AffinityItems.AFFINITY_STONES.get(Affinity.SPACE).get()),
                        Component.translatable("advancements.elementalrealms.root.title"),
                        Component.translatable("advancements.elementalrealms.root.description"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "block/cracked_deepslate_tiles"),
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("killed_dragon",
                        KilledTrigger.TriggerInstance.playerKilledEntity(
                                Optional.of(EntityPredicate.Builder.entity().of(EntityType.ENDER_DRAGON).build())
                        )
                )
                .save(consumer, "elementalrealms:root");

        Advancement.Builder.advancement()
                .parent(rootAdvancement)
                .display(
                        new ItemStack(HandEquipmentItems.DIMENSION_STAFF.get()),
                        Component.translatable("advancements.elementalrealms.get_staff.title"),
                        Component.translatable("advancements.elementalrealms.get_staff.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("has_staff",
                        InventoryChangeTrigger.TriggerInstance.hasItems(HandEquipmentItems.DIMENSION_STAFF.get())
                )
                .save(consumer, "elementalrealms:get_staff");
    }
}
