package de.piggidragon.elementalrealms.client.particles.lodestone;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

public class RenderManager {
    public static final List<RenderTask> TASKS = new ArrayList<>();
    public static final List<RenderTask> TASKS_TO_REMOVE = new ArrayList<>();

    /**
     * Adds a new render task to the manager
     *
     * @param task The task to add
     */
    public static void addTask(RenderTask task) {
        synchronized (TASKS) {
            TASKS.add(task);
        }
    }

    /**
     * Executes all render tasks (called every frame)
     *
     * @param partialTicks      Interpolation value between 0.0 and 1.0
     * @param poseStack         The pose stack for rendering
     * @param multiBufferSource The buffer source for rendering
     */
    public static void executeAll(float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        synchronized (TASKS) {
            if (TASKS.isEmpty()) return;
            for (RenderTask task : TASKS) {
                task.render(partialTicks, poseStack, multiBufferSource);
            }
        }
    }

    /**
     * Ticks all render tasks (called every tick - 20 times per second)
     * This is where logic updates happen, separate from rendering
     */
    public static void tickAll() {
        synchronized (TASKS) {
            for (RenderTask task : TASKS) {
                task.tick();
            }
        }
    }

    /**
     * Removes a render task from the manager
     *
     * @param task The task to remove
     */
    public static void requestRemoveTask(RenderTask task) {
        synchronized (TASKS) {
            TASKS_TO_REMOVE.add(task);
        }
    }

    public static void removeRequestedTasks() {
        synchronized (TASKS) {
            if (TASKS_TO_REMOVE.isEmpty()) return;
            TASKS.removeAll(TASKS_TO_REMOVE);
            TASKS_TO_REMOVE.clear();
        }
    }

    /**
     * Checks if a task is currently registered
     *
     * @param task The task to check
     * @return true if the task is registered
     */
    public static boolean hasTask(RenderTask task) {
        synchronized (TASKS) {
            return TASKS.contains(task);
        }
    }
}
