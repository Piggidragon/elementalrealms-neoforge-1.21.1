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
        if (obj.has("spawn")) {
            JsonObject spawn = obj.getAsJsonObject("spawn");
            spawnHeightOffset = Json5SectionReader.getDouble(spawn, "heightOffset", spawnHeightOffset);
            spawnZOffset = Json5SectionReader.getDouble(spawn, "zOffset", spawnZOffset);
        }
        if (obj.has("search")) {
            JsonObject search = obj.getAsJsonObject("search");
            searchRadius = Json5SectionReader.getDouble(search, "radius", searchRadius);
        }
        if (obj.has("particles")) {
            JsonObject particles = obj.getAsJsonObject("particles");
            particleSpawnIntervalTicks = Json5SectionReader.getInt(particles, "spawnIntervalTicks", particleSpawnIntervalTicks);
            particleCount = Json5SectionReader.getInt(particles, "count", particleCount);
            particleRadius = Json5SectionReader.getDouble(particles, "radius", particleRadius);
            particleYOffset = Json5SectionReader.getDouble(particles, "yOffset", particleYOffset);
        }
        if (obj.has("explosion")) {
            JsonObject explosion = obj.getAsJsonObject("explosion");
            explosionPower = Json5SectionReader.getFloat(explosion, "power", explosionPower);
            explosionYOffset = Json5SectionReader.getDouble(explosion, "yOffset", explosionYOffset);
        }
        if (obj.has("teleport")) {
            JsonObject teleport = obj.getAsJsonObject("teleport");
            returnOffset = Json5SectionReader.getDouble(teleport, "returnOffset", returnOffset);
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