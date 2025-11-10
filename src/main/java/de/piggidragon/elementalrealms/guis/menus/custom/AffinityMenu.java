package de.piggidragon.elementalrealms.guis.menus.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.guis.menus.ModMenus;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu for displaying player affinities with completion percentages
 * Has two constructors: one for server (with data), one for client (with buffer)
 */
public class AffinityMenu extends AbstractContainerMenu {

    // List to store all affinity data with completion percentages
    private final List<AffinityData> affinities;

    /**
     * CLIENT constructor - receives data from server via FriendlyByteBuf
     * Called automatically by Minecraft when the menu is opened on client side
     *
     * @param containerId The container ID
     * @param playerInv The player's inventory
     * @param extraData Additional data containing affinity list from server
     */
    public AffinityMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);

        ElementalRealms.LOGGER.info("=== CLIENT MENU CONSTRUCTOR CALLED ===");

        // Read all affinities with their completion percentages from buffer
        int count = extraData.readInt();
        ElementalRealms.LOGGER.info("=== READING " + count + " AFFINITIES FROM BUFFER ===");

        this.affinities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Affinity affinity = extraData.readEnum(Affinity.class);
            int completion = extraData.readInt(); // 0-100 percentage
            this.affinities.add(new AffinityData(affinity, completion));
            ElementalRealms.LOGGER.info("Read affinity: " + affinity + " = " + completion + "%");
        }

        ElementalRealms.LOGGER.info("=== CLIENT MENU CONSTRUCTOR FINISHED ===");
    }

    /**
     * SERVER constructor - receives data directly as a list
     * Called by SimpleMenuProvider on server side when creating the menu
     *
     * @param containerId The container ID
     * @param playerInv The player's inventory
     * @param affinities The list of affinity data to display
     */
    public AffinityMenu(int containerId, Inventory playerInv, List<AffinityData> affinities) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);

        ElementalRealms.LOGGER.info("=== SERVER MENU CONSTRUCTOR CALLED WITH " + affinities.size() + " AFFINITIES ===");

        // Store the affinity data directly (no need to read from buffer)
        this.affinities = new ArrayList<>(affinities);

        ElementalRealms.LOGGER.info("=== SERVER MENU CONSTRUCTOR FINISHED ===");
    }

    /**
     * Gets the list of all affinities with their completion percentages
     * @return List of affinity data
     */
    public List<AffinityData> getAffinities() {
        return this.affinities;
    }

    /**
     * Gets only completed affinities (100% completion)
     * @return List of completed affinities
     */
    public List<AffinityData> getCompletedAffinities() {
        return this.affinities.stream()
                .filter(AffinityData::isCompleted)
                .toList();
    }

    /**
     * Gets only incomplete affinities (< 100% completion)
     * @return List of incomplete affinities
     */
    public List<AffinityData> getIncompleteAffinities() {
        return this.affinities.stream()
                .filter(data -> !data.isCompleted())
                .toList();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // No slots in this menu, so shift-clicking does nothing
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // Always valid since this is a player-specific display GUI
        return true;
    }

    /**
     * Data class to hold affinity information
     * Stores affinity type and completion percentage
     */
    public static class AffinityData {
        private final Affinity affinity;
        private final int completionPercent; // 0-100

        /**
         * Creates a new affinity data entry
         * @param affinity The affinity type
         * @param completionPercent The completion percentage (0-100)
         */
        public AffinityData(Affinity affinity, int completionPercent) {
            this.affinity = affinity;
            this.completionPercent = Math.clamp(completionPercent, 0, 100);
        }

        /**
         * Gets the affinity type
         * @return The affinity type
         */
        public Affinity getAffinity() {
            return affinity;
        }

        /**
         * Gets the completion percentage
         * @return Completion percentage (0-100)
         */
        public int getCompletionPercent() {
            return completionPercent;
        }

        /**
         * Checks if this affinity is completed
         * @return True if completion is 100%
         */
        public boolean isCompleted() {
            return completionPercent >= 100;
        }
    }
}
