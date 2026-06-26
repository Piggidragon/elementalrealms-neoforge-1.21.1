package de.piggidragon.elementalrealms.registries.rarities;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

/**
 * Custom rarity tiers that sit above vanilla EPIC.
 *
 * <p>Registered through NeoForge's enum extension system (see
 * {@code META-INF/enumextensions.json}). The {@link EnumProxy} constants are the
 * recipe; the actual {@link Rarity} instances are injected into the {@code Rarity}
 * enum at runtime by the FML loader. Use {@link #legendary()} and {@link #mythic()}
 * to obtain the live instances — those wrappers centralise the lazy lookup so any
 * "Enum not initialized" error points at one place.</p>
 *
 * <p>Resolution is safe at item-registration time: by then the FML loader has
 * already injected the constants, so {@link EnumProxy#getValue()} returns the
 * real instance. Do not call {@code getValue()} from a {@code static {}} block
 * that runs before FML bootstrap — it will throw.</p>
 */
public final class ModRarities {

    public static final EnumProxy<Rarity> LEGENDARY = new EnumProxy<>(
            Rarity.class,
            -1,
            "elementalrealms:legendary",
            (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.GOLD)
    );
    public static final EnumProxy<Rarity> MYTHIC = new EnumProxy<>(
            Rarity.class,
            -1,
            "elementalrealms:mythic",
            (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.DARK_PURPLE)
    );

    private ModRarities() {
    }

    public static Rarity legendary() {
        return LEGENDARY.getValue();
    }

    public static Rarity mythic() {
        return MYTHIC.getValue();
    }
}