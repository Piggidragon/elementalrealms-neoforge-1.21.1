package de.piggidragon.elementalrealms.client.rendering.tasks;

public interface TickTask {

    /**
     * Called every tick for logic updates (20 times per second)
     */
    default void tick() {
        // Optional: Override in implementations that need tick logic
    }
}
