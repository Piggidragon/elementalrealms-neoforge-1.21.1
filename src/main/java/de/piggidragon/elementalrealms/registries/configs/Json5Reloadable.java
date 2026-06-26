package de.piggidragon.elementalrealms.registries.configs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tag interface for any JSON5-backed config loader. Loaders self-register on class
 * init so {@link ConfigReloadListener#reloadAllJson5()} can iterate them.
 * <p>
 * Each loader is responsible for reading its file via {@link Json5ConfigLoader},
 * validating the schema version, applying values to its static fields, and falling
 * back to defaults on any failure.
 */
public interface Json5Reloadable {

    /** Default registry of all known loaders. Loaders add themselves in their static initializer. */
    List<Json5Reloadable> REGISTRY = new CopyOnWriteArrayList<>();

    /** Filename relative to {@code config/elementalrealms/}, e.g. {@code "affinities.json"}. */
    String configFileName();

    /** Trigger a reload from disk. Implementations must be idempotent. */
    void reload();

    /** Convenience: snapshot of all registered loaders. Safe to iterate without locking. */
    static List<Json5Reloadable> all() {
        return REGISTRY;
    }
}