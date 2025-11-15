package de.piggidragon.elementalrealms.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.particles.lodestone.RenderManager;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.client.EmptyPortalRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Client-side event handlers for entity renderers.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    /**
     * Registers entity renderers for custom entities.
     *
     * @param event the renderer registration event
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register empty renderer for portal (actual rendering via Lodestone particles)
        event.registerEntityRenderer(ModEntities.PORTAL_ENTITY.get(), EmptyPortalRenderer::new);
    }

    /**
     * Handles rendering of particles every frame (60+ FPS)
     * @param event The render level stage event
     */
    @SubscribeEvent
    public static void renderEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();

        // Render all tasks with interpolation
        RenderManager.executeAll(partialTick, poseStack, bufferSource);
        bufferSource.endBatch();
    }

    /**
     * Handles tick logic for particles (20 TPS)
     * @param event The level tick event
     */
    @SubscribeEvent
    public static void onClientTick(LevelTickEvent.Post event) {
        // Only run on client side
        if (!event.getLevel().isClientSide()) return;

        // Tick all tasks for logic updates (position tracking, damage, etc.)
        RenderManager.tickAll();
    }
}
