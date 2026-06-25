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
 * Deferred items for affinity stones and shards.
 */
public final class AffinityItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_STONES = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerStone(map, Affinity.FIRE, Rarity.EPIC);
        registerStone(map, Affinity.WATER, Rarity.EPIC);
        registerStone(map, Affinity.WIND, Rarity.EPIC);
        registerStone(map, Affinity.EARTH, Rarity.EPIC);
        registerStone(map, Affinity.LIGHTNING, Rarity.EPIC);
        registerStone(map, Affinity.ICE, Rarity.EPIC);
        registerStone(map, Affinity.SOUND, Rarity.EPIC);
        registerStone(map, Affinity.GRAVITY, Rarity.EPIC);
        registerStone(map, Affinity.TIME, Rarity.EPIC);
        registerStone(map, Affinity.SPACE, Rarity.EPIC);
        registerStone(map, Affinity.LIFE, Rarity.EPIC);
        registerStone(map, Affinity.VOID, Rarity.RARE);
    });
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_SHARDS = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerShard(map, Affinity.FIRE, Rarity.RARE);
        registerShard(map, Affinity.WATER, Rarity.RARE);
        registerShard(map, Affinity.WIND, Rarity.RARE);
        registerShard(map, Affinity.EARTH, Rarity.RARE);
        registerShard(map, Affinity.LIGHTNING, Rarity.EPIC);
        registerShard(map, Affinity.ICE, Rarity.EPIC);
        registerShard(map, Affinity.SOUND, Rarity.EPIC);
        registerShard(map, Affinity.GRAVITY, Rarity.EPIC);
    });

    private AffinityItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static void registerStone(Map<Affinity, DeferredItem<Item>> map, Affinity affinity, Rarity rarity) {
        map.put(affinity, ITEMS.registerItem(
                "affinity_stone_" + affinity.getName(),
                props -> new AffinityStone(props.rarity(rarity), affinity)
        ));
    }

    private static void registerShard(Map<Affinity, DeferredItem<Item>> map, Affinity affinity, Rarity rarity) {
        map.put(affinity, ITEMS.registerItem(
                "affinity_shard_" + affinity.getName(),
                props -> new AffinityShard(props.rarity(rarity), affinity)
        ));
    }
}
