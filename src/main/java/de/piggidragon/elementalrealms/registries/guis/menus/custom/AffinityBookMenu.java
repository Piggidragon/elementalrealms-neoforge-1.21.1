package de.piggidragon.elementalrealms.registries.guis.menus.custom;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.guis.menus.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
public class AffinityBookMenu extends AbstractContainerMenu {

    // List to store all affinity data with completion percentages
    private final List<AffinityData> affinities;

    /**
     * CLIENT constructor - receives data from server via FriendlyByteBuf
     * Called automatically by Minecraft when the menu is opened on client side
     *
     * @param containerId The container ID
     * @param extraData   Additional data containing affinity list from server
     */
    public AffinityBookMenu(int containerId, FriendlyByteBuf extraData) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);

        // Read all affinities with their completion percentages from buffer
        int count = extraData.readInt();

        this.affinities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Affinity affinity = extraData.readEnum(Affinity.class);
            int completion = extraData.readInt(); // 0-100 percentage
            this.affinities.add(new AffinityData(affinity, completion));
        }
    }

    /**
     * SERVER constructor - receives data directly as a list
     * Called by SimpleMenuProvider on server side when creating the menu
     *
     * @param containerId The container ID
     * @param affinities  The list of affinity data to display
     */
    public AffinityBookMenu(int containerId, List<AffinityData> affinities) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);

        // Store the affinity data directly (no need to read from buffer)
        this.affinities = new ArrayList<>(affinities);
    }

    public AffinityBookMenu(int i, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(i, registryFriendlyByteBuf);
    }

    /**
     * Gets the list of all affinities with their completion percentages
     *
     * @return List of affinity data
     */
    public List<AffinityData> getAffinities() {
        return this.affinities;
    }

    /**
     * Gets only completed affinities (100% completion)
     *
     * @return List of completed affinities
     */
    public List<AffinityData> getCompletedAffinities() {
        return this.affinities.stream()
                .filter(AffinityData::isCompleted)
                .toList();
    }

    /**
     * Gets only incomplete affinities (< 100% completion)
     *
     * @return List of incomplete affinities
     */
    public List<AffinityData> getIncompleteAffinities() {
        return this.affinities.stream()
                .filter(data -> !data.isCompleted())
                .toList();
    }

    // No slot transfers needed for this display-only GUI
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // No slots in this menu, so shift-clicking does nothing
        return ItemStack.EMPTY;
    }

    // Always valid as this is a display-only GUI
    @Override
    public boolean stillValid(Player player) {
        // Always valid since this is a player-specific display GUI
        return true;
    }

    /**
     * Data class to hold affinity information
     * Stores affinity type and completion percentage
     *
     * @param completionPercent 0-100
     */
    public record AffinityData(Affinity affinity, int completionPercent) {
        /**
         * Creates a new affinity data entry
         *
         * @param affinity          The affinity type
         * @param completionPercent The completion percentage (0-100)
         */
        public AffinityData(Affinity affinity, int completionPercent) {
            this.affinity = affinity;
            this.completionPercent = Math.clamp(completionPercent, 0, 100);
        }

        /**
         * Gets the affinity type
         *
         * @return The affinity type
         */
        @Override
        public Affinity affinity() {
            return affinity;
        }

        /**
         * Gets the completion percentage
         *
         * @return Completion percentage (0-100)
         */
        @Override
        public int completionPercent() {
            return completionPercent;
        }

        /**
         * Checks if this affinity is completed
         *
         * @return True if completion is 100%
         */
        public boolean isCompleted() {
            return completionPercent >= 100;
        }
    }
}
