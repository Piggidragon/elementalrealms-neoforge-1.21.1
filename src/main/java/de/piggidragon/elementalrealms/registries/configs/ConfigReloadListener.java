package de.piggidragon.elementalrealms.registries.configs;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.List;

/**
 * Listens for NeoForge's TOML config events and re-reads the JSON5 layer on each
 * load/reload. The TOML spec values themselves are live on read (NeoForge handles
 * the reload automatically), so we only need this hook to refresh the JSON5 files
 * in lockstep.
 * <p>
 * Lazy-apply semantics: existing entities / live data keep their snapshot. New
 * reads go through the freshly loaded config. This avoids mid-tick mutation bugs
 * when the reload happens during gameplay (see PLANS.md §18.2).
 * <p>
 * Also exposed as a public method {@link #reloadAllJson5()} that the
 * {@code /elementalrealms reload} command invokes directly for an in-game test
 * path that doesn't require touching a TOML file.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ConfigReloadListener {

    private ConfigReloadListener() {
    }

    /**
     * Fired by NeoForge when a TOML config first loads. We trigger a full
     * JSON5 reload so the two layers are in sync from the start.
     */
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        ElementalRealms.LOGGER.debug("TOML config loaded: {}", configEvent.getConfig().getFileName());
        reloadAllJson5();
    }

    /**
     * Fired by NeoForge when a TOML config is edited on disk. Re-reads the JSON5
     * layer so all config-backed systems pick up the latest values.
     */
    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        ElementalRealms.LOGGER.info("TOML config reloaded: {}", configEvent.getConfig().getFileName());
        reloadAllJson5();
    }

    /**
     * Called by the reload command. Re-reads every JSON5 config file from disk
     * and refreshes the static holders. Safe to call repeatedly.
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