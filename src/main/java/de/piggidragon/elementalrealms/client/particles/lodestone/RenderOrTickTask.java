package de.piggidragon.elementalrealms.client.particles.lodestone;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderOrTickTask {
    /**
     * Called every frame for rendering (60+ times per second)
     *
     * @param partialTicks      Interpolation value between 0.0 and 1.0
     * @param poseStack         The pose stack for rendering
     * @param multiBufferSource The buffer source for rendering
     */
    default void render(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource){
    }

    /**
     * Called every tick for logic updates (20 times per second)
     */
    default void tick() {
        // Optional: Override in implementations that need tick logic
    }
}
