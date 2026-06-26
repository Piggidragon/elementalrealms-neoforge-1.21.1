package de.piggidragon.elementalrealms.registries.items.magic.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.custom.AffinityShard;
import de.piggidragon.elementalrealms.registries.items.magic.affinities.custom.AffinityStone;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/**
 * Deferred items for affinity stones and shards.
 *
 * <p>Rarities are read from {@link AffinityConfig} (resolved from
 * {@code affinities.json.rarities}). The rarity is captured per-item at
 * registration time by resolving it inside the {@link DeferredRegister}
 * supplier lambda - this avoids touching {@code ModRarities.legendary()} /
 * {@code ModRarities.mythic()} during the {@link AffinityItems} static
 * initializer, which would crash with "Enum not initialized" because the
 * custom Rarity enum constants are injected only after FML bootstrap.</p>
 *
 * <p>A config edit followed by {@code /elementalrealms reload} will NOT
 * retroactively change already-registered items' rarities - the {@code Rarity}
 * is baked into the {@link Item.Properties} at registration, and live
 * re-registration of items is not supported by NeoForge.</p>
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
        // Rarity is resolved inside the supplier lambda, not in the static-init
        // scope of AffinityItems.<clinit>. The supplier runs at registry-event
        // time, well after FML bootstrap has injected LEGENDARY/MYTHIC into the
        // Rarity enum. Calling AffinityConfig.stoneRarity(affinity) eagerly
        // here would crash with "Enum not initialized" because the static-init
        // runs during class loading, before FML bootstrap completes.
        map.put(affinity, ITEMS.registerItem(
                "affinity_stone_" + affinity.getName(),
                props -> new AffinityStone(props.rarity(AffinityConfig.stoneRarity(affinity)), affinity)
        ));
    }

    private static void registerShard(Map<Affinity, DeferredItem<Item>> map, Affinity affinity) {
        // Same deferred-resolution contract as registerStone above.
        map.put(affinity, ITEMS.registerItem(
                "affinity_shard_" + affinity.getName(),
                props -> new AffinityShard(props.rarity(AffinityConfig.shardRarity(affinity)), affinity)
        ));
    }
}