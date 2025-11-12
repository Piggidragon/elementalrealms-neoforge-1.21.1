package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

/**
 * Button that appears next to the recipe book button.
 * Opens the affinity book screen when clicked.
 */
public class AffinityBookButton extends ImageButton {

    /**
     * Sprites for the button (normal and highlighted states).
     * Using recipe book button sprites as base.
     */
    private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/button"),
            ResourceLocation.withDefaultNamespace("recipe_book/button_highlighted")
    );

    /**
     * Create a new affinity book button.
     *
     * @param x       X position
     * @param y       Y position
     * @param onPress Action to perform when clicked
     */
    public AffinityBookButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 18, SPRITES, onPress);
    }
}

