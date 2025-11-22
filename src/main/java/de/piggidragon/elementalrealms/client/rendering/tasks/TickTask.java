package de.piggidragon.elementalrealms.client.rendering.tasks;

/**
 * Interface for tasks requiring logic updates every game tick (20 TPS).
 * Used for position tracking, collision detection, and state updates.
 */
public interface TickTask {
    /**
     * Updates task logic every game tick.
     * Called 20 times per second for consistent behavior.
     */
    void tick();
}
