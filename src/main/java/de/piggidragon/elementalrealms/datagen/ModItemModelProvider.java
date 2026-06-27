package de.piggidragon.elementalrealms.datagen;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.AffinityItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates flat 2D item models for all affinity stones and shards.
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ElementalRealms.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        AffinityItems.AFFINITY_STONES.values().forEach(item -> basicItem(item.get()));
        AffinityItems.AFFINITY_SHARDS.values().forEach(item -> basicItem(item.get()));
    }

    @Override
    public ItemModelBuilder basicItem(Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "item/" + name);
        return withExistingParent(name, "item/generated").texture("layer0", texture);
    }
}
