package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

/**
 * Button shown next to the recipe book toggle in container screens.
 */
public class AffinityBookButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_book/blank_button"),
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_book/blank_button_highlighted")
    );

    public AffinityBookButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 18, SPRITES, onPress);
    }
}
