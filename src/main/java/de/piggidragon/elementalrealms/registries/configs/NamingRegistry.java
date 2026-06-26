package de.piggidragon.elementalrealms.registries.configs;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for user-facing display names of affinities, dimensions,
 * bosses, and spells. All in-game strings that reference content types flow through
 * here so that modpack authors can re-theme the mod without touching code.
 * <p>
 * Defaults are intentionally generic fantasy terms — NOT from TBATE (see
 * project-constraints §3). Boss names get designed when Phase 5 hits; for now we
 * carry placeholders that match the pattern the user will eventually use.
 * <p>
 * Lookup is case-insensitive for the {@code String} overloads. Affinity values are
 * mapped 1:1 by enum name. Custom entries (from JSON) win over defaults.
 */
public final class NamingRegistry {

    private static final Map<String, String> CUSTOM = new ConcurrentHashMap<>();

    private static final Map<String, String> DEFAULTS;

    static {
        Map<String, String> defaults = new HashMap<>();

        // Affinity display names — generic fantasy terms.
        defaults.put("affinity.fire", "Fire");
        defaults.put("affinity.water", "Water");
        defaults.put("affinity.wind", "Wind");
        defaults.put("affinity.earth", "Earth");
        defaults.put("affinity.lightning", "Lightning");
        defaults.put("affinity.ice", "Ice");
        defaults.put("affinity.sound", "Sound");
        defaults.put("affinity.gravity", "Gravity");
        defaults.put("affinity.life", "Life");
        defaults.put("affinity.space", "Space");
        defaults.put("affinity.time", "Time");
        defaults.put("affinity.void", "Void");

        // Shard / stone item name fragments (combined by the item registry).
        defaults.put("item.shard.small", "Small Affinity Shard of %s");
        defaults.put("item.shard.medium", "Medium Affinity Shard of %s");
        defaults.put("item.shard.big", "Big Affinity Shard of %s");
        defaults.put("item.stone", "Affinity Stone of %s");
        defaults.put("item.void_stone", "Void Stone");

        // Boss placeholders — Phase 5 will fill these in. Generic fantasy tone.
        defaults.put("boss.fire", "Lord of Embers");
        defaults.put("boss.water", "Tidal Sovereign");
        defaults.put("boss.wind", "Stormweaver");
        defaults.put("boss.earth", "The Stoneheart");
        defaults.put("boss.lightning", "Tempest Conductor");
        defaults.put("boss.ice", "The Glacial Maw");
        defaults.put("boss.sound", "Echo of the Deep");
        defaults.put("boss.gravity", "The Inverted One");
        defaults.put("boss.life", "The Verdant Throne");
        defaults.put("boss.space", "Voidwalker");
        defaults.put("boss.time", "The Unending");

        // Dimension placeholders.
        defaults.put("dimension.school", "The Academy");
        defaults.put("dimension.pocket.fire", "Ember Wastes");
        defaults.put("dimension.pocket.water", "Trench of Drowning Light");
        defaults.put("dimension.pocket.wind", "Skyloft Shards");
        defaults.put("dimension.pocket.earth", "Hollowdeep");
        defaults.put("dimension.pocket.lightning", "Stormspire");
        defaults.put("dimension.pocket.ice", "Frozen Crown");
        defaults.put("dimension.pocket.sound", "Echoing Catacombs");
        defaults.put("dimension.pocket.gravity", "The Inverted Spire");
        defaults.put("dimension.pocket.life", "Overgrowth");
        defaults.put("dimension.pocket.space", "The Empty Between");
        defaults.put("dimension.pocket.time", "The Still Hours");

        DEFAULTS = Collections.unmodifiableMap(defaults);
    }

    private NamingRegistry() {
    }

    /**
     * Looks up a name by key. Custom entries (registered via {@link #register})
     * take precedence over defaults. Returns the key itself if nothing matches —
     * better to show a debug label than crash.
     */
    public static String get(String key) {
        String custom = CUSTOM.get(key.toLowerCase());
        if (custom != null) return custom;
        String def = DEFAULTS.get(key.toLowerCase());
        if (def != null) return def;
        return key;
    }

    /**
     * Registers a custom name, overriding the default. Thread-safe. Re-registering
     * the same key overwrites the previous custom value (used by the JSON5 loader).
     */
    public static void register(String key, String value) {
        CUSTOM.put(key.toLowerCase(), value);
    }

    /**
     * Removes a custom registration, falling back to defaults.
     */
    public static void unregister(String key) {
        CUSTOM.remove(key.toLowerCase());
    }

    /**
     * Wipes all custom registrations. Used by the reload command to start clean
     * before re-applying the JSON5 layer.
     */
    public static void clearCustom() {
        CUSTOM.clear();
    }

    /**
     * Convenience: display name for an {@link Affinity}.
     */
    public static String displayName(Affinity affinity) {
        return get("affinity." + affinity.getName());
    }
}