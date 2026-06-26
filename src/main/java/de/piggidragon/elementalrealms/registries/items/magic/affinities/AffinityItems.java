package de.piggidragon.elementalrealms.registries.items.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
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
 *
 * <p>Rarities are read from {@link AffinityConfig} (resolved from
 * {@code affinities.json.rarities}). Resolution runs once at static-init time;
 * a config edit followed by {@code /elementalrealms reload} will NOT retroactively
 * change already-registered items' rarities - the {@link Rarity} is baked into the
 * {@link Item.Properties} at registration, and live re-registration of items is
 * not supported by NeoForge.</p>
 */
public final class AffinityItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElementalRealms.MODID);
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_STONES = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerStone(map, Affinity.FIRE);
        registerStone(map, Affinity.WATER);
        registerStone(map, Affinity.WIND);
        registerStone(map, Affinity.EARTH);
        registerStone(map, Affinity.LIGHTNING);
        registerStone(map, Affinity.ICE);
        registerStone(map, Affinity.SOUND);
        registerStone(map, Affinity.GRAVITY);
        registerStone(map, Affinity.TIME);
        registerStone(map, Affinity.SPACE);
        registerStone(map, Affinity.LIFE);
        registerStone(map, Affinity.VOID);
    });
    public static final Map<Affinity, DeferredItem<Item>> AFFINITY_SHARDS = Util.make(new EnumMap<>(Affinity.class), map -> {
        registerShard(map, Affinity.FIRE);
        registerShard(map, Affinity.WATER);
        registerShard(map, Affinity.WIND);
        registerShard(map, Affinity.EARTH);
        registerShard(map, Affinity.LIGHTNING);
        registerShard(map, Affinity.ICE);
        registerShard(map, Affinity.SOUND);
        registerShard(map, Affinity.GRAVITY);
    });

    private AffinityItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static void registerStone(Map<Affinity, DeferredItem<Item>> map, Affinity affinity) {
        Rarity rarity = AffinityConfig.stoneRarity(affinity);
        map.put(affinity, ITEMS.registerItem(
                "affinity_stone_" + affinity.getName(),
                props -> new AffinityStone(props.rarity(rarity), affinity)
        ));
    }

    private static void registerShard(Map<Affinity, DeferredItem<Item>> map, Affinity affinity) {
        Rarity rarity = AffinityConfig.shardRarity(affinity);
        map.put(affinity, ITEMS.registerItem(
                "affinity_shard_" + affinity.getName(),
                props -> new AffinityShard(props.rarity(rarity), affinity)
        ));
    }
}