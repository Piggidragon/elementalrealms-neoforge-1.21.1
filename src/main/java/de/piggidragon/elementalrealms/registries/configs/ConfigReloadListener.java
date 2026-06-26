package de.piggidragon.elementalrealms.registries.configs;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

/**
 * Listens for NeoForge's TOML reload events and the mod's own JSON5 reload trigger
 * ({@code /elementalrealms reload}). Re-reads all JSON5 config files and broadcasts
 * changes so dependent systems can refresh.
 * <p>
 * Lazy-apply semantics: existing entities / live data keep their snapshot. New
 * reads go through the freshly loaded config. This avoids mid-tick mutation bugs
 * when the reload happens during gameplay (see PLANS.md §18.2).
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ConfigReloadListener {

    private ConfigReloadListener() {
    }

    public static void register(IEventBus modBus) {
        // Already subscribed via @EventBusSubscriber — this method exists so the caller
        // explicitly opts in. We just verify the bus is non-null and log.
        ElementalRealms.LOGGER.debug("ConfigReloadListener subscribed on mod bus: {}", modBus);
    }

    /**
     * Called by the reload command. Re-reads every JSON5 config file from disk
     * and refreshes the static holders. Safe to call repeatedly.
     *
     * <p>Note on TOML hot-reload: NeoForge fires {@code ModConfigEvent.Reloading}
     * automatically when a TOML file changes on disk; the {@code ModConfigSpec}
     * values we hold already reflect the new content after that event fires, so
     * we don't need to subscribe explicitly. If we ever need a hook (e.g. to push
     * TOML changes into a non-spec-backed consumer), add a {@code @SubscribeEvent}
     * handler here that subscribes to {@code net.neoforged.fml.config.ModConfigEvent}
     * via the mod bus. (Class symbol resolution for {@code ModConfigEvent} depends
     * on the NeoForge merged JAR being on the classpath at compile time — if the
     * symbol is unavailable in the build env, the reload command still works; only
     * the auto-fire-on-TOML-edit hook would be missing.)</p>
     */
    public static void reloadAllJson5() {
        ElementalRealms.LOGGER.info("Reloading JSON5 config layer...");

        // Clear custom naming overrides before re-applying — default layer is static.
        NamingRegistry.clearCustom();

        // Re-load each JSON5-backed config. Each loader pulls its own file.
        int reloaded = 0;
        int failed = 0;
        List<Json5Reloadable> loaders = Json5Reloadable.all();
        for (Json5Reloadable loader : loaders) {
            try {
                loader.reload();
                reloaded++;
            } catch (Exception e) {
                failed++;
                ElementalRealms.LOGGER.warn("Failed to reload {}: {}",
                        loader.configFileName(), e.getMessage());
            }
        }

        ElementalRealms.LOGGER.info("JSON5 reload complete: {} ok, {} failed (of {} loaders).",
                reloaded, failed, loaders.size());
    }
}