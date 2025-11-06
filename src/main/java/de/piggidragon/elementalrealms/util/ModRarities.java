package de.piggidragon.elementalrealms.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

/**
 * Custom rarity tiers extending Minecraft's vanilla rarity system.
 * Provides higher tiers for advanced affinity items.
 */
public class ModRarities {

    /**
     * LEGENDARY rarity (gold text) for deviant affinity stones.
     * Higher tier than vanilla's EPIC rarity.
     */
    public static final EnumProxy<Rarity> LEGENDARY = new EnumProxy<>(
            Rarity.class,
            -1,
            "elementalrealms:legendary",
            (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.GOLD)
    );

    /**
     * MYTHIC rarity (dark purple text) for eternal affinity stones.
     * Highest tier representing ultimate power.
     */
    public static final EnumProxy<Rarity> MYTHIC = new EnumProxy<>(
            Rarity.class,
            -1,
            "elementalrealms:mythic",
            (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.DARK_PURPLE)
    );
}
