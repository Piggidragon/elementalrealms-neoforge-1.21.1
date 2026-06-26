package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/school.json}. Controls School-dimension
 * access flow: the Dimension Staff's beam animation, portal lifespan, and the
 * search radius it uses to clean up old portals before spawning a new one.
 */
public final class SchoolConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final SchoolConfig INSTANCE = new SchoolConfig();

    // Effective defaults — values previously hardcoded in SchoolStaff.
    private static int portalDespawnTicks = 200;
    private static double portalSpawnDistance = 2.0;
    private static double portalSpawnHeight = 0.5;
    private static double staffTipDistance = 0.8;
    private static int portalSearchRadius = 1000;
    private static int beamTotalTicks = 40;

    public SchoolConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "school.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("school.json not found — wrote defaults. Using in-memory defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("school.json schema mismatch — keeping in-memory defaults.");
            return;
        }

        JsonObject obj = root.getAsJsonObject();
        if (obj.has("dimensionStaff") && obj.get("dimensionStaff").isJsonObject()) {
            JsonObject staff = obj.get("dimensionStaff").getAsJsonObject();
            if (staff.has("portalDespawnTicks")) portalDespawnTicks = staff.get("portalDespawnTicks").getAsInt();
            if (staff.has("portalSpawnDistance")) portalSpawnDistance = staff.get("portalSpawnDistance").getAsDouble();
            if (staff.has("portalSpawnHeight")) portalSpawnHeight = staff.get("portalSpawnHeight").getAsDouble();
            if (staff.has("staffTipDistance")) staffTipDistance = staff.get("staffTipDistance").getAsDouble();
            if (staff.has("portalSearchRadius")) portalSearchRadius = staff.get("portalSearchRadius").getAsInt();
            if (staff.has("beamTotalTicks")) beamTotalTicks = staff.get("beamTotalTicks").getAsInt();
        }

        ElementalRealms.LOGGER.debug("school.json loaded: portalDespawnTicks={}, beamTotalTicks={}",
                portalDespawnTicks, beamTotalTicks);
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — school config (JSON5).
                // Phase 2 fleshes out crystalOrb + further dimension-staff options.
                {
                  "schemaVersion": 1,

                  "dimensionStaff": {
                    // Lifespan (ticks) of portals spawned by the Dimension Staff.
                    "portalDespawnTicks": 200,
                    // Distance (blocks) in front of the player the portal spawns at.
                    "portalSpawnDistance": 2.0,
                    // Y offset (blocks) added to the player's Y when placing the portal.
                    "portalSpawnHeight": 0.5,
                    // Distance (blocks) in front of the player's eyes the staff-tip beam starts at.
                    "staffTipDistance": 0.8,
                    // Radius (blocks) the staff searches for the player's previous portals to remove.
                    "portalSearchRadius": 1000,
                    // How many ticks the beam animation lasts before the portal actually spawns.
                    "beamTotalTicks": 40
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }

    public static int portalDespawnTicks() { return portalDespawnTicks; }
    public static double portalSpawnDistance() { return portalSpawnDistance; }
    public static double portalSpawnHeight() { return portalSpawnHeight; }
    public static double staffTipDistance() { return staffTipDistance; }
    public static int portalSearchRadius() { return portalSearchRadius; }
    public static int beamTotalTicks() { return beamTotalTicks; }
}