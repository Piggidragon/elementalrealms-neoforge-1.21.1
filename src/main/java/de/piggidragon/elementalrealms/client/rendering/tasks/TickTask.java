package de.piggidragon.elementalrealms.client.rendering.tasks;

/**
 * Updates task logic once per game tick (20 TPS). Used for state tracking independent of rendering.
 */
public interface TickTask {
    void tick();
}
