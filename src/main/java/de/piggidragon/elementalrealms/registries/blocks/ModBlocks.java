package de.piggidragon.elementalrealms.registries.blocks;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for custom blocks and their corresponding items.
 * Currently empty but structured for future block additions.
 */
public final class ModBlocks {

    /** Deferred register for block registrations */
    // Block-Registry
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ElementalRealms.MODID);

    /** Deferred register for block items */
    // Item-Registry
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);

    /*
    // Block-Registrierung mit Eigenschaften
    public static final DeferredBlock<SchoolDimensionPortal> SCHOOL_DIMENSION_PORTAL =
            BLOCKS.registerBlock("school_dimension_portal",
                    (p) -> new SchoolDimensionPortal(p
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.GLASS)
                            .lightLevel(s -> 10)
                            .noOcclusion()
                            .noCollission()
                            .isViewBlocking((s, r, pos) -> false)
                            .isRedstoneConductor((s, r, pos) -> false))
            );

    // Item-Registrierung für den Block
    public static final DeferredItem<BlockItem> SCHOOL_DIMENSION_PORTAL_ITEM =
            ITEMS.registerSimpleBlockItem("school_dimension_portal", SCHOOL_DIMENSION_PORTAL,
                    new Item.Properties());

     */

    /**
     * Registers blocks and block items with the mod event bus.
     *
     * @param bus The mod's event bus for registration
     */
    // Registrierungsmethode fürs EventBus
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
