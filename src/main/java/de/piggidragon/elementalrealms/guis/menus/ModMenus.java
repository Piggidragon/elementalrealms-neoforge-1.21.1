package de.piggidragon.elementalrealms.guis.menus;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.guis.menus.custom.AffinityMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ElementalRealms.MODID);

    public static final Supplier<MenuType<AffinityMenu>> AFFINITY_MENU =
            MENUS.register("affinity_menu", () ->
                    // IMenuTypeExtension allows FriendlyByteBuf parameter
                    IMenuTypeExtension.create(AffinityMenu::new)
            );

    /**
     * Registers all menu types with the mod event bus.
     *
     * @param bus The mod's event bus for registration
     */
    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
