package de.piggidragon.elementalrealms.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.client.renderer.misc.PortalEmptyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Client-only entity renderer registration and per-frame render/tick task dispatch.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PORTAL_ENTITY.get(), PortalEmptyRenderer::new);
    }

    @SubscribeEvent
    public static void renderEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        RenderManager.removeRequestedTasks();

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();

        RenderManager.executeAll(partialTick, poseStack, bufferSource);
    }

    @SubscribeEvent
    public static void onClientTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) return;

        RenderManager.removeRequestedTasks();
        RenderManager.tickAll();
    }
}
