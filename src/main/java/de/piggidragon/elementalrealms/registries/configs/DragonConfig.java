package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/dragon.json}. Controls the stationary-player
 * laser attack behaviour injected via {@code EnderDragonMixin}.
 */
public final class DragonConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final DragonConfig INSTANCE = new DragonConfig();

    // Effective defaults — values previously hardcoded in EnderDragonMixin.
    private static int laserCheckIntervalTicks = 60;
    private static double laserCheckRadius = 1.5;
    private static double laserDetectionRange = 100.0;
    private static int laserCooldownTicks = 100;
    private static double laserSoundHearingRange = 100.0;
    private static float laserSoundVolume = 5.0F;
    private static float laserSoundPitch = 1.0F;

    public DragonConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "dragon.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("dragon.json not found — wrote defaults. Using in-memory defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("dragon.json schema mismatch — keeping in-memory defaults.");
            return;
        }

        JsonObject obj = root.getAsJsonObject();
        if (obj.has("laser")) {
            JsonObject laser = obj.getAsJsonObject("laser");
            laserCheckIntervalTicks = Json5SectionReader.getInt(laser, "checkIntervalTicks", laserCheckIntervalTicks);
            laserCheckRadius = Json5SectionReader.getDouble(laser, "checkRadius", laserCheckRadius);
            laserDetectionRange = Json5SectionReader.getDouble(laser, "detectionRange", laserDetectionRange);
            laserCooldownTicks = Json5SectionReader.getInt(laser, "cooldownTicks", laserCooldownTicks);
            laserSoundHearingRange = Json5SectionReader.getDouble(laser, "soundHearingRange", laserSoundHearingRange);
            laserSoundVolume = Json5SectionReader.getFloat(laser, "soundVolume", laserSoundVolume);
            laserSoundPitch = Json5SectionReader.getFloat(laser, "soundPitch", laserSoundPitch);
        }

        ElementalRealms.LOGGER.debug("dragon.json loaded: laserCheckIntervalTicks={}, laserDetectionRange={}",
                laserCheckIntervalTicks, laserDetectionRange);
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — dragon config (JSON5).
                // HP / damage multipliers stay in the TOML layer; this file holds the
                // per-attack behaviour injected by EnderDragonMixin.
                {
                  "schemaVersion": 1,

                  "laser": {
                    // How often (ticks) the dragon checks for stationary players within checkRadius.
                    "checkIntervalTicks": 60,
                    // Radius (blocks) the player must stay within for the dragon to count them as "still".
                    "checkRadius": 1.5,
                    // Range (blocks) at which the dragon starts scanning players for the laser attack.
                    "detectionRange": 100.0,
                    // Cooldown (ticks) before the dragon can target the same player again.
                    "cooldownTicks": 100,
                    // Range (blocks) at which nearby players hear the laser beam sound.
                    "soundHearingRange": 100.0,
                    "soundVolume": 5.0,
                    "soundPitch": 1.0
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }

    public static int laserCheckIntervalTicks() { return laserCheckIntervalTicks; }
    public static double laserCheckRadius() { return laserCheckRadius; }
    public static double laserDetectionRange() { return laserDetectionRange; }
    public static int laserCooldownTicks() { return laserCooldownTicks; }
    public static double laserSoundHearingRange() { return laserSoundHearingRange; }
    public static float laserSoundVolume() { return laserSoundVolume; }
    public static float laserSoundPitch() { return laserSoundPitch; }
}