package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonElement;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.nio.file.Path;

/**
 * Loads {@code config/elementalrealms/spells.json}. Spell definitions, cooldowns,
 * mana costs, VFX hooks, combo rules. Phase 3 fleshes this out.
 */
public final class SpellsConfig implements Json5Reloadable {

    public static final int SCHEMA_VERSION = 1;
    public static final SpellsConfig INSTANCE = new SpellsConfig();

    public SpellsConfig() {
        REGISTRY.add(this);
        reload();
    }

    @Override
    public String configFileName() {
        return "spells.json";
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
            ElementalRealms.LOGGER.warn("spells.json schema mismatch — keeping stub defaults.");
            return;
        }
        ElementalRealms.LOGGER.debug("spells.json loaded (stub).");
    }

    private static void writeDefaultIfMissing(Path file) {
        String content = """
                // Elemental Realms — spells config (JSON5). Phase 3 fleshes this out.
                {
                  "schemaVersion": 1,
                  "global": {
                    "baseManaPool": 100,
                    "manaRegenPerSecond": 2
                  },
                  "perSpell": {}
                }
                """;
        Json5ConfigLoader.writeDefault(file, content);
    }
}