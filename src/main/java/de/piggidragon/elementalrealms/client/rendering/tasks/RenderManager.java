package de.piggidragon.elementalrealms.client.rendering.tasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe registry for client-side render and tick tasks.
 * Removal is deferred to {@link #removeRequestedTasks()} so background threads can flag
 * a task for removal without risking concurrent modification during render.
 */
public final class RenderManager {

    private static final List<RenderTask> RENDER_TASKS = new ArrayList<>();
    private static final List<TickTask> TICK_TASKS = new ArrayList<>();
    private static final List<TickTask> TICK_TASKS_TO_REMOVE = new ArrayList<>();
    private static final List<RenderTask> RENDER_TASKS_TO_REMOVE = new ArrayList<>();

    private RenderManager() {
    }

    public static void addTickTask(TickTask task) {
        synchronized (TICK_TASKS) {
            TICK_TASKS.add(task);
        }
    }

    public static void addRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS) {
            RENDER_TASKS.add(task);
        }
    }

    public static void executeAll(float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        synchronized (RENDER_TASKS) {
            if (RENDER_TASKS.isEmpty()) return;
            for (RenderTask task : RENDER_TASKS) {
                task.render(partialTicks, poseStack, bufferSource);
            }
        }
    }

    public static void tickAll() {
        synchronized (TICK_TASKS) {
            for (TickTask task : TICK_TASKS) {
                task.tick();
            }
        }
    }

    public static void requestRemoveTickTask(TickTask task) {
        synchronized (TICK_TASKS_TO_REMOVE) {
            TICK_TASKS_TO_REMOVE.add(task);
        }
    }

    public static void requestRemoveRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS_TO_REMOVE) {
            RENDER_TASKS_TO_REMOVE.add(task);
        }
    }

    public static void removeRequestedTasks() {
        synchronized (TICK_TASKS) {
            if (!TICK_TASKS_TO_REMOVE.isEmpty()) {
                TICK_TASKS.removeAll(TICK_TASKS_TO_REMOVE);
                TICK_TASKS_TO_REMOVE.clear();
            }
        }
        synchronized (RENDER_TASKS) {
            if (!RENDER_TASKS_TO_REMOVE.isEmpty()) {
                RENDER_TASKS.removeAll(RENDER_TASKS_TO_REMOVE);
                RENDER_TASKS_TO_REMOVE.clear();
            }
        }
    }

    public static boolean hasTickTask(TickTask task) {
        synchronized (TICK_TASKS) {
            return TICK_TASKS.contains(task);
        }
    }

    public static boolean hasRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS) {
            return RENDER_TASKS.contains(task);
        }
    }
}
