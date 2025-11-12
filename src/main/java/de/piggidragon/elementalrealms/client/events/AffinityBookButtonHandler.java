package de.piggidragon.elementalrealms.client.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.gui.screens.affinitybook.AffinityBookButton;
import de.piggidragon.elementalrealms.packets.custom.AffinitySuccessPacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Adds the affinity book button next to the recipe book button
 * in screens that have a recipe book (like inventory, crafting table, etc.)
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class AffinityBookButtonHandler {

    private static AffinityBookButton currentAffinityButton = null;
    private static AbstractContainerScreen<?> currentScreen = null;

    /**
     * Add the affinity book button to screens that have a recipe book.
     *
     * @param event The screen initialization event
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        // Reset references
        currentAffinityButton = null;
        currentScreen = null;

        // Only add button to screens with recipe books
        if (!(event.getScreen() instanceof RecipeUpdateListener)) {
            return;
        }

        // Only handle container screens
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        // Get the recipe book component
        RecipeBookComponent recipeBook = null;

        // For inventory screen
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            recipeBook = inventoryScreen.getRecipeBookComponent();
        }
        // For other screens with recipe books (crafting table, furnace, etc.)
        // Add more checks here if needed

        if (recipeBook == null) {
            return;
        }

        currentScreen = containerScreen;
        RecipeBookComponent finalRecipeBook = recipeBook;

        // Calculate initial button position
        int leftPos = containerScreen.getGuiLeft();
        int topPos = containerScreen.getGuiTop();

        // Position: 24 pixels to the right of recipe book button
        // Recipe book button is at (leftPos + 104, topPos + 61)
        int buttonX = leftPos + 104 + 24;
        int buttonY = topPos + 61;

        // Create the affinity book button
        AffinityBookButton affinityButton = new AffinityBookButton(
                buttonX,
                buttonY,
                (button) -> {
                    // Close recipe book if it's open
                    if (finalRecipeBook.isVisible()) {
                        finalRecipeBook.toggleVisibility();
                    }

                    // Send packet to server to open affinity book
                    PacketDistributor.sendToServer(new AffinitySuccessPacket.OpenAffinityBookPacket());
                }
        );

        currentAffinityButton = affinityButton;

        // Add button to screen
        event.addListener(affinityButton);
    }

    /**
     * Update button position every frame to follow the container.
     * This is called before rendering, ensuring smooth movement.
     *
     * @param event The render event
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (currentAffinityButton == null || currentScreen == null) {
            return;
        }

        // Verify we're still on the same screen
        if (event.getScreen() != currentScreen) {
            currentAffinityButton = null;
            currentScreen = null;
            return;
        }

        // Update button position to follow container
        int leftPos = currentScreen.getGuiLeft();
        int topPos = currentScreen.getGuiTop();

        int newButtonX = leftPos + 104 + 24;  // 24 pixels right of recipe book button
        int newButtonY = topPos + 61;

        // Update button position
        currentAffinityButton.setPosition(newButtonX, newButtonY);
    }
}
