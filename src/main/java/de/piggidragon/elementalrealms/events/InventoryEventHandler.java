package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.gui.screens.AffinityScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = ElementalRealms.MODID)
public class InventoryEventHandler {
    @SubscribeEvent
    public static void onInventoryScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        InventoryScreen invScreen = (InventoryScreen) event.getScreen();

        int x = invScreen.getGuiLeft() + 150;
        int y = invScreen.getGuiTop() + 10;

        Button affinityButton = new Button(
                x,
                y,
                20,
                20,
                Component.literal("Affinities"),
                button -> {
            Minecraft.getInstance().setScreen(new AffinityScreen(invScreen.getMinecraft().player.getInventory()));
        });

        event.addListener(affinityButton);
    }
}
