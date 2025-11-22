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
 * Generates item model JSON files.
 */
public class ModItemModelProvider extends ItemModelProvider {

    /**
     * Creates the model provider.
     *
     * @param output             Pack output handler
     * @param existingFileHelper Helper for checking existing files
     */
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ElementalRealms.MODID, existingFileHelper);
    }

    /**
     * Registers models for items.
     */
    @Override
    protected void registerModels() {
        // Generate flat 2D models for all affinity stones
        AffinityItems.AFFINITY_STONES.values().forEach(item ->
                basicItem(item.get())
        );

        // Generate flat 2D models for all affinity shards
        AffinityItems.AFFINITY_SHARDS.values().forEach(item ->
                basicItem(item.get())
        );
    }

    /**
     * Helper method to create a basic item model with texture.
     *
     * @param item The item to generate a model for
     * @return
     */
    @Override
    public ItemModelBuilder basicItem(Item item) {
        ResourceLocation itemLocation = ResourceLocation.fromNamespaceAndPath(
                ElementalRealms.MODID,
                "item/" + getItemName(item)
        );

        withExistingParent(getItemName(item), "item/generated")
                .texture("layer0", itemLocation);
        return null;
    }

    /**
     * Gets the registry name of an item without namespace.
     *
     * @param item The item
     * @return The item's name
     */
    private String getItemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }
}
