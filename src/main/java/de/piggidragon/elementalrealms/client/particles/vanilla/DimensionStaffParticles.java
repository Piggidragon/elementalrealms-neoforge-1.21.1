package de.piggidragon.elementalrealms.client.particles.vanilla;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Particle bursts for the dimension staff. Particle count scales with remaining durability.
 */
public final class DimensionStaffParticles {

    private static final float SCALE = 10.0f;
    private static final int MIN_PARTICLES = 3;

    private DimensionStaffParticles() {
    }

    public static void addDurabilityEffects(ServerLevel level, Player player, ItemStack staff) {
        float durabilityPercent = 1.0f - ((float) staff.getDamageValue() / staff.getMaxDamage());
        int particleCount = (int) (durabilityPercent * SCALE) + MIN_PARTICLES;

        for (int i = 0; i < particleCount; i++) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
