package de.piggidragon.elementalrealms.registries.entities.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.registries.entities.custom.PortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * No-op renderer for {@link PortalEntity}. The portal's visuals come from
 * Lodestone particles spawned by the entity itself, not from a model.
 */
public class EmptyPortalRenderer extends EntityRenderer<PortalEntity> {

    public EmptyPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Intentionally empty - rendering handled by Lodestone particles.
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/empty.png");
    }
}
