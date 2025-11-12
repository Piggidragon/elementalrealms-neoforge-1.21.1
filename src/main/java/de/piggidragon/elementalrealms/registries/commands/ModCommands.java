package de.piggidragon.elementalrealms.registries.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinitiesRoll;
import de.piggidragon.elementalrealms.util.PortalUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers custom commands for managing affinities and portals.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class ModCommands {

    /**
     * Provides auto-completion suggestions for valid affinity names.
     */
    public static final SuggestionProvider<CommandSourceStack> AFFINITY_SUGGESTIONS = (context, builder) -> {
        for (Affinity a : Affinity.values()) {
            builder.suggest(a.toString());
        }
        return builder.buildFuture();
    };

    /**
     * Provides auto-completion suggestions for valid dimension names.
     */
    public static final SuggestionProvider<CommandSourceStack> LEVEL_SUGGESTIONS = (context, builder) -> {
        for (ResourceKey<Level> level : ModLevel.getLevels()) {
            builder.suggest(level.location().toString());
        }
        return builder.buildFuture();
    };

    /**
     * Registers /portal and /affinities commands.
     */
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

                                    ServerLevel level = (ServerLevel) player.level();
                                    PortalEntity portal = PortalUtils.findNearestPortal(level, player.position(), radius);

                                    if (portal != null) {
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                "Found portal at: " + portal.position() +
                                                        " (Distance: " + String.format("%.2f", portal.position().distanceTo(player.position())) + " blocks)"
                                        ), false);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("No portal found within " + radius + " blocks"));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("spawn")
                        .then(Commands.literal("random")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                                    ResourceKey<Level> levelResourceKey = ModLevel.getRandomLevel();

                                    // Create portal entity
                                    PortalEntity portal = new PortalEntity(
                                            ModEntities.PORTAL_ENTITY.get(),
                                            player.level(),
                                            false,
                                            -1,
                                            null,
                                            player.getUUID()
                                    );

                                    portal.setTargetLevel(DynamicDimensionHandler.createDimensionForPortal(player.level().getServer(), portal, levelResourceKey));

                                    // Position portal 2 blocks in front of player
                                    Vec3 lookVec = player.getLookAngle();
                                    double distance = 2.0;
                                    Vec3 targetPos = new Vec3(
                                            player.getX() + lookVec.x * distance,
                                            player.getY() + 0.5,
                                            player.getZ() + lookVec.z * distance
                                    );

                                    portal.setPos(targetPos.x, targetPos.y, targetPos.z);
                                    portal.setYRot(player.getYRot());
                                    player.level().addFreshEntity(portal);
                                    return 1;
                                })
                        )
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String dimString = StringArgumentType.getString(ctx, "dimension");
                                    ResourceLocation location = ResourceLocation.parse(dimString);
                                    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location);

                                    if (!ModLevel.getLevels().contains(dimensionKey)) {
                                        ctx.getSource().sendFailure(Component.literal("Invalid Dimension"));
                                        return 0;
                                    }

                                    // Create portal entity
                                    PortalEntity portal = new PortalEntity(
                                            ModEntities.PORTAL_ENTITY.get(),
                                            player.level(),
                                            false,
                                            -1,
                                            null,
                                            player.getUUID()
                                    );

                                    if (ModLevel.getLevelsRandomSource().contains(dimensionKey)) {
                                        portal.setTargetLevel(DynamicDimensionHandler.createDimensionForPortal(player.level().getServer(), portal, dimensionKey));
                                    }

                                    // Position portal 2 blocks in front of player
                                    Vec3 lookVec = player.getLookAngle();
                                    double distance = 2.0;
                                    Vec3 targetPos = new Vec3(
                                            player.getX() + lookVec.x * distance,
                                            player.getY() + 0.5,
                                            player.getZ() + lookVec.z * distance
                                    );

                                    portal.setPos(targetPos.x, targetPos.y, targetPos.z);
                                    portal.setYRot(player.getYRot());
                                    player.level().addFreshEntity(portal);
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
                            var affinities = ModAffinities.getAffinities(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Your affinities: " + affinities), false);
                            return 1;
                        })
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("affinity", StringArgumentType.word())
                                .suggests(AFFINITY_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String affinityName = StringArgumentType.getString(ctx, "affinity");
                                    try {
                                        Affinity affinity = Affinity.valueOf(affinityName.toUpperCase());
                                        ModAffinities.addAffinity(player, affinity);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Set affinity: " + affinity), false);
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.literal("Invalid affinity: " + affinityName));
                                    } catch (Exception e) {
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
                            } catch (Exception e) {
                                ctx.getSource().sendFailure(Component.literal("No affinities to clear!"));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal("Cleared all affinities."), false);
                            return 1;
                        })
                )
                .then(Commands.literal("reroll")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            try {
                                ModAffinities.clearAffinities(player);
                            } catch (Exception ignored) {
                            }

                            // Roll new random affinities
                            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player).keySet()) {
                                if (affinity != Affinity.VOID) {
                                    try {
                                        ModAffinities.addAffinity(player, affinity);
                                    } catch (Exception e) {
                                        ElementalRealms.LOGGER.error("Error re-rolling affinities for player " + player.getName().getString() + ": " + e.getMessage());
                                    }
                                }
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal("Re-rolled affinities."), false);
                            return 1;
                        })
                )
        );

    }
}
