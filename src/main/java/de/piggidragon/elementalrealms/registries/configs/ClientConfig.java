package de.piggidragon.elementalrealms.registries.configs;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * TOML config: client-only. Visual toggles and sliders.
 */
public final class ClientConfig {

    public final ModConfigSpec.BooleanValue showManaBar;
    public final ModConfigSpec.DoubleValue particleMultiplier;
    public final ModConfigSpec.DoubleValue uiScale;

    public ClientConfig(ModConfigSpec.Builder builder) {
        builder.comment("Elemental Realms — client-only config").push("client");

        showManaBar = builder
                .comment("If true, the mana bar HUD overlay is drawn (placeholder until Phase 3).")
                .define("showManaBar", true);

        particleMultiplier = builder
                .comment("Global particle density multiplier. 1.0 = default.")
                .defineInRange("particleMultiplier", 1.0, 0.0, 4.0);

        uiScale = builder
                .comment("UI element scale multiplier. 1.0 = default.")
                .defineInRange("uiScale", 1.0, 0.5, 2.0);

        builder.pop();
    }
}