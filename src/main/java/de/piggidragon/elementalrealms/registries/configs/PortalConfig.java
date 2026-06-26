package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/portal.json}. Controls portal spawn offsets,
 * search radius, particle cadence, and the explosion used to clear the air column
 * when a natural portal primes.
 */
public final class PortalConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final PortalConfig INSTANCE = new PortalConfig();

    // Effective defaults — values previously hardcoded in PortalEntity.
    private static double spawnHeightOffset = 5.0;
    private static double spawnZOffset = 2.0;
    private static double searchRadius = 128.0;
    private static int particleSpawnIntervalTicks = 5;
    private static int particleCount = 3;
    private static double particleRadius = 0.8;
    private static double particleYOffset = 0.5;
    private static float explosionPower = 25.0f;
    private static double explosionYOffset = 1.0;
    private static double returnOffset = 2.0;

    public PortalConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "portal.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("portal.json not found — wrote defaults. Using in-memory defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("portal.json schema mismatch — keeping in-memory defaults.");
            return;
        }

        JsonObject obj = root.getAsJsonObject();
        if (obj.has("spawn") && obj.get("spawn").isJsonObject()) {
            JsonObject spawn = obj.get("spawn").getAsJsonObject();
            if (spawn.has("heightOffset")) spawnHeightOffset = spawn.get("heightOffset").getAsDouble();
            if (spawn.has("zOffset")) spawnZOffset = spawn.get("zOffset").getAsDouble();
        }
        if (obj.has("search") && obj.get("search").isJsonObject()) {
            JsonObject search = obj.get("search").getAsJsonObject();
            if (search.has("radius")) searchRadius = search.get("radius").getAsDouble();
        }
        if (obj.has("particles") && obj.get("particles").isJsonObject()) {
            JsonObject particles = obj.get("particles").getAsJsonObject();
            if (particles.has("spawnIntervalTicks")) particleSpawnIntervalTicks = particles.get("spawnIntervalTicks").getAsInt();
            if (particles.has("count")) particleCount = particles.get("count").getAsInt();
            if (particles.has("radius")) particleRadius = particles.get("radius").getAsDouble();
            if (particles.has("yOffset")) particleYOffset = particles.get("yOffset").getAsDouble();
        }
        if (obj.has("explosion") && obj.get("explosion").isJsonObject()) {
            JsonObject explosion = obj.get("explosion").getAsJsonObject();
            if (explosion.has("power")) explosionPower = explosion.get("power").getAsFloat();
            if (explosion.has("yOffset")) explosionYOffset = explosion.get("yOffset").getAsDouble();
        }
        if (obj.has("teleport") && obj.get("teleport").isJsonObject()) {
            JsonObject teleport = obj.get("teleport").getAsJsonObject();
            if (teleport.has("returnOffset")) returnOffset = teleport.get("returnOffset").getAsDouble();
        }

        ElementalRealms.LOGGER.debug("portal.json loaded: spawnHeightOffset={}, searchRadius={}, explosionPower={}",
                spawnHeightOffset, searchRadius, explosionPower);
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — portal config (JSON5).
                {
                  "schemaVersion": 1,

                  "spawn": {
                    // Y offset (blocks) added to the surface height where a portal is placed.
                    "heightOffset": 5.0,
                    // Z offset (blocks) added when placing portals (e.g. dragon-death origin portal).
                    "zOffset": 2.0
                  },

                  "search": {
                    // Radius (blocks) within which PortalEntity scans for an existing return portal.
                    "radius": 128.0
                  },

                  "particles": {
                    // Ambient portal particles are spawned every N ticks.
                    "spawnIntervalTicks": 5,
                    "count": 3,
                    "radius": 0.8,
                    "yOffset": 0.5
                  },

                  "explosion": {
                    // Power used to clear the air column when a natural portal primes itself.
                    "power": 25.0,
                    "yOffset": 1.0
                  },

                  "teleport": {
                    // Return-portal offset (blocks) added to the original return position when
                    // teleporting back from a custom dimension, so the player doesn't land inside
                    // the portal hitbox.
                    "returnOffset": 2.0
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }

    public static double spawnHeightOffset() { return spawnHeightOffset; }
    public static double spawnZOffset() { return spawnZOffset; }
    public static double searchRadius() { return searchRadius; }
    public static int particleSpawnIntervalTicks() { return particleSpawnIntervalTicks; }
    public static int particleCount() { return particleCount; }
    public static double particleRadius() { return particleRadius; }
    public static double particleYOffset() { return particleYOffset; }
    public static float explosionPower() { return explosionPower; }
    public static double explosionYOffset() { return explosionYOffset; }
    public static double returnOffset() { return returnOffset; }
}