package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/enchantments.json}. Per-enchantment
 * multipliers for the global nerf pass. Phase 0.5 (enchantment nerf) fleshes
 * this out alongside the corresponding mixins.
 */
public final class EnchantmentsConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final EnchantmentsConfig INSTANCE = new EnchantmentsConfig();

    public EnchantmentsConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "enchantments.json";
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
            ElementalRealms.LOGGER.warn("enchantments.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("enchantments.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — enchantments config (JSON5). Phase 0.5 fleshes this out.
                // Multipliers applied on top of vanilla values: <1 = nerf, >1 = buff.
                {
                  "schemaVersion": 1,
                  "protection": {
                    "perLevelMultiplier": 1.0
                  },
                  "sharpness": {
                    "perLevelMultiplier": 1.0
                  },
                  "sweeping": {
                    "perLevelMultiplier": 1.0
                  },
                  "smite": {
                    "perLevelMultiplier": 1.0
                  },
                  "baneOfArthropods": {
                    "perLevelMultiplier": 1.0
                  },
                  "ominousPotionScaling": {
                    "difficultyMultiplier": 1.0
                  }
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}