package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/portal.json}. Portal spawn rates, lifetime,
 * visual parameters, portal-to-affinity routing rules. Filled in during Phase 2 + 4.
 */
public final class PortalConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final PortalConfig INSTANCE = new PortalConfig();

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
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("portal.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("portal.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — portal config (JSON5).
                {
                  "schemaVersion": 1,
                  "spawning": {
                    "vanillaSpawnRateMultiplier": 1.0,
                    "minDistanceBetweenPortals": 256
                  },
                  "routing": {
                    "randomSourceWeight": 1.0
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}