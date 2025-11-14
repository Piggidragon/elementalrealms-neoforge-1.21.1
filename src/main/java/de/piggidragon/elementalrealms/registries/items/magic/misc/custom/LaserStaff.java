package de.piggidragon.elementalrealms.registries.items.magic.misc.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.particles.lodestone.LodestoneParticleManager;
import de.piggidragon.elementalrealms.client.particles.lodestone.tasks.LaserBeamTask;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
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
    private LaserBeamTask laserBeamTask;

    public LaserStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) {
            laserBeamTask = new LaserBeamTask(player, level, reach);
            if (!LodestoneParticleManager.hasTask(laserBeamTask)){
                LodestoneParticleManager.addTask(laserBeamTask);
                ElementalRealms.LOGGER.info("Add " + laserBeamTask.toString());
            }
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(player.getItemInHand(usedHand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide) {
            LodestoneParticleManager.removeTask(laserBeamTask);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}
