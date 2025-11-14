package de.piggidragon.elementalrealms.client.particles.lodestone;

import java.util.ArrayList;
import java.util.List;

public class LodestoneParticleManager {
    public static final List<RenderTask> tasks = new ArrayList<>();

    public static void addTask(RenderTask task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public static void executeAll(float partialTicks) {
        synchronized (tasks) {
            for (RenderTask task : tasks) {
                task.render(partialTicks);
            }
        }
    }

    public static void removeTask(RenderTask task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }
}
