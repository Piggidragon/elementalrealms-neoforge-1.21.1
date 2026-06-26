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
    private static boolean deviantRequiresBase = true;
    private static boolean elementalGuaranteed = true;
    private static boolean eternalAllOrNothing = true;
    private static int eternalStoneRarityPercent = 5;

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
        if (obj.has("roll") && obj.get("roll").isJsonObject()) {
            JsonObject roll = obj.get("roll").getAsJsonObject();
            if (roll.has("slotChances") && roll.get("slotChances").isJsonArray()) {
                int[] parsed = new int[roll.get("slotChances").getAsJsonArray().size()];
                int i = 0;
                for (JsonElement e : roll.get("slotChances").getAsJsonArray()) {
                    parsed[i++] = e.getAsInt();
                }
                if (parsed.length > 0) slotChances = parsed;
            }
            if (roll.has("elementalGuaranteed")) {
                elementalGuaranteed = roll.get("elementalGuaranteed").getAsBoolean();
            }
            if (roll.has("deviantChancePercent")) {
                deviantChancePercent = roll.get("deviantChancePercent").getAsInt();
            }
        }
        if (obj.has("tiers")) {
            JsonObject tiers = obj.get("tiers").getAsJsonObject();
            if (tiers.has("deviantRequiresBase")) {
                deviantRequiresBase = tiers.get("deviantRequiresBase").getAsBoolean();
            }
            if (tiers.has("eternalAllOrNothing")) {
                eternalAllOrNothing = tiers.get("eternalAllOrNothing").getAsBoolean();
            }
        }
        if (obj.has("drops")) {
            JsonObject drops = obj.get("drops").getAsJsonObject();
            if (drops.has("eternalStoneRarityPercent")) {
                eternalStoneRarityPercent = drops.get("eternalStoneRarityPercent").getAsInt();
            }
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
                    "deviantChancePercent": 25
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
    public static boolean deviantRequiresBase() { return deviantRequiresBase; }
    public static boolean elementalGuaranteed() { return elementalGuaranteed; }
    public static boolean eternalAllOrNothing() { return eternalAllOrNothing; }
    public static int eternalStoneRarityPercent() { return eternalStoneRarityPercent; }
}