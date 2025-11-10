package de.piggidragon.elementalrealms.guis.menus.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Provider for creating AffinityMenu with player's affinity data
 */
public class AffinityMenuProvider implements MenuProvider {

    private final List<AffinityMenu.AffinityData> affinities;

    /**
     * Creates a new menu provider with affinity data
     * @param affinities List of all affinities with completion percentages
     */
    public AffinityMenuProvider(List<AffinityMenu.AffinityData> affinities) {
        this.affinities = affinities;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.elementalrealms.affinity.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return null; // Not used when using FriendlyByteBuf
    }

    /**
     * Opens the affinity menu for the given player
     * @param player The player to open the menu for
     */
    public static void openForPlayer(ServerPlayer player) {
        // TODO: Get actual affinity data from player capability/attachment
        // Example data:
        List<AffinityMenu.AffinityData> affinities = List.of(
                new AffinityMenu.AffinityData(Affinity.FIRE, 100),
                new AffinityMenu.AffinityData(Affinity.WATER, 75),
                new AffinityMenu.AffinityData(Affinity.EARTH, 50),
                new AffinityMenu.AffinityData(Affinity.WIND, 25),
                new AffinityMenu.AffinityData(Affinity.LIGHTNING, 10)
        );

        AffinityMenuProvider provider = new AffinityMenuProvider(affinities);
        ElementalRealms.LOGGER.info("AffinityMenuProvider: " + provider);
        //player.openMenu(provider, buf -> writeAffinityData(buf, affinities));
        // Use SimpleMenuProvider - it handles createMenu() automatically
        player.openMenu(
                new SimpleMenuProvider(
                        // This lambda creates the server-side menu
                        (containerId, playerInventory, p) -> {
                            ElementalRealms.LOGGER.info("=== SERVER MENU CONSTRUCTOR CALLED ===");
                            // Create server menu with actual data
                            return new AffinityMenu(containerId, playerInventory, affinities);
                        },
                        // Menu title
                        Component.translatable("gui.elementalrealms.affinity.title")
                ),
                // This lambda writes data to the buffer for the client
                buf -> {
                    ElementalRealms.LOGGER.info("=== WRITING DATA TO BUFFER ===");
                    writeAffinityData(buf, affinities);
                    ElementalRealms.LOGGER.info("=== FINISHED WRITING DATA ===");
                }
        );
    }

    /**
     * Writes affinity data to the buffer for client synchronization
     * @param buf The buffer to write to
     * @param affinities The affinity data to write
     */
    private static void writeAffinityData(
            FriendlyByteBuf buf,
            List<AffinityMenu.AffinityData> affinities
    ) {
        // Write affinity count
        buf.writeInt(affinities.size());

        // Write each affinity
        for (AffinityMenu.AffinityData affinity : affinities) {
            buf.writeEnum(affinity.getAffinity());
            buf.writeInt(affinity.getCompletionPercent());
            ElementalRealms.LOGGER.info("Wrote affinity to buffer: " + affinity);
        }
    }
}
