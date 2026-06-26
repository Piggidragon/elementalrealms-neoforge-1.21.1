package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.piggidragon.elementalrealms.ElementalRealms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lightweight JSON5 loader for the mod's config files in {@code config/elementalrealms/}.
 * <p>
 * JSON5 differs from plain JSON only by:
 * <ul>
 *   <li>{@code //} and {@code /* *}{@code /} comments</li>
 *   <li>trailing commas in arrays and objects</li>
 * </ul>
 * We strip both before handing the string to Gson. Schema versioning is enforced
 * via a top-level {@code schemaVersion} field. Unknown extra keys are tolerated
 * and logged at debug level so modpack authors can add their own fields safely.
 * <p>
 * If the file is missing, the caller is expected to fall back to its hardcoded
 * defaults (we don't auto-write defaults — that's the responsibility of the
 * owning config class so it can control formatting + comments).
 */
public final class Json5ConfigLoader {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Json5ConfigLoader() {
    }

    /**
     * Loads a JSON5 file from disk, stripping JSON5-only syntax before parsing.
     * Returns {@code null} if the file doesn't exist or fails to parse (with a logged warning).
     */
    public static JsonElement load(Path file) {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            String raw = Files.readString(file);
            String stripped = stripComments(raw);
            String deTrailed = stripTrailingCommas(stripped);
            return JsonParser.parseString(deTrailed);
        } catch (IOException | JsonParseException e) {
            ElementalRealms.LOGGER.warn("Failed to load JSON5 config {}: {}", file, e.getMessage());
            return null;
        }
    }

    /**
     * Validates that {@code root} has the expected {@code schemaVersion}. Logs and returns false
     * if mismatched. Behaviour when mismatch occurs is controlled by
     * {@link CommonConfig#schemaMismatchBehavior}: 0 = warn + use defaults, 1 = refuse.
     */
    public static boolean validateSchema(JsonElement root, int expectedVersion) {
        if (root == null || !root.isJsonObject()) {
            return false;
        }
        if (!root.getAsJsonObject().has("schemaVersion")) {
            ElementalRealms.LOGGER.warn("JSON5 config missing schemaVersion field; assuming v{}", expectedVersion);
            return true;
        }
        int actual;
        try {
            actual = root.getAsJsonObject().get("schemaVersion").getAsInt();
        } catch (Exception e) {
            ElementalRealms.LOGGER.warn("JSON5 config schemaVersion is not an integer");
            return false;
        }
        if (actual != expectedVersion) {
            ElementalRealms.LOGGER.warn("JSON5 config schemaVersion mismatch: file={}, expected={}",
                    actual, expectedVersion);
            // schemaMismatchBehavior lives in the TOML ModConfig, which is not
            // loaded yet during early static-init (e.g. when Json5Reloadable
            // singletons trigger their first reload() from a `static {}` block).
            // Reading the ConfigValue before load throws IllegalStateException;
            // default to behavior 0 (warn + use in-memory defaults) in that
            // window. After TOML loads the real value is consulted on subsequent
            // reload() calls fired by ModConfigEvent.Reloading.
            int behavior = 0;
            try {
                behavior = ModConfigs.COMMON.schemaMismatchBehavior.get();
            } catch (IllegalStateException ignored) {
                // ModConfig not loaded yet - default behaviour applies.
            }
            return behavior == 0;
        }
        return true;
    }

    /**
     * Strips {@code //...} and {@code /* ... *}{@code /} comments that are NOT inside string
     * literals. Pure state-machine parser — no regex needed.
     */
    static String stripComments(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        int len = input.length();
        boolean inString = false;
        while (i < len) {
            char c = input.charAt(i);
            if (!inString && c == '/' && i + 1 < len) {
                char next = input.charAt(i + 1);
                if (next == '/') {
                    // Single-line comment: skip until newline.
                    i += 2;
                    while (i < len && input.charAt(i) != '\n' && input.charAt(i) != '\r') {
                        i++;
                    }
                    continue;
                }
                if (next == '*') {
                    // Block comment: skip until closing */.
                    i += 2;
                    while (i + 1 < len && !(input.charAt(i) == '*' && input.charAt(i + 1) == '/')) {
                        i++;
                    }
                    i = Math.min(len, i + 2);
                    continue;
                }
            }
            if (c == '"') {
                inString = !inString;
            } else if (c == '\\' && inString && i + 1 < len) {
                // Escaped char inside a string: keep both this char and the next.
                sb.append(c).append(input.charAt(i + 1));
                i += 2;
                continue;
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    /**
     * Removes trailing commas before {@code }} or {@code ]}. Naive but sufficient for the
     * well-formed config files we control.
     */
    static String stripTrailingCommas(String input) {
        return input.replaceAll(",(\\s*[}\\]])", "$1");
    }

    /**
     * Writes a default config to disk with JSON5-compatible formatting. Caller controls
     * the actual content (typically a hand-written string).
     */
    public static void writeDefault(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content);
        } catch (IOException e) {
            ElementalRealms.LOGGER.warn("Failed to write default JSON5 config {}: {}", file, e.getMessage());
        }
    }

    /**
     * Pretty-prints a {@link JsonElement} and writes it to {@code file}.
     * Used by the reload command when "save effective config back to disk" is desired
     * (not implemented yet — placeholder for future tooling).
     */
    public static void save(Path file, JsonElement root) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, PRETTY.toJson(root));
        } catch (IOException e) {
            ElementalRealms.LOGGER.warn("Failed to save JSON5 config {}: {}", file, e.getMessage());
        }
    }

    /**
     * Convenience: get the resolved TOML config directory for this mod's ModConfig.
     * The {@code config} subfolder next to the main config dir holds our JSON5 files.
     */
    public static Path getConfigDir() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get()
                .resolve(ElementalRealms.MODID);
    }

    /**
     * Convenience: full path to a named JSON5 file in the mod's config dir.
     */
    public static Path resolve(String fileName) {
        return getConfigDir().resolve(fileName);
    }

    }