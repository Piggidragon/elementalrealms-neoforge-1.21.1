package de.piggidragon.elementalrealms.client.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.gui.screens.affinitybook.AffinityBookButton;
import de.piggidragon.elementalrealms.client.gui.screens.affinitybook.AffinityBookOverlay;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds the affinity book button next to the recipe book button
 * in screens that have a recipe book (like inventory, crafting table, etc.)
 * Renders the affinity book as an overlay and shifts the inventory to the right.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class AffinityBookButtonHandler {

    /**
     * Affinity button references per screen.
     */
    private static final Map<AbstractContainerScreen<?>, AffinityBookButton> affinityButtons = new HashMap<>();

    /**
     * Affinity book overlay references per screen.
     */
    private static final Map<AbstractContainerScreen<?>, AffinityBookOverlay> affinityOverlays = new HashMap<>();

    /**
     * Recipe book button references per screen.
     */
    private static final Map<AbstractContainerScreen<?>, ImageButton> recipeBookButtons = new HashMap<>();

    /**
     * Tracks whether the affinity book should be open.
     * This persists across inventory screen opens/closes.
     */
    private static boolean shouldAffinityBookBeOpen = false;

    /**
     * Cached reflection field for leftPos in EffectRenderingInventoryScreen.
     */
    private static Field leftPosField = null;

    /**
     * Add the affinity book button to screens that have a recipe book.
     *
     * @param event The screen initialization event
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
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

        if (recipeBook == null) {
            return;
        }

        // Find and store the recipe book button
        findAndStoreRecipeBookButton(containerScreen);

        // Create affinity book overlay
        AffinityBookOverlay overlay = new AffinityBookOverlay(
                containerScreen.getMinecraft().player
        );
        affinityOverlays.put(containerScreen, overlay);

        // Restore previous visibility state
        if (shouldAffinityBookBeOpen) {
            overlay.setVisible(true);

            // Close recipe book if it's open
            if (recipeBook.isVisible()) {
                recipeBook.toggleVisibility();
            }

            // Shift inventory to the right
            shiftInventoryForAffinityBook(containerScreen, true);
        }

        // Create and add affinity book button
        AffinityBookButton affinityButton = getAffinityBookButton(containerScreen, recipeBook, overlay);
        affinityButtons.put(containerScreen, affinityButton);
        event.addListener(affinityButton);
    }

    /**
     * Find and store the recipe book button for later position updates.
     *
     * @param containerScreen The container screen
     */
    private static void findAndStoreRecipeBookButton(AbstractContainerScreen<?> containerScreen) {
        // Search through all renderables to find the recipe book button
        for (Renderable renderable : containerScreen.renderables) {
            if (renderable instanceof ImageButton imgButton) {
                // The recipe book button uses specific sprites
                // We identify it by checking if it's an ImageButton (simple heuristic)
                // Store the first ImageButton we find (usually the recipe book button)
                if (!recipeBookButtons.containsValue(imgButton)) {
                    recipeBookButtons.put(containerScreen, imgButton);
                    ElementalRealms.LOGGER.debug("Found recipe book button at ({}, {})",
                            imgButton.getX(), imgButton.getY());
                    break;
                }
            }
        }
    }

    /**
     * Create the affinity book button with proper positioning.
     *
     * @param containerScreen The container screen to add the button to
     * @param recipeBook      The recipe book component
     * @param overlay         The affinity book overlay
     * @return The created affinity book button
     */
    private static @NotNull AffinityBookButton getAffinityBookButton(
            AbstractContainerScreen<?> containerScreen,
            RecipeBookComponent recipeBook,
            AffinityBookOverlay overlay
    ) {
        // Calculate initial button position
        int leftPos = containerScreen.getGuiLeft();
        int topPos = containerScreen.getGuiTop();

        // Position: next to recipe book button
        int buttonX = leftPos + 104 + 24;
        int buttonY = topPos + 61;

        // Create the affinity book button
        return new AffinityBookButton(
                buttonX,
                buttonY,
                (button) -> {
                    // Toggle affinity book overlay
                    overlay.toggleVisibility();

                    // Update persistence flag
                    shouldAffinityBookBeOpen = overlay.isVisible();

                    // Close recipe book if affinity book is opening
                    if (overlay.isVisible() && recipeBook.isVisible()) {
                        recipeBook.toggleVisibility();
                    }

                    // Shift inventory position
                    shiftInventoryForAffinityBook(containerScreen, overlay.isVisible());
                }
        );
    }

    /**
     * Shift the inventory screen to make room for the affinity book.
     * Uses reflection to modify the leftPos of EffectRenderingInventoryScreen.
     *
     * @param containerScreen The container screen to shift
     * @param shift           Whether to shift right (true) or reset (false)
     */
    private static void shiftInventoryForAffinityBook(AbstractContainerScreen<?> containerScreen, boolean shift) {
        if (!(containerScreen instanceof EffectRenderingInventoryScreen<?> inventoryScreen)) {
            return;
        }

        try {
            // Cache the field on first use
            if (leftPosField == null) {
                leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
                leftPosField.setAccessible(true);
            }

            // Calculate new position
            int originalLeftPos = (containerScreen.width - 176) / 2;
            int shiftAmount = shift ? 77 : 0;  // Shift by overlay width (147) / 2 to center better
            int newLeftPos = originalLeftPos + shiftAmount;

            // Set the new leftPos
            leftPosField.setInt(containerScreen, newLeftPos);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            ElementalRealms.LOGGER.error("Failed to shift inventory for affinity book", e);
        }
    }

    /**
     * Update button positions every frame to follow the container.
     *
     * @param event The render event
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        int leftPos = containerScreen.getGuiLeft();
        int topPos = containerScreen.getGuiTop();

        // Update affinity book button position
        AffinityBookButton affinityButton = affinityButtons.get(containerScreen);
        if (affinityButton != null) {
            int newButtonX = leftPos + 104 + 24;
            int newButtonY = topPos + 61;
            affinityButton.setPosition(newButtonX, newButtonY);
        }

        // Update recipe book button position
        ImageButton recipeButton = recipeBookButtons.get(containerScreen);
        if (recipeButton != null) {
            int newRecipeButtonX = leftPos + 104;
            int newRecipeButtonY = topPos + 61;
            recipeButton.setPosition(newRecipeButtonX, newRecipeButtonY);
        }

        // Check if recipe book opened while affinity book is open
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            AffinityBookOverlay overlay = affinityOverlays.get(containerScreen);
            RecipeBookComponent recipeBook = inventoryScreen.getRecipeBookComponent();

            // If both are visible, close affinity book
            if (overlay != null && recipeBook != null && overlay.isVisible() && recipeBook.isVisible()) {
                overlay.setVisible(false);
                shouldAffinityBookBeOpen = false;
            }
        }
    }

    /**
     * Render the affinity book overlay next to the inventory.
     *
     * @param event The render event
     */
    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        // Get overlay for this screen
        AffinityBookOverlay overlay = affinityOverlays.get(containerScreen);
        if (overlay == null || !overlay.isVisible()) {
            return;
        }

        // Calculate position (left of inventory)
        int leftPos = containerScreen.getGuiLeft();
        int topPos = containerScreen.getGuiTop();

        // Position affinity book to the left of the inventory
        int overlayX = leftPos - overlay.getWidth();

        // Render overlay
        overlay.render(
                event.getGuiGraphics(),
                overlayX,
                topPos,
                event.getMouseX(),
                event.getMouseY(),
                event.getPartialTick()
        );
    }

    /**
     * Handle screen closing to clean up references.
     *
     * @param event The screen closing event
     */
    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        // Clean up references
        affinityButtons.remove(containerScreen);
        affinityOverlays.remove(containerScreen);
        recipeBookButtons.remove(containerScreen);
    }

    /**
     * Manually set whether the affinity book should be open.
     *
     * @param shouldBeOpen Whether the affinity book should persist as open
     */
    public static void setShouldAffinityBookBeOpen(boolean shouldBeOpen) {
        shouldAffinityBookBeOpen = shouldBeOpen;
    }

    /**
     * Get the current affinity book persistence state.
     *
     * @return true if affinity book should be open when inventory reopens
     */
    public static boolean shouldAffinityBookBeOpen() {
        return shouldAffinityBookBeOpen;
    }
}
