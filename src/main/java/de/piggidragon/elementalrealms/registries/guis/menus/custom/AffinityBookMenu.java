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
 * Display-only menu that shows the player's affinities and completion percentages.
 * Has a server constructor (data passed as a list) and a client constructor (data read from buffer).
 */
public class AffinityBookMenu extends AbstractContainerMenu {

    private final List<AffinityData> affinities;

    public AffinityBookMenu(int containerId, FriendlyByteBuf extraData) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);

        int count = extraData.readInt();
        this.affinities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Affinity affinity = extraData.readEnum(Affinity.class);
            int completion = extraData.readInt();
            this.affinities.add(new AffinityData(affinity, completion));
        }
    }

    public AffinityBookMenu(int containerId, List<AffinityData> affinities) {
        super(ModMenus.AFFINITY_MENU.get(), containerId);
        this.affinities = new ArrayList<>(affinities);
    }

    public AffinityBookMenu(int i, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(i, buf);
    }

    public List<AffinityData> getAffinities() {
        return this.affinities;
    }

    public List<AffinityData> getCompletedAffinities() {
        return this.affinities.stream()
                .filter(AffinityData::isCompleted)
                .toList();
    }

    public List<AffinityData> getIncompleteAffinities() {
        return this.affinities.stream()
                .filter(data -> !data.isCompleted())
                .toList();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Affinity with its completion percentage, clamped to 0-100.
     */
    public record AffinityData(Affinity affinity, int completionPercent) {

        public AffinityData {
            completionPercent = Math.clamp(completionPercent, 0, 100);
        }

        public boolean isCompleted() {
            return completionPercent >= 100;
        }
    }
}
