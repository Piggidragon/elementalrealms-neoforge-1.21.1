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
        if (obj.has("pocket") && obj.get("pocket").isJsonObject()) {
            JsonObject pocket = obj.get("pocket").getAsJsonObject();
            if (pocket.has("outerRingRadius")) pocketOuterRingRadius = pocket.get("outerRingRadius").getAsInt();
            if (pocket.has("intermediateRingRadius")) pocketIntermediateRingRadius = pocket.get("intermediateRingRadius").getAsInt();
            if (pocket.has("centerRadius")) pocketCenterRadius = pocket.get("centerRadius").getAsInt();
            if (pocket.has("boundedChunkRadius")) boundedChunkRadius = pocket.get("boundedChunkRadius").getAsInt();
        }
        if (obj.has("school") && obj.get("school").isJsonObject()) {
            JsonObject school = obj.get("school").getAsJsonObject();
            if (school.has("dimensionRadius")) schoolDimensionRadius = school.get("dimensionRadius").getAsInt();
        }
        if (obj.has("generation") && obj.get("generation").isJsonObject()) {
            JsonObject generation = obj.get("generation").getAsJsonObject();
            if (generation.has("maxLayers")) maxLayers = generation.get("maxLayers").getAsInt();
            if (generation.has("maxGenerationAttempts")) maxGenerationAttempts = generation.get("maxGenerationAttempts").getAsInt();
        }
        if (obj.has("behaviour") && obj.get("behaviour").isJsonObject()) {
            JsonObject behaviour = obj.get("behaviour").getAsJsonObject();
            if (behaviour.has("bossDeathRemovesVanillaPortal")) bossDeathRemovesVanillaPortal = behaviour.get("bossDeathRemovesVanillaPortal").getAsBoolean();
            if (behaviour.has("pocketsPersistent")) pocketsPersistent = behaviour.get("pocketsPersistent").getAsBoolean();
        }
        if (obj.has("effects") && obj.get("effects").isJsonObject()) {
            JsonObject effects = obj.get("effects").getAsJsonObject();
            if (effects.has("dimensionalEffectStrengthPercent")) dimensionalEffectStrengthPercent = effects.get("dimensionalEffectStrengthPercent").getAsInt();
            if (effects.has("affinityBuffThresholdPercent")) affinityBuffThresholdPercent = effects.get("affinityBuffThresholdPercent").getAsInt();
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