package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/dimensions.json}. Controls pocket-dimension
 * sizes, ring layout, dimensional effects, and the affinity-to-pocket map.
 * <p>
 * Concrete numbers are deliberately marked TBD here — they get tuned during
 * the Phase 4 build. Phase 0 only stands up the loading machinery.
 */
public final class DimensionsConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final DimensionsConfig INSTANCE = new DimensionsConfig();

    // Effective defaults — placeholders, not locked-in numbers (PLANS.md §13 / §15).
    private static int pocketOuterRingRadius = 64;
    private static int pocketIntermediateRingRadius = 128;
    private static int pocketCenterRadius = 32;
    private static int schoolDimensionRadius = 96;
    private static int boundedChunkRadius = 10;
    private static int maxLayers = 100;
    private static int maxGenerationAttempts = 10000;
    private static boolean bossDeathRemovesVanillaPortal = true;
    private static boolean pocketsPersistent = true;
    private static int dimensionalEffectStrengthPercent = 50;
    private static int affinityBuffThresholdPercent = 100;

    public DimensionsConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "dimensions.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("dimensions.json not found — wrote defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("dimensions.json schema mismatch — keeping in-memory defaults.");
            return;
        }

        JsonObject obj = root.getAsJsonObject();
        if (obj.has("pocket")) {
            JsonObject pocket = obj.getAsJsonObject("pocket");
            pocketOuterRingRadius = Json5SectionReader.getInt(pocket, "outerRingRadius", pocketOuterRingRadius);
            pocketIntermediateRingRadius = Json5SectionReader.getInt(pocket, "intermediateRingRadius", pocketIntermediateRingRadius);
            pocketCenterRadius = Json5SectionReader.getInt(pocket, "centerRadius", pocketCenterRadius);
            boundedChunkRadius = Json5SectionReader.getInt(pocket, "boundedChunkRadius", boundedChunkRadius);
        }
        if (obj.has("school")) {
            JsonObject school = obj.getAsJsonObject("school");
            schoolDimensionRadius = Json5SectionReader.getInt(school, "dimensionRadius", schoolDimensionRadius);
        }
        if (obj.has("generation")) {
            JsonObject generation = obj.getAsJsonObject("generation");
            maxLayers = Json5SectionReader.getInt(generation, "maxLayers", maxLayers);
            maxGenerationAttempts = Json5SectionReader.getInt(generation, "maxGenerationAttempts", maxGenerationAttempts);
        }
        if (obj.has("behaviour")) {
            JsonObject behaviour = obj.getAsJsonObject("behaviour");
            bossDeathRemovesVanillaPortal = Json5SectionReader.getBoolean(behaviour, "bossDeathRemovesVanillaPortal", bossDeathRemovesVanillaPortal);
            pocketsPersistent = Json5SectionReader.getBoolean(behaviour, "pocketsPersistent", pocketsPersistent);
        }
        if (obj.has("effects")) {
            JsonObject effects = obj.getAsJsonObject("effects");
            dimensionalEffectStrengthPercent = Json5SectionReader.getInt(effects, "dimensionalEffectStrengthPercent", dimensionalEffectStrengthPercent);
            affinityBuffThresholdPercent = Json5SectionReader.getInt(effects, "affinityBuffThresholdPercent", affinityBuffThresholdPercent);
        }

        ElementalRealms.LOGGER.debug("dimensions.json loaded: boundedChunkRadius={}, maxLayers={}, maxGenerationAttempts={}",
                boundedChunkRadius, maxLayers, maxGenerationAttempts);
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — dimensions config (JSON5).
                // Concrete numbers are TBD until Phase 4 (pocket build). Phase 0 only stands up the loader.

                {
                  "schemaVersion": 1,

                  "pocket": {
                    // Concentric ring radii (in blocks) inside each pocket dimension.
                    // Player spawns at outer, fights inward to the center boss arena.
                    "outerRingRadius": 64,
                    "intermediateRingRadius": 128,
                    "centerRadius": 32,
                    // Chunk radius each pocket's BoundedChunkGenerator keeps generated. Past this,
                    // chunks are air-filled (void). Used by both BoundedChunkGenerator + ring layout.
                    "boundedChunkRadius": 10
                  },

                  "school": {
                    "dimensionRadius": 96
                  },

                  "generation": {
                    // Max number of concentric rings DynamicDimensionHandler will scan for a free
                    // generation center before giving up with an IllegalStateException.
                    "maxLayers": 100,
                    // Cap on generation-center placement attempts; reached -> bail with the error
                    // message above. Effectively a safety net around the ring-walk loop.
                    "maxGenerationAttempts": 10000
                  },

                  "behaviour": {
                    // When a boss dies, the matching vanilla portal disappears (encourages variety).
                    "bossDeathRemovesVanillaPortal": true,
                    // Boss-killed pockets stay killed. false = regenerate on portal re-entry.
                    "pocketsPersistent": true
                  },

                  "effects": {
                    // Strength (%) of the negative passive dimensional effect when player has
                    // matching affinity. At 100% matching affinity, effects flip to buffs.
                    "dimensionalEffectStrengthPercent": 50,
                    "affinityBuffThresholdPercent": 100
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }

    public static int pocketOuterRingRadius() { return pocketOuterRingRadius; }
    public static int pocketIntermediateRingRadius() { return pocketIntermediateRingRadius; }
    public static int pocketCenterRadius() { return pocketCenterRadius; }
    public static int schoolDimensionRadius() { return schoolDimensionRadius; }
    public static int boundedChunkRadius() { return boundedChunkRadius; }
    public static int maxLayers() { return maxLayers; }
    public static int maxGenerationAttempts() { return maxGenerationAttempts; }
    public static boolean bossDeathRemovesVanillaPortal() { return bossDeathRemovesVanillaPortal; }
    public static boolean pocketsPersistent() { return pocketsPersistent; }
    public static int dimensionalEffectStrengthPercent() { return dimensionalEffectStrengthPercent; }
    public static int affinityBuffThresholdPercent() { return affinityBuffThresholdPercent; }
}