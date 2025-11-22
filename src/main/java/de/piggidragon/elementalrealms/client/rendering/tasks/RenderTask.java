package de.piggidragon.elementalrealms.client.rendering.tasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Interface for tasks that require custom rendering every frame.
 * Implementations handle visual effects like laser beams or particle systems.
 */
public interface RenderTask {
    /**
     * Renders the task with interpolation for smooth visuals.
     *
     * @param partialTicks      Interpolation value between 0.0 and 1.0 for smooth animation
     * @param poseStack         The pose stack for rendering transformations
     * @param multiBufferSource The buffer source for rendering
     */
    void render(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource);
}
