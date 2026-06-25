package de.piggidragon.elementalrealms.registries.items.magic.misc;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.items.magic.misc.custom.SchoolStaff;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred items that don't fit into affinity or material categories.
 */
public final class MiscItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);
    /**
     * Staff that opens temporary portals to the School dimension. 16 uses.
     */
    public static final DeferredItem<Item> DIMENSION_STAFF = ITEMS.registerItem(
            "dimension_staff",
            props -> new SchoolStaff(props.durability(16).rarity(Rarity.UNCOMMON))
    );

    private MiscItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
