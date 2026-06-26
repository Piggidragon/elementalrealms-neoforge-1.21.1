package de.piggidragon.elementalrealms.registries.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Convenience accessors for the JSON5 config files. Centralises the
 * "object has key + key is the right shape + default on miss" pattern that
 * each config's {@code reload()} body would otherwise repeat five times.
 * <p>
 * Every getter is null-safe: a missing section, a wrong-type field, or a
 * missing file all fall through to the supplied default rather than throwing.
 * This matches the JSON5-loader contract — see {@link Json5ConfigLoader}.
 */
final class Json5SectionReader {

    private final JsonObject root;

    Json5SectionReader(JsonObject root) {
        this.root = root;
    }

    JsonObject section(String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) {
            return new JsonObject();
        }
        return root.getAsJsonObject(key);
    }

    int getInt(String key, int defaultValue) {
        return getInt(root, key, defaultValue);
    }

    double getDouble(String key, double defaultValue) {
        return getDouble(root, key, defaultValue);
    }

    float getFloat(String key, float defaultValue) {
        return getFloat(root, key, defaultValue);
    }

    boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(root, key, defaultValue);
    }

    int[] getIntArray(String key, int[] defaultValue) {
        JsonElement el = root.get(key);
        if (el == null || !el.isJsonArray()) {
            return defaultValue;
        }
        JsonArray arr = el.getAsJsonArray();
        int[] out = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            try {
                out[i] = arr.get(i).getAsInt();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return out;
    }

    static int getInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key)) return defaultValue;
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static double getDouble(JsonObject obj, String key, double defaultValue) {
        if (obj == null || !obj.has(key)) return defaultValue;
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static float getFloat(JsonObject obj, String key, float defaultValue) {
        if (obj == null || !obj.has(key)) return defaultValue;
        try {
            return obj.get(key).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (obj == null || !obj.has(key)) return defaultValue;
        try {
            return obj.get(key).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}