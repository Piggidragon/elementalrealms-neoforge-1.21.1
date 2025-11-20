package de.piggidragon.elementalrealms.registries.items.magic.misc.custom;

import de.piggidragon.elementalrealms.client.particles.lodestone.RenderManager;
import de.piggidragon.elementalrealms.client.particles.lodestone.tasks.LaserBeamTask;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * LaserStaff item that spawns a smooth particle beam using Lodestone
 * Uses partialTick interpolation for smooth camera-based effects
 */
public class LaserStaff extends Item {

    private static final int reach = 20;

    public LaserStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) {
            LaserBeamTask laserBeamTask = new LaserBeamTask(player, level, reach, 50, 10f, 100, 10);
            if (!RenderManager.hasTask(laserBeamTask)) {
                RenderManager.addTask(laserBeamTask);
            }
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }
}
