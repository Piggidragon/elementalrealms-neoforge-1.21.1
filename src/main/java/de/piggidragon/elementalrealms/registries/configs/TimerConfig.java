package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/timer.json}. Corrupted-world timer difficulty.
 * Phase 8 endgame content fills in concrete numbers.
 */
public final class TimerConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final TimerConfig INSTANCE = new TimerConfig();

    public TimerConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "timer.json";
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
            ElementalRealms.LOGGER.warn("timer.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("timer.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — timer config (JSON5). Phase 8 endgame fleshes this out.
                {
                  "schemaVersion": 1,
                  "corruptedWorld": {
                    "enabled": true,
                    "difficultyRampPerDay": 0.1
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}