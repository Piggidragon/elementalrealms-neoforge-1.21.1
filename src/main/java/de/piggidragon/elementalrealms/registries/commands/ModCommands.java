package de.piggidragon.elementalrealms.registries.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinitiesRoll;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.util.PortalUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers the {@code /portal} and {@code /affinities} commands.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> AFFINITY_SUGGESTIONS = (context, builder) -> {
        for (Affinity a : Affinity.values()) {
            builder.suggest(a.toString());
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> LEVEL_SUGGESTIONS = (context, builder) -> {
        for (ResourceKey<Level> level : ModLevel.getLevels()) {
            builder.suggest(level.location().toString());
        }
        return builder.buildFuture();
    };
    private static final double PORTAL_SPAWN_DISTANCE = 2.0;
    private static final double PORTAL_HEIGHT = 0.5;
    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("portal")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("locate")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 30000000))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int radius = IntegerArgumentType.getInteger(ctx, "radius");
                                    PortalEntity portal = PortalUtils.findNearestPortal(
                                            player.serverLevel(), player.position(), radius);

                                    if (portal != null) {
                                        double distance = portal.position().distanceTo(player.position());
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                "Found portal at: " + portal.position()
                                                        + " (Distance: " + String.format("%.2f", distance) + " blocks)"
                                        ), false);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal(
                                                "No portal found within " + radius + " blocks"));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("spawn")
                        .then(Commands.literal("random")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    spawnPortalInFront(player, DynamicDimensionHandler.createDimensionForPortal(
                                            player.level().getServer(),
                                            new PortalEntity(ModEntities.PORTAL_ENTITY.get(), player.level(), false, -1, null, player.getUUID()),
                                            ModLevel.getRandomLevel()
                                    ));
                                    return 1;
                                })
                        )
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ResourceKey<Level> dimensionKey = ResourceKey.create(
                                            Registries.DIMENSION,
                                            ResourceLocation.parse(StringArgumentType.getString(ctx, "dimension"))
                                    );

                                    if (!ModLevel.getLevels().contains(dimensionKey)) {
                                        ctx.getSource().sendFailure(Component.literal("Invalid Dimension"));
                                        return 0;
                                    }

                                    PortalEntity portal = new PortalEntity(
                                            ModEntities.PORTAL_ENTITY.get(),
                                            player.level(),
                                            false,
                                            -1,
                                            null,
                                            player.getUUID()
                                    );
                                    if (ModLevel.getLevelsRandomSource().contains(dimensionKey)) {
                                        portal.setTargetLevel(DynamicDimensionHandler.createDimensionForPortal(
                                                player.level().getServer(), portal, dimensionKey));
                                    }
                                    spawnPortalInFront(player, null);
                                    return 1;
                                })
                        )
                )
        );

        dispatcher.register(Commands.literal("affinities")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Your affinities: " + ModAffinities.getAffinities(player)), false);
                            return 1;
                        })
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("affinity", StringArgumentType.word())
                                .suggests(AFFINITY_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "affinity");
                                    try {
                                        Affinity affinity = Affinity.valueOf(name.toUpperCase());
                                        ModAffinities.addAffinity(player, affinity);
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("Set affinity: " + affinity), false);
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.literal("Invalid affinity: " + name));
                                    } catch (IllegalStateException e) {
                                        ctx.getSource().sendFailure(Component.literal(e.getMessage()));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            try {
                                ModAffinities.clearAffinities(player);
                            } catch (IllegalStateException e) {
                                ctx.getSource().sendFailure(Component.literal("No affinities to clear!"));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Cleared all affinities."), false);
                            return 1;
                        })
                )
                .then(Commands.literal("reroll")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            try {
                                ModAffinities.clearAffinities(player);
                            } catch (IllegalStateException ignored) {
                                // No affinities yet - roll directly.
                            }
                            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player).keySet()) {
                                if (affinity == Affinity.VOID) continue;
                                try {
                                    ModAffinities.addAffinity(player, affinity);
                                } catch (IllegalStateException e) {
                                    ElementalRealms.LOGGER.error(
                                            "Error rerolling affinities for {}: {}",
                                            player.getName().getString(), e.getMessage());
                                }
                            }
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Re-rolled affinities."), false);
                            return 1;
                        })
                )
        );
    }

    private static void spawnPortalInFront(ServerPlayer player, ResourceKey<Level> targetLevel) {
        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                player.level(),
                false,
                -1,
                targetLevel,
                player.getUUID()
        );
        if (targetLevel != null) {
            portal.setTargetLevel(targetLevel);
        }
        Vec3 look = player.getLookAngle();
        portal.setPos(
                player.getX() + look.x * PORTAL_SPAWN_DISTANCE,
                player.getY() + PORTAL_HEIGHT,
                player.getZ() + look.z * PORTAL_SPAWN_DISTANCE
        );
        portal.setYRot(player.getYRot());
        player.level().addFreshEntity(portal);
    }
}
