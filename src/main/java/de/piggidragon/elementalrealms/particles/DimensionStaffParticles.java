package de.piggidragon.elementalrealms.particles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Particle effects for Dimension Staff usage and durability indication.
 */
public class DimensionStaffParticles {

    /**
     * Spawns enchantment particles scaled by remaining staff durability.
     * Visual feedback for staff condition (3-13 particles based on durability).
     *
     * @param level  Server level for particles
     * @param player Center of effect
     * @param staff  Staff item to check durability
     */
    public static void addDurabilityEffects(ServerLevel level, Player player, ItemStack staff) {
        int maxDamage = staff.getMaxDamage();
        int currentDamage = staff.getDamageValue();
        float durabilityPercent = 1.0f - ((float) currentDamage / maxDamage);

        // More particles = better condition
        int particleCount = (int) (durabilityPercent * 10) + 3;

        for (int i = 0; i < particleCount; i++) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
