package de.piggidragon.elementalrealms.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Generates block tags for the mod during data generation.
 * Block tags are used for tool requirements, mineable blocks, and other block groupings.
 * Currently empty as no custom blocks require tags yet.
 */
public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    /**
     * Adds blocks to various tags for categorization and behavior.
     * Examples: mineable_with_pickaxe, needs_iron_tool, fences, walls, etc.
     *
     * @param provider Registry lookup provider
     */
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Currently no block tags defined
        // Future examples:
        // - Tool requirements (pickaxe, axe, etc.)
        // - Material tier requirements (iron, diamond, etc.)
        // - Block groupings (fences, walls, logs, etc.)
    }
}