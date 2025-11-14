package de.piggidragon.elementalrealms.registries.items.magic.misc;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.items.magic.misc.custom.LaserStaff;
import de.piggidragon.elementalrealms.registries.items.magic.misc.custom.SchoolStaff;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for dimension-related items.
 */
public class MiscItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);

    /**
     * Staff that creates temporary portals to School dimension.
     * 16 uses, beam animation, 10-second portals.
     */
    public static final DeferredItem<Item> DIMENSION_STAFF = ITEMS.registerItem(
            "dimension_staff",
            (p) -> new SchoolStaff(p.durability(16).rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<Item> LASER_STAFF = ITEMS.registerItem(
            "laser_staff",
            (p) -> new LaserStaff(p.durability(50).rarity(Rarity.RARE))
    );

    /**
     * Registers all dimension items with the mod event bus.
     *
     * @param bus The mod event bus
     */
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
