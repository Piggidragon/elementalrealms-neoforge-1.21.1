package de.piggidragon.elementalrealms.client.rendering.tasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderTask {
    void render(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource);
}
