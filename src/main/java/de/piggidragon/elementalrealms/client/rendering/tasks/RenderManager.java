package de.piggidragon.elementalrealms.client.rendering.tasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages client-side rendering and tick tasks.
 * Provides thread-safe registration and execution of custom visual effects.
 */
public class RenderManager {
    public static final List<RenderTask> RENDER_TASKS = new ArrayList<>();
    public static final List<TickTask> TICK_TASKS = new ArrayList<>();
    public static final List<TickTask> TICK_TASKS_TO_REMOVE = new ArrayList<>();
    public static final List<RenderTask> RENDER_TASKS_TO_REMOVE = new ArrayList<>();

    /**
     * Adds a new tick task to the manager.
     *
     * @param task The task to add
     */
    public static void addTickTask(TickTask task) {
        synchronized (TICK_TASKS) {
            TICK_TASKS.add(task);
        }
    }

    /**
     * Adds a new render task to the manager.
     *
     * @param task The task to add
     */
    public static void addRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS) {
            RENDER_TASKS.add(task);
        }
    }

    /**
     * Executes all render tasks (called every frame).
     *
     * @param partialTicks      Interpolation value between 0.0 and 1.0
     * @param poseStack         The pose stack for rendering
     * @param multiBufferSource The buffer source for rendering
     */
    public static void executeAll(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        synchronized (RENDER_TASKS) {
            if (RENDER_TASKS.isEmpty()) return;
            for (RenderTask task : RENDER_TASKS) {
                task.render(partialTicks, poseStack, multiBufferSource);
            }
        }
    }

    /**
     * Ticks all render tasks (called every tick - 20 times per second).
     * This is where logic updates happen, separate from rendering.
     */
    public static void tickAll() {
        synchronized (TICK_TASKS) {
            for (TickTask task : TICK_TASKS) {
                task.tick();
            }
        }
    }

    /**
     * Marks a tick task for removal.
     *
     * @param task The task to remove
     */
    public static void requestRemoveTickTask(TickTask task) {
        synchronized (TICK_TASKS_TO_REMOVE) {
            TICK_TASKS_TO_REMOVE.add(task);
        }
    }

    /**
     * Marks a render task for removal.
     *
     * @param task The task to remove
     */
    public static void requestRemoveRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS_TO_REMOVE) {
            RENDER_TASKS_TO_REMOVE.add(task);
        }
    }

    /**
     * Removes all marked tasks from their respective lists.
     * Called at the start of each frame to prevent concurrent modification.
     */
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

    /**
     * Checks if a tick task is currently registered.
     *
     * @param task The task to check
     * @return true if the task is registered
     */
    public static boolean hasTickTask(TickTask task) {
        synchronized (TICK_TASKS) {
            return TICK_TASKS.contains(task);
        }
    }

    /**
     * Checks if a render task is currently registered.
     *
     * @param task The task to check
     * @return true if the task is registered
     */
    public static boolean hasRenderTask(RenderTask task) {
        synchronized (RENDER_TASKS) {
            return RENDER_TASKS.contains(task);
        }
    }
}
