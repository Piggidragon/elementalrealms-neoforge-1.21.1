package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

import java.util.List;

/**
 * Spawns a permanent School-dimension portal at world spawn when the dragon is defeated.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class DragonDeathHandler {

    private static final ResourceLocation DRAGON_ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "root");

    private static volatile boolean advancementCompleted = false;

    private DragonDeathHandler() {
    }

    public static boolean isAdvancementCompleted() {
        return advancementCompleted;
    }

    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        AdvancementHolder advancement = event.getAdvancement();

        if (!advancement.id().equals(DRAGON_ADVANCEMENT_ID)) return;
        if (player.level().isClientSide()) return;

        advancementCompleted = true;
        ServerLevel level = (ServerLevel) player.level();
        spawnPortalOrigin(level.getServer());
    }

    public static void spawnPortalOrigin(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                overworld,
                ModLevel.SCHOOL_DIMENSION
        );

        BlockPos spawn = overworld.getSharedSpawnPos();
        BlockPos surface = overworld.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, spawn);
        portal.setPos(
                surface.getX(),
                surface.getY() + PortalEntity.PORTAL_HEIGHT_OFFSET,
                surface.getZ() + PortalEntity.PORTAL_Z_OFFSET
        );

        Component message = Component.literal("You can feel the dimension barrier cracking...");
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            player.displayClientMessage(message, true);
        }

        overworld.addFreshEntity(portal);
    }
}
