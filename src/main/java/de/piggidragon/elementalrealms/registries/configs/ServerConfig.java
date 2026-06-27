package de.piggidragon.elementalrealms.registries.configs;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * TOML config: server-side only. Multipliers and gating for host-controlled
 * behavior. Anything data-table-shaped lives in JSON5 instead.
 */
public final class ServerConfig {

    public final ModConfigSpec.BooleanValue allowAffinityRerollCommand;
    public final ModConfigSpec.IntValue maxRerollsPerPlayer;
    public final ModConfigSpec.IntValue rollCooldownSeconds;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Elemental Realms — server-only config").push("server");

        allowAffinityRerollCommand = builder
                .comment("If true, players with permission can use /affinities reroll.")
                .define("allowAffinityRerollCommand", true);

        maxRerollsPerPlayer = builder
                .comment("Max rerolls a single player can perform per session. -1 = unlimited.")
                .defineInRange("maxRerollsPerPlayer", -1, -1, 1000);

        rollCooldownSeconds = builder
                .comment("Cooldown between rerolls for a single player, in seconds.")
                .defineInRange("rollCooldownSeconds", 0, 0, 86400);

        builder.pop();
    }
}