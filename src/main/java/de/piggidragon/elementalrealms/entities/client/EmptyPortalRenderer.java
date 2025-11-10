package de.piggidragon.elementalrealms.entities.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Empty renderer for PortalEntity.
 * Actual rendering is done through Lodestone particle effects.
 */
public class EmptyPortalRenderer extends EntityRenderer<PortalEntity> {

    /**
     * Creates an empty portal renderer.
     *
     * @param context the renderer context
     */
    public EmptyPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Renders nothing - portal uses particle effects instead.
     */
    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Intentionally empty - rendering handled by Lodestone particles
    }

    /**
     * Returns a dummy texture location (never used).
     */
    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/empty.png");
    }
}
