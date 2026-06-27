package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.AffinityType;
import de.piggidragon.elementalrealms.registries.rarities.ModRarities;
import net.minecraft.world.item.Rarity;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Loads {@code config/elementalrealms/affinities.json}. Controls the login roll
 * distribution, per-affinity tunables, and per-item rarity mapping.
 * <p>
 * The values mirror what was previously hardcoded in {@code ModAffinitiesRoll}
 * (Phase-0.3 will swap the live callers to read from this config).
 * <p>
 * Rarity lookup is lazy: the per-affinity rarity maps are not materialised
 * during {@link #reload()} (which can fire in {@code static {}} context via
 * {@code INSTANCE = new AffinityConfig()}, before the FML loader has injected
 * {@link ModRarities#LEGENDARY}/{@link ModRarities#MYTHIC} into the {@code Rarity}
 * enum). The first call to {@link #stoneRarity(Affinity)} or
 * {@link #shardRarity(Affinity)} resolves the maps against live {@code Rarity}
 * constants; subsequent reloads re-seed them in place.
 */
public final class AffinityConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 2;

    // Effective values, refreshed by reload().
    private static int[] slotChances = {100, 25, 20, 20};
    private static int deviantChancePercent = 25;
    private static int maxCompletionPercent = 100;
    private static boolean deviantRequiresBase = true;
    private static boolean elementalGuaranteed = true;
    private static boolean eternalAllOrNothing = true;
    private static int eternalStoneRarityPercent = 5;

    // Three-stage roll (issue #23).
    private static int deviantPartialChancePercent = 10;
    private static int deviantMaxCompletionPercent = 80;
    private static int partialDeviantWeightPercent = 15;
    private static int elementalContinueChanceStartPercent = 50;
    private static int elementalContinueChanceDecayPercent = 50;
    private static int elementalMaxCompletionPercent = 80;
    private static int elementalMaxIterations = 5;
    private static double partialCompletionSlope = 3.0;

    // Parsed JSON5 sub-sections, retained across reloads so the rarity lookup can
    // re-apply them lazily once the FML loader has injected LEGENDARY / MYTHIC.
    // null until the first reload() that sees a "rarities" block.
    private static volatile JsonObject lastRaritiesRoot;
    private static final AtomicReference<Map<Affinity, Rarity>> STONE_RARITIES =
            new AtomicReference<>();
    private static final AtomicReference<Map<Affinity, Rarity>> SHARD_RARITIES =
            new AtomicReference<>();

    public static final AffinityConfig INSTANCE = new AffinityConfig();

    public AffinityConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "affinities.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("affinities.json not found - wrote defaults. Using in-memory defaults.");
            lastRaritiesRoot = null;
            // Drop any cached maps so the next lookup rebuilds from defaults.
            STONE_RARITIES.set(null);
            SHARD_RARITIES.set(null);
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("affinities.json schema mismatch - keeping in-memory defaults.");
            // Don't drop lastRaritiesRoot - the old config might still be valid for
            // the schema we're running with, and dropping it would force a rebuild
            // against hardcoded defaults.
            return;
        }

        JsonObject obj = root.getAsJsonObject();
        if (obj.has("roll")) {
            JsonObject roll = obj.getAsJsonObject("roll");
            slotChances = new Json5SectionReader(roll).getIntArray("slotChances", slotChances);
            elementalGuaranteed = Json5SectionReader.getBoolean(roll, "elementalGuaranteed", elementalGuaranteed);
            deviantChancePercent = Json5SectionReader.getInt(roll, "deviantChancePercent", deviantChancePercent);
            deviantPartialChancePercent = Json5SectionReader.getInt(roll, "deviantPartialChancePercent", deviantPartialChancePercent);
            deviantMaxCompletionPercent = Json5SectionReader.getInt(roll, "deviantMaxCompletionPercent", deviantMaxCompletionPercent);
            partialDeviantWeightPercent = Json5SectionReader.getInt(roll, "partialDeviantWeightPercent", partialDeviantWeightPercent);
            elementalContinueChanceStartPercent = Json5SectionReader.getInt(roll, "elementalContinueChanceStartPercent", elementalContinueChanceStartPercent);
            elementalContinueChanceDecayPercent = Json5SectionReader.getInt(roll, "elementalContinueChanceDecayPercent", elementalContinueChanceDecayPercent);
            elementalMaxCompletionPercent = Json5SectionReader.getInt(roll, "elementalMaxCompletionPercent", elementalMaxCompletionPercent);
            elementalMaxIterations = Json5SectionReader.getInt(roll, "elementalMaxIterations", elementalMaxIterations);
            partialCompletionSlope = Json5SectionReader.getDouble(roll, "partialCompletionSlope", partialCompletionSlope);
        }
        if (obj.has("completion")) {
            JsonObject completion = obj.getAsJsonObject("completion");
            maxCompletionPercent = Json5SectionReader.getInt(completion, "maxCompletionPercent", maxCompletionPercent);
        }
        if (obj.has("tiers")) {
            JsonObject tiers = obj.getAsJsonObject("tiers");
            deviantRequiresBase = Json5SectionReader.getBoolean(tiers, "deviantRequiresBase", deviantRequiresBase);
            eternalAllOrNothing = Json5SectionReader.getBoolean(tiers, "eternalAllOrNothing", eternalAllOrNothing);
        }
        if (obj.has("drops")) {
            JsonObject drops = obj.getAsJsonObject("drops");
            eternalStoneRarityPercent = Json5SectionReader.getInt(drops, "eternalStoneRarityPercent", eternalStoneRarityPercent);
        }
        // Capture the rarities block but don't resolve it yet - resolution touches
        // LEGENDARY/MYTHIC which only exist after the FML loader has injected them.
        if (obj.has("rarities")) {
            lastRaritiesRoot = obj.getAsJsonObject("rarities");
        } else {
            lastRaritiesRoot = null;
        }
        // Invalidate cached maps so the next lookup re-applies the (possibly new)
        // lastRaritiesRoot. Do NOT touch the maps in-place here - the old map might
        // still be referenced by code that ran before this reload fired.
        STONE_RARITIES.set(null);
        SHARD_RARITIES.set(null);

        ElementalRealms.LOGGER.debug("affinities.json loaded: slotChances={}, deviantChance={}",
                java.util.Arrays.toString(slotChances), deviantChancePercent);
    }

    /**
     * Resolves a section like {@code rarities.stones} into a per-affinity map.
     * Lookup order for each entry: explicit per-affinity name -> per-tier name
     * (elemental/deviant/eternal/void) -> {@code "default"} -> hardcoded fallback.
     * Unknown rarity strings log a warning and fall back to EPIC.
     * <p>
     * MUST NOT be called from {@code static {}} context - it touches
     * {@link ModRarities#legendary()}, which throws if the enum extension hasn't
     * been injected yet.
     */
    private static Map<Affinity, Rarity> buildRarityMap(String kind, Map<Affinity, Rarity> defaults) {
        Map<Affinity, Rarity> map = new EnumMap<>(Affinity.class);
        JsonObject raritiesRoot = lastRaritiesRoot;
        JsonObject section = (raritiesRoot != null
                && raritiesRoot.has(kind)
                && raritiesRoot.get(kind).isJsonObject())
                ? raritiesRoot.getAsJsonObject(kind)
                : new JsonObject();

        Rarity fallback = resolveRarityName(section, "default", defaults.values().iterator().next());

        for (Affinity affinity : Affinity.values()) {
            // 1. explicit per-affinity override
            String explicitKey = affinity.name().toLowerCase();
            if (section.has(explicitKey)) {
                Rarity r = resolveRarityName(section, explicitKey, fallback);
                if (r != null) {
                    map.put(affinity, r);
                    continue;
                }
            }
            // 2. per-tier override
            String tierKey = (affinity.getType() == AffinityType.NONE) ? "void" : affinity.getType().name().toLowerCase();
            if (section.has(tierKey)) {
                Rarity r = resolveRarityName(section, tierKey, fallback);
                if (r != null) {
                    map.put(affinity, r);
                    continue;
                }
            }
            // 3. seeded default (matches the issue #24 spec)
            map.put(affinity, defaults.getOrDefault(affinity, fallback));
        }
        return map;
    }

    /**
     * Maps a string like {@code "EPIC"} or {@code "LEGENDARY"} to the runtime
     * {@link Rarity} instance. {@code null} on miss/wrong-type so the caller can
     * fall through to the next lookup layer.
     * <p>
     * Same timing caveat as {@link #buildRarityMap}: don't call from {@code static {}}.
     */
    private static Rarity resolveRarityName(JsonObject section, String key, Rarity fallback) {
        if (!section.has(key) || !section.get(key).isJsonPrimitive()) {
            return fallback;
        }
        String name;
        try {
            name = section.get(key).getAsString();
        } catch (Exception e) {
            return fallback;
        }
        return parseRarity(name).orElse(fallback);
    }

    /** Accepts {@code EPIC}, {@code LEGENDARY}, {@code MYTHIC}, etc. Case-insensitive. */
    static java.util.Optional<Rarity> parseRarity(String name) {
        if (name == null) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(switch (name.trim().toUpperCase()) {
                case "COMMON" -> Rarity.COMMON;
                case "UNCOMMON" -> Rarity.UNCOMMON;
                case "RARE" -> Rarity.RARE;
                case "EPIC" -> Rarity.EPIC;
                case "LEGENDARY" -> ModRarities.legendary();
                case "MYTHIC" -> ModRarities.mythic();
                default -> {
                    ElementalRealms.LOGGER.warn("Unknown rarity '{}' in affinities.json - falling back to EPIC.", name);
                    yield Rarity.EPIC;
                }
            });
        } catch (Exception e) {
            ElementalRealms.LOGGER.warn("Failed to resolve rarity '{}': {} - falling back to EPIC.", name, e.getMessage());
            return java.util.Optional.of(Rarity.EPIC);
        }
    }

    private static Map<Affinity, Rarity> defaultStoneRarities() {
        Map<Affinity, Rarity> m = new EnumMap<>(Affinity.class);
        for (Affinity a : Affinity.values()) {
            if (a == Affinity.VOID) {
                m.put(a, Rarity.RARE);
            } else {
                m.put(a, switch (a.getType()) {
                    case ELEMENTAL -> Rarity.EPIC;
                    case DEVIANT -> ModRarities.legendary();
                    case ETERNAL -> ModRarities.mythic();
                    default -> Rarity.EPIC;
                });
            }
        }
        return m;
    }

    private static Map<Affinity, Rarity> defaultShardRarities() {
        Map<Affinity, Rarity> m = new EnumMap<>(Affinity.class);
        for (Affinity a : Affinity.values()) {
            if (a == Affinity.VOID || a.getType() == AffinityType.ETERNAL) {
                // Eternal has no shards, VOID has no shards - fall back so a stray
                // call doesn't return null.
                m.put(a, Rarity.RARE);
            } else {
                m.put(a, switch (a.getType()) {
                    case ELEMENTAL -> Rarity.RARE;
                    case DEVIANT -> Rarity.EPIC;
                    default -> Rarity.RARE;
                });
            }
        }
        return m;
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms - affinities config (JSON5: comments + trailing commas OK)
                // Edit and run /elementalrealms reload to apply without restart.

                {
                  "schemaVersion": 2,

                  "roll": {
                    // Probability (%) that each of the four roll slots grants an ELEMENTAL affinity.
                    // First slot is the guaranteed 100%-roll for one elemental.
                    "slotChances": [100, 25, 20, 20],
                    "elementalGuaranteed": true,
                    // When a roll succeeds, chance (%) the matching DEVIANT also unlocks.
                    "deviantChancePercent": 25,

                    // Three-stage roll (issue #23):
                    //   Stage 1: 1 guaranteed ELEMENTAL at 100% (hardcoded).
                    //   Stage 2: rare DEVIANT partial roll for the matching Deviant.
                    //   Stage 3: decaying loop for additional ELEMENTALs (and the matching
                    //            Deviant if Stage 2 didn't already claim it).
                    // Partial completions use the continuous left-skew
                    // completion = round(maxCompletion * U^slope) - see partialCompletionSlope.
                    "deviantPartialChancePercent": 10,
                    "deviantMaxCompletionPercent": 80,
                    "partialDeviantWeightPercent": 15,
                    "elementalContinueChanceStartPercent": 50,
                    "elementalContinueChanceDecayPercent": 50,
                    "elementalMaxCompletionPercent": 80,
                    "elementalMaxIterations": 5,
                    // slope=1 is uniform; slope=3 (default) is heavily left-skewed:
                    // with maxCompletion=80, ~79% of partials land <= 40%.
                    "partialCompletionSlope": 3.0
                  },

                  "completion": {
                    // Max completion (%) any affinity can reach. Items that would push completion
                    // beyond this throw IllegalStateException; tier-validation thresholds use this too.
                    "maxCompletionPercent": 100
                  },

                  "tiers": {
                    // DEVIANT affinity requires its ELEMENTAL base at 100%.
                    "deviantRequiresBase": true,
                    // Only one ETERNAL affinity per player.
                    "eternalAllOrNothing": true
                  },

                  "drops": {
                    // Chance (%) that a boss drops its affinity stone (rare even from boss).
                    "eternalStoneRarityPercent": 5
                  },

                  // Per-item rarity mapping (issue #24). Resolution per affinity:
                    //   1. explicit per-affinity key (e.g. "fire": "EPIC")
                    //   2. per-tier key (elemental / deviant / eternal / void)
                    //   3. "default" for the section
                    //   4. EPIC if no key matches.
                  // Unknown rarity names log a warning and fall back to EPIC.
                  "rarities": {
                    "stones": {
                      "default": "EPIC",
                      "elemental": "EPIC",
                      "deviant": "LEGENDARY",
                      "eternal": "MYTHIC",
                      "void": "RARE"
                    },
                    "shards": {
                      "default": "RARE",
                      "elemental": "RARE",
                      "deviant": "EPIC"
                    }
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }

    public static int[] slotChances() { return slotChances.clone(); }
    public static int deviantChancePercent() { return deviantChancePercent; }
    public static int maxCompletionPercent() { return maxCompletionPercent; }
    public static boolean deviantRequiresBase() { return deviantRequiresBase; }
    public static boolean elementalGuaranteed() { return elementalGuaranteed; }
    public static boolean eternalAllOrNothing() { return eternalAllOrNothing; }
    public static int eternalStoneRarityPercent() { return eternalStoneRarityPercent; }

    // Three-stage roll (issue #23).
    public static int deviantPartialChancePercent() { return deviantPartialChancePercent; }
    public static int deviantMaxCompletionPercent() { return deviantMaxCompletionPercent; }
    public static int partialDeviantWeightPercent() { return partialDeviantWeightPercent; }
    public static int elementalContinueChanceStartPercent() { return elementalContinueChanceStartPercent; }
    public static int elementalContinueChanceDecayPercent() { return elementalContinueChanceDecayPercent; }
    public static int elementalMaxCompletionPercent() { return elementalMaxCompletionPercent; }
    public static int elementalMaxIterations() { return elementalMaxIterations; }
    public static double partialCompletionSlope() { return partialCompletionSlope; }

    /**
     * Resolves the rarity of an Affinity Stone. Lazily builds the underlying
     * map on first call so we never touch {@link ModRarities#legendary()} before
     * the FML loader has injected the custom enum constant.
     */
    public static Rarity stoneRarity(Affinity affinity) {
        Map<Affinity, Rarity> map = STONE_RARITIES.get();
        if (map == null) {
            map = buildRarityMap("stones", defaultStoneRarities());
            // Race-tolerant: if another thread won, use their map (same shape).
            if (!STONE_RARITIES.compareAndSet(null, map)) {
                map = STONE_RARITIES.get();
            }
        }
        return map.getOrDefault(affinity, Rarity.EPIC);
    }

    /**
     * Resolves the rarity of an Affinity Shard. Same lazy contract as
     * {@link #stoneRarity(Affinity)}.
     */
    public static Rarity shardRarity(Affinity affinity) {
        Map<Affinity, Rarity> map = SHARD_RARITIES.get();
        if (map == null) {
            map = buildRarityMap("shards", defaultShardRarities());
            if (!SHARD_RARITIES.compareAndSet(null, map)) {
                map = SHARD_RARITIES.get();
            }
        }
        return map.getOrDefault(affinity, Rarity.RARE);
    }
}