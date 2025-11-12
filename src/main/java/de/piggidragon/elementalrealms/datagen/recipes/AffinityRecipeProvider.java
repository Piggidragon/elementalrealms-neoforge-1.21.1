package de.piggidragon.elementalrealms.datagen.recipes;

import de.piggidragon.elementalrealms.registries.items.magic.affinities.AffinityItems;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * Generates crafting recipes for affinity items.
 */
public class AffinityRecipeProvider extends RecipeProvider {

    public AffinityRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Void Stone
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AffinityItems.AFFINITY_STONES.get(Affinity.VOID).get())
                .pattern("PNP")
                .pattern("NSN")
                .pattern("PNP")
                .define('S', Items.NETHER_STAR)
                .define('P', Items.ENDER_PEARL)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_stone", has(AffinityItems.AFFINITY_STONES.get(Affinity.VOID)))
                .save(recipeOutput);

        super.buildRecipes(recipeOutput);
    }
}
