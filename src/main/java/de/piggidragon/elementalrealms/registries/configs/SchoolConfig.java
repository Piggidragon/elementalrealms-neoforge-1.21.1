package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/school.json}. School dimension + Crystal Orb
 * + Dimension Staff tunables. Phase 2 fleshes this out.
 */
public final class SchoolConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final SchoolConfig INSTANCE = new SchoolConfig();

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
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("school.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("school.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — school config (JSON5). Phase 2 fleshes this out.
                {
                  "schemaVersion": 1,
                  "crystalOrb": {
                    "revealAllOnUse": true
                  },
                  "dimensionStaff": {
                    "maxUses": -1
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}