package de.piggidragon.elementalrealms.client.particles.lodestone.tasks;

import de.piggidragon.elementalrealms.client.particles.lodestone.RenderTask;
import de.piggidragon.elementalrealms.client.particles.lodestone.custom.TestParticle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(partialTicks);

        TestParticle.spawnBeamEffect(level,
                eyePos.add(lookVec.scale(1.2)),
                eyePos.add(player.getLookAngle().scale(beamRange))
        );
    }
}
