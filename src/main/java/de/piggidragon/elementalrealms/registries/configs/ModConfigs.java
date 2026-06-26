package de.piggidragon.elementalrealms.registries.configs;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Registers the three NeoForge TOML {@link ModConfigSpec} files for the mod.
 * <p>
 * Layer 1 of the configuration subsystem (see PLANS.md §18.2 / §19):
 * TOML is reserved for binary toggles and simple multipliers. Complex nested
 * tables live in JSON5 files loaded via {@link Json5ConfigLoader}.
 * <p>
 * Layer 2 (JSON5 files) is loaded independently from TOML. The reload command
 * re-reads the JSON5 layer; the TOML layer is managed by NeoForge's built-in
 * reload pipeline ({@code ModConfigEvent.Reloading}).
 */
public final class ModConfigs {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final CommonConfig COMMON;
    public static final ServerConfig SERVER;
    public static final ClientConfig CLIENT;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        COMMON = new CommonConfig(common);
        COMMON_SPEC = common.build();

        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        SERVER = new ServerConfig(server);
        SERVER_SPEC = server.build();

        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        CLIENT = new ClientConfig(client);
        CLIENT_SPEC = client.build();
    }

    private ModConfigs() {
    }

    /**
     * Registers all three config specs with the mod container.
     * Must be called from the main mod constructor with the {@code ModContainer}
     * (not the mod event bus — config registration goes through the container).
     */
    public static void register(ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, COMMON_SPEC);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, CLIENT_SPEC);

        ElementalRealms.LOGGER.info("Registered elementalrealms configs (common/server/client TOML).");
    }

    /**
     * Hook for tests / scripts that don't have a real {@link ModContainer}.
     * Only validates the specs can be built without throwing — does not register.
     */
    public static void initForTesting() {
        // Touch the static fields so they are initialized.
        var c = COMMON_SPEC;
        var s = SERVER_SPEC;
        var cl = CLIENT_SPEC;
        ElementalRealms.LOGGER.debug("ModConfigs specs initialized for testing: common={}, server={}, client={}",
                c == COMMON_SPEC, s == SERVER_SPEC, cl == CLIENT_SPEC);
    }

    /**
     * Subscribes the config-reload listener to the given event bus.
     * Currently a no-op because {@link ConfigReloadListener} uses
     * {@code @EventBusSubscriber} on the mod bus, which NeoForge wires up
     * automatically when the mod class loads. Kept for symmetry with the
     * {@link #register(ModContainer)} call site so future manual registration
     * (e.g. for a GameTest-only bus) has an obvious hook.
     */
    public static void subscribeReloadListener(IEventBus modBus) {
        ElementalRealms.LOGGER.debug("ConfigReloadListener auto-subscribed via @EventBusSubscriber on bus: {}", modBus);
    }
}