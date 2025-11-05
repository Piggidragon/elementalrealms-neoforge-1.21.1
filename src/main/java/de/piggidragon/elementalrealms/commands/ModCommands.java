package de.piggidragon.elementalrealms.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.level.ModLevel;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers /affinities command for managing player affinities.
 * Subcommands: list, set, clear, reroll
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public class ModCommands {

    /**
     * Provides auto-completion suggestions for valid affinity names
     */
    public static final SuggestionProvider<CommandSourceStack> AFFINITY_SUGGESTIONS = (context, builder) -> {
        for (Affinity a : Affinity.values()) {
            builder.suggest(a.toString());
        }
        return builder.buildFuture();
    };
    public static final SuggestionProvider<CommandSourceStack> LEVEL_SUGGESTIONS = (context, builder) -> {
        for (ResourceKey<Level> level : ModLevel.LEVELS) {
            builder.suggest(level.location().toString());
        }
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("portal")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("locate")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 30000000)))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int radius = IntegerArgumentType.getInteger(ctx, "radius");

                            var portal = PortalUtils.findNearestPortal(player.level(), player.position(), radius);
                            if (portal == null) {
                                ctx.getSource().sendFailure(Component.literal("Could not find portal in search radius."));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal("Nearest Portal: " + portal.getPositionVec()), false);
                            return 1;
                        })
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String dimString = StringArgumentType.getString(ctx, "dimension");
                                    ResourceLocation location = ResourceLocation.parse(dimString);
                                    ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location);
                                    if (ModLevel.LEVELS.contains(dimensionKey)) {
                                        PortalEntity portal = new PortalEntity(
                                                ModEntities.PORTAL_ENTITY.get(),
                                                player.level(),
                                                true,
                                                -1,
                                                dimensionKey,
                                                player.getUUID()
                                        );
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
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("Invalid Dimension"));
                                        return 0;
                                    }
                                })
                        )
                )
        );

        dispatcher.register(Commands.literal("affinities")
                .requires(cs -> cs.hasPermission(2)) // Requires OP level 2

                // List current affinities
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var affinities = ModAffinities.getAffinities(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Your affinities: " + affinities), false);
                            return 1;
                        })
                )

                // Manually set a specific affinity
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

                // Clear all player affinities
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

                // Re-roll affinities randomly
                .then(Commands.literal("reroll")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            // Clear existing affinities
                            try {
                                ModAffinities.clearAffinities(player);
                            } catch (Exception ignored) {
                            }
                            // Roll and apply new random affinities
                            for (Affinity affinity : ModAffinitiesRoll.rollAffinities(player)) {
                                if (affinity != Affinity.VOID) {
                                    try {
                                        ModAffinities.addAffinity(player, affinity);
                                    } catch (Exception e) {
                                        // Should not occur since we're rolling unique affinities
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
