package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/bosses.json}. Per-affinity boss stats, phase
 * transitions, drops. Schema deliberately minimal in Phase 0; gets fleshed out
 * during Phase 5 (boss build).
 */
public final class BossesConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final BossesConfig INSTANCE = new BossesConfig();

    public BossesConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "bosses.json";
    }

    @Override
    public void reload() {
        Path file = Json5ConfigLoader.resolve(configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null) {
            writeDefaultIfMissing(file);
            ElementalRealms.LOGGER.debug("bosses.json not found — wrote stub defaults.");
            return;
        }
        if (!Json5ConfigLoader.validateSchema(root, SCHEMA_VERSION)) {
            ElementalRealms.LOGGER.warn("bosses.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("bosses.json loaded (stub — no fields applied in Phase 0).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — bosses config (JSON5). Phase 5 fleshes this out.
                {
                  "schemaVersion": 1,
                  "globalMultipliers": {
                    "healthMultiplier": 1.0,
                    "damageMultiplier": 1.0,
                    "xpMultiplier": 1.0
                  },
                  "perAffinity": {}
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}