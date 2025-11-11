package de.piggidragon.elementalrealms.events;

import com.mojang.blaze3d.platform.InputConstants;
import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Registers custom key bindings for the mod
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class ModKeyBindingsHandler {

    /**
     * Key binding to open the affinity GUI
     */
    public static final KeyMapping OPEN_AFFINITY_GUI = new KeyMapping(
            "key.elementalrealms.open_affinity_gui", // Translation key
            InputConstants.Type.KEYSYM,               // Key type (keyboard)
            GLFW.GLFW_KEY_K,                         // Default key (K)
            "key.categories.elementalrealms"         // Category translation key
    );

    /**
     * Registers all key mappings
     * @param event The registration event
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_AFFINITY_GUI);
    }
}
