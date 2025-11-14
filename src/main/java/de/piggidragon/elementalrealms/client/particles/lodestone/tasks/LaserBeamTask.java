package de.piggidragon.elementalrealms.client.particles.lodestone.tasks;

import de.piggidragon.elementalrealms.client.particles.lodestone.RenderTask;
import de.piggidragon.elementalrealms.client.particles.lodestone.custom.TestParticle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LaserBeamTask implements RenderTask {

    private final Player player;
    private final Level level;
    private final float beamRange;

    public LaserBeamTask(Player player, Level level, float beamRange) {
        this.player = player;
        this.level = level;
        this.beamRange = beamRange;
    }

    @Override
    public void render(float partialTicks) {
        TestParticle.spawnBeamEffect(level,
                player.getEyePosition(partialTicks),
                player.getEyePosition(partialTicks).add(player.getLookAngle().scale(beamRange))
        );
    }
}
