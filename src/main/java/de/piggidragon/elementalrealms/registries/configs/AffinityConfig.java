package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/affinities.json}. Controls the login roll
 * distribution and per-affinity tunables.
 * <p>
 * The values mirror what was previously hardcoded in {@code ModAffinitiesRoll}
 * (Phase-0.3 will swap the live callers to read from this config).
 */
public final class AffinityConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;

    // Effective values, refreshed by reload().
    private static int[] slotChances = {100, 25, 20, 20};
    private static int deviantChancePercent = 25;
    private static int maxCompletionPercent = 100;
    private static boolean deviantRequiresBase = true;
    private static boolean elementalGuaranteed = true;
    private static boolean eternalAllOrNothing = true;
    private static int eternalStoneRarityPercent = 5;

    // Three-stage roll (issue #23).
    // Stage 1 always fires: 1 guaranteed ELEMENTAL at 100% (hardcoded).
    // Stage 2: rare roll for matching DEVIANT at partial completion (left-skewed).
    // Stage 3: decaying loop for additional ELEMENTALs / DEVIANTs (left-skewed).
    // Stage 2 + 3 partials use a continuous left-skew distribution
    // (completion = (int)(maxCompletion * U^slope)) with implicit int-cast truncation.
    private static int deviantPartialChancePercent = 10;
    private static int deviantMaxCompletionPercent = 80;
    private static int partialDeviantWeightPercent = 15;
    private static int elementalContinueChanceStartPercent = 50;
    private static int elementalContinueChanceDecayPercent = 50;
    private static int elementalMaxCompletionPercent = 80;
    private static int elementalMaxIterations = 5;
    // Exponential left-skew: completion = (int)(maxCompletion * U^slope).
    // slope=1 -> uniform, slope=3 -> ~79% <= 40% (default), slope>3 -> more extreme.
    private static double partialCompletionSlope = 3.0;

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
            ElementalRealms.LOGGER.debug("affinities.json not found — wrote defaults. Using in-memory defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("affinities.json schema mismatch — keeping in-memory defaults.");
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

        ElementalRealms.LOGGER.debug("affinities.json loaded: slotChances={}, deviantChance={}",
                java.util.Arrays.toString(slotChances), deviantChancePercent);
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — affinities config (JSON5: comments + trailing commas OK)
                // Edit and run /elementalrealms reload to apply without restart.

                {
                  "schemaVersion": 1,

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
                    // completion = round(maxCompletion * U^slope) — see partialCompletionSlope.
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
}