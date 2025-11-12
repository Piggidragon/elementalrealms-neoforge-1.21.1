package de.piggidragon.elementalrealms.registries.items.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.custom.AffinityShard;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.custom.AffinityStone;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for affinity items: stones, shards, and essences.
 */
public class AffinityItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);

    /**
     * Affinity stones mapped by type. Used to grant affinities to players.
     */
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_STONES = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerAffinityStone(map, Affinity.FIRE, Rarity.EPIC);
        registerAffinityStone(map, Affinity.WATER, Rarity.EPIC);
        registerAffinityStone(map, Affinity.WIND, Rarity.EPIC);
        registerAffinityStone(map, Affinity.EARTH, Rarity.EPIC);
        registerAffinityStone(map, Affinity.LIGHTNING, Rarity.EPIC);
        registerAffinityStone(map, Affinity.ICE, Rarity.EPIC);
        registerAffinityStone(map, Affinity.SOUND, Rarity.EPIC);
        registerAffinityStone(map, Affinity.GRAVITY, Rarity.EPIC);
        registerAffinityStone(map, Affinity.TIME, Rarity.EPIC);
        registerAffinityStone(map, Affinity.SPACE, Rarity.EPIC);
        registerAffinityStone(map, Affinity.LIFE, Rarity.EPIC);
        registerAffinityStone(map, Affinity.VOID, Rarity.RARE); // Void stone
    });

    /**
     * Affinity shards mapped by type. Crafting material for stones.
     */
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_SHARDS = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerAffinityShard(map, Affinity.FIRE, Rarity.RARE);
        registerAffinityShard(map, Affinity.WATER, Rarity.RARE);
        registerAffinityShard(map, Affinity.WIND, Rarity.RARE);
        registerAffinityShard(map, Affinity.EARTH, Rarity.RARE);
        registerAffinityShard(map, Affinity.LIGHTNING, Rarity.EPIC);
        registerAffinityShard(map, Affinity.ICE, Rarity.EPIC);
        registerAffinityShard(map, Affinity.SOUND, Rarity.EPIC);
        registerAffinityShard(map, Affinity.GRAVITY, Rarity.EPIC);
    });

    /**
     * Helper to register an affinity stone with given rarity.
     *
     * @param map      Map to store the registered stone
     * @param affinity Affinity type for this stone
     * @param rarity   Item rarity
     */
    private static void registerAffinityStone(Map<Affinity, DeferredItem<Item>> map, Affinity affinity, Rarity rarity) {
        String name = "affinity_stone_" + affinity.getName();
        DeferredItem<Item> stone = ITEMS.registerItem(
                name,
                (p) -> new AffinityStone(p.rarity(rarity), affinity)
        );
        map.put(affinity, stone);
    }

    /**
     * Helper to register an affinity shard with given rarity.
     *
     * @param map      Map to store the registered shard
     * @param affinity Affinity type for this shard
     * @param rarity   Item rarity
     */
    private static void registerAffinityShard(Map<Affinity, DeferredItem<Item>> map, Affinity affinity, Rarity rarity) {
        String name = "affinity_shard_" + affinity.getName();
        DeferredItem<Item> shard = ITEMS.registerItem(
                name,
                (p) -> new AffinityShard(p.rarity(rarity), affinity)
        );
        map.put(affinity, shard);
    }

    /**
     * Registers all affinity items with the mod event bus.
     *
     * @param bus The mod event bus
     */
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
