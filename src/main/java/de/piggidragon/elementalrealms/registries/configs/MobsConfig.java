package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/mobs.json}. Affinity-mob and modded-mob
 * spawn rules. Phase 6 fills in concrete weights; Phase 0 only stands up the loader.
 */
public final class MobsConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final MobsConfig INSTANCE = new MobsConfig();

    public MobsConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "mobs.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("mobs.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("mobs.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — mobs config (JSON5). Phase 6 fleshes this out.
                {
                  "schemaVersion": 1,
                  "affinityMobs": {
                    "spawnRateMultiplier": 1.0,
                    "tierGating": {
                      "elementalUnlocked": true,
                      "deviantUnlockedAfterFirstElementalBoss": true,
                      "eternalUnlockedAfterFirstDeviantBoss": true
                    }
                  },
                  "moddedMobs": {
                    "pocketDensityMultiplier": 1.0
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}