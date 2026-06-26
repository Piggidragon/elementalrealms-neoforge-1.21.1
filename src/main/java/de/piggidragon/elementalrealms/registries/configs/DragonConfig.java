package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/dragon.json}. HP multiplier, phase thresholds,
 * attack configs. Phase 1 fleshes this out.
 */
public final class DragonConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final DragonConfig INSTANCE = new DragonConfig();

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
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("dragon.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("dragon.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — dragon config (JSON5). Phase 1 fleshes this out.
                {
                  "schemaVersion": 1,
                  "scaling": {
                    "hpMultiplier": 1.0,
                    "damageMultiplier": 1.0
                  },
                  "phases": []
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}