package de.piggidragon.elementalrealms.registries.rarities;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

/**
 * Custom rarity tiers that sit above vanilla EPIC.
 * Registered through NeoForge's enum extension system.
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
}
