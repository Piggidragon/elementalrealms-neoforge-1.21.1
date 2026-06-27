package de.piggidragon.elementalrealms.registries.configs;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * TOML config: shared between client and server. Trivial toggles and multipliers.
 * <p>
 * Anything gameplay-balance related goes into JSON5 (see PLANS.md §18.2). This
 * file is intentionally kept minimal so modpack authors don't have to dig here
 * for the actual numbers.
 */
public final class CommonConfig {

    public final ModConfigSpec.BooleanValue debugLogging;
    public final ModConfigSpec.BooleanValue enableDevTools;
    public final ModConfigSpec.IntValue schemaMismatchBehavior;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Elemental Realms — common config (shared client + server)").push("common");

        debugLogging = builder
                .comment("Enable verbose debug logging (very chatty; not for normal play).")
                .define("debugLogging", false);

        enableDevTools = builder
                .comment("Enable /elementalrealms dev subcommands. Off in production builds.")
                .define("enableDevTools", false);

        schemaMismatchBehavior = builder
                .comment("How to behave when a JSON5 config has the wrong schemaVersion. " +
                        "0 = log warning + use defaults. 1 = throw + refuse to load.")
                .defineInRange("schemaMismatchBehavior", 0, 0, 1);

        builder.pop();
    }
}