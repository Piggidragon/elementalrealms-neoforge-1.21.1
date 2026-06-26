package de.piggidragon.elementalrealms.registries.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.helper.AffinitiesRoll;
import de.piggidragon.elementalrealms.registries.configs.ConfigReloadListener;
import de.piggidragon.elementalrealms.registries.configs.Json5Reloadable;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.misc.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.util.entities.portal.PortalUtils;
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
 * Single registration point for all mod commands, dispatched under the
 * {@code /elementalrealms} root. Branching:
 * <pre>
 *   /elementalrealms reload
 *   /elementalrealms list
 *   /elementalrealms portal locate &lt;radius&gt;
 *   /elementalrealms portal spawn [random | &lt;dimension&gt;]
 *   /elementalrealms affinities list
 *   /elementalrealms affinities set &lt;affinity&gt;
 *   /elementalrealms affinities clear
 *   /elementalrealms affinities reroll
 * </pre>
 * Adding a new topic (Phase 1+: dragon, spell, boss) means adding a new
 * {@code .then(Commands.literal("topic")...)} block here and, if the branch
 * gets crowded, splitting it into a small {@code *Commands} helper class
 * in this same package.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ElementalRealmsCommand {

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

    // Distance the debug-spawn command puts a portal in front of the player.
    // Held here (not PortalConfig) because the value is purely a debug-spawn
    // UX choice — not the same semantic as PortalConfig.portalSpawnDistance,
    // which is the runtime teleport-target offset used by SchoolStaff.
    private static final double DEBUG_PORTAL_SPAWN_DISTANCE = 2.0;
    private static final double DEBUG_PORTAL_Y_OFFSET = 0.5;

    private ElementalRealmsCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("elementalrealms")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(ElementalRealmsCommand::reload))
                        .then(Commands.literal("list")
                                .executes(ElementalRealmsCommand::listConfigs))
                        .then(registerPortalBranch())
                        .then(registerAffinitiesBranch())
        );
    }

    // ---- JSON5 config subcommands ------------------------------------------------

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigReloadListener.reloadAllJson5();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reloaded " + Json5Reloadable.all().size() + " JSON5 config file(s)."
        ), true);
        return 1;
    }

    private static int listConfigs(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Loaded JSON5 config files:"), false);
        for (Json5Reloadable loader : Json5Reloadable.all()) {
            src.sendSuccess(() -> Component.literal(" - " + loader.configFileName()), false);
        }
        return 1;
    }

    // ---- /elementalrealms portal -------------------------------------------------

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> registerPortalBranch() {
        return Commands.literal("portal")
                .then(Commands.literal("locate")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 30000000))
                                .executes(ElementalRealmsCommand::portalLocate)))
                .then(Commands.literal("spawn")
                        .then(Commands.literal("random")
                                .executes(ElementalRealmsCommand::portalSpawnRandom))
                        .then(Commands.argument("dimension", StringArgumentType.greedyString())
                                .suggests(LEVEL_SUGGESTIONS)
                                .executes(ElementalRealmsCommand::portalSpawnDimension)));
    }

    private static int portalLocate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
    }

    private static int portalSpawnRandom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<Level> target = DynamicDimensionHandler.createDimensionForPortal(
                player.level().getServer(),
                null,
                ModLevel.getRandomLevel()
        );
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Could not create a new realm dimension"));
            return 0;
        }
        spawnDebugPortal(player, target);
        return 1;
    }

    private static int portalSpawnDimension(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(StringArgumentType.getString(ctx, "dimension"))
        );

        if (!ModLevel.getLevels().contains(dimensionKey)) {
            ctx.getSource().sendFailure(Component.literal("Invalid Dimension"));
            return 0;
        }

        ResourceKey<Level> portalTarget;
        if (ModLevel.getLevelsRandomSource().contains(dimensionKey)) {
            // RandomSource dimensions get a freshly-allocated realm_<n> generated for them.
            ResourceKey<Level> generated = DynamicDimensionHandler.createDimensionForPortal(
                    player.level().getServer(), null, dimensionKey);
            if (generated == null) {
                ctx.getSource().sendFailure(Component.literal("Could not create a new realm dimension"));
                return 0;
            }
            portalTarget = generated;
        } else {
            // Existing (test / test2 / school) dimensions route directly to themselves.
            portalTarget = dimensionKey;
        }
        spawnDebugPortal(player, portalTarget);
        return 1;
    }

    private static void spawnDebugPortal(ServerPlayer player, ResourceKey<Level> targetLevel) {
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
                player.getX() + look.x * DEBUG_PORTAL_SPAWN_DISTANCE,
                player.getY() + DEBUG_PORTAL_Y_OFFSET,
                player.getZ() + look.z * DEBUG_PORTAL_SPAWN_DISTANCE
        );
        portal.setYRot(player.getYRot());
        player.level().addFreshEntity(portal);
    }

    // ---- /elementalrealms affinities ---------------------------------------------

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> registerAffinitiesBranch() {
        return Commands.literal("affinities")
                .then(Commands.literal("list")
                        .executes(ElementalRealmsCommand::affinitiesList))
                .then(Commands.literal("set")
                        .then(Commands.argument("affinity", StringArgumentType.word())
                                .suggests(AFFINITY_SUGGESTIONS)
                                .executes(ElementalRealmsCommand::affinitiesSet)))
                .then(Commands.literal("clear")
                        .executes(ElementalRealmsCommand::affinitiesClear))
                .then(Commands.literal("reroll")
                        .executes(ElementalRealmsCommand::affinitiesReroll));
    }

    private static int affinitiesList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Your affinities: " + ModAffinities.getAffinities(player)), false);
        return 1;
    }

    private static int affinitiesSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
    }

    private static int affinitiesClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
    }

    private static int affinitiesReroll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        try {
            ModAffinities.clearAffinities(player);
        } catch (IllegalStateException ignored) {
            // No affinities yet - roll directly.
        }
        for (Affinity affinity : AffinitiesRoll.rollAffinities(player).keySet()) {
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
    }
}