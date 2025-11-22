package de.piggidragon.elementalrealms.registries.guis.hud;

/**
 * Manages the state and animation of the Affinity Hotbar.
 * Controls fade-in and fade-out with cubic interpolation.
 */
public class AffinityHotbarManager {
    /**
     * Duration of slide animation in milliseconds
     */
    private static final int SLIDE_DURATION = 500;

    /**
     * Whether the custom hotbar is currently displayed
     */
    private static boolean replaced = false;

    /**
     * Start time of the animation in milliseconds
     */
    private static long animationStartTime = 0;

    /**
     * Whether the animation is currently fading in (true) or out (false)
     */
    private static boolean slidingIn = true;

    /**
     * Toggles between normal and Affinity hotbar.
     * Blocks toggling during active animation.
     */
    public static void toggle() {
        // Block toggle during animation
        if (isAnimating()) {
            return;
        }

        // Start new animation
        animationStartTime = System.currentTimeMillis();
        slidingIn = !replaced;
        replaced = !replaced;
    }

    /**
     * @return true if the custom hotbar is currently displayed
     */
    public static boolean isReplaced() {
        return replaced;
    }

    /**
     * @return true if an animation is currently running
     */
    public static boolean isAnimating() {
        return System.currentTimeMillis() - animationStartTime < SLIDE_DURATION;
    }

    /**
     * Calculates the interpolation progress of the slide animation.
     * Uses cubic easing for smooth movement.
     *
     * @return Progress between 0.0 (start) and 1.0 (end)
     */
    public static float getSlideProgress() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(elapsed / (float) SLIDE_DURATION, 1.0f);

        // Cubic easing: ease-in when sliding in, ease-out when sliding out
        if (slidingIn) {
            return 1 - (float) Math.pow(1 - progress, 3);
        } else {
            return (float) Math.pow(progress, 3);
        }
    }
}
