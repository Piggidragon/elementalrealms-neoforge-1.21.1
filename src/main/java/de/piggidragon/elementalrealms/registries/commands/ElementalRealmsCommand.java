package de.piggidragon.elementalrealms.registries.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.AffinityType;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.magic.affinities.helper.AffinitiesRoll;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import de.piggidragon.elementalrealms.registries.configs.ConfigReloadListener;
import de.piggidragon.elementalrealms.registries.configs.Json5ConfigLoader;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Single registration point for all mod commands, dispatched under the
 * {@code /elementalrealms} root. Branching:
 * <pre>
 *   /elementalrealms reload
 *   /elementalrealms list
 *   /elementalrealms portal locate &lt;radius&gt;
 *   /elementalrealms portal spawn &lt;dimension&gt;
 *   /elementalrealms affinities list
 *   /elementalrealms affinities set &lt;affinity&gt;
 *   /elementalrealms affinities clear
 *   /elementalrealms affinities reroll
 *   /elementalrealms affinities roll show
 *   /elementalrealms affinities roll set &lt;field&gt; &lt;value&gt;
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

    /**
     * Suggestions for the {@code affinities set} command: excludes {@link Affinity#VOID}
     * because setting it isn't a real operation \u2014 use {@code affinities clear} instead.
     */
    private static final SuggestionProvider<CommandSourceStack> SETTABLE_AFFINITY_SUGGESTIONS = (context, builder) -> {
        for (Affinity a : Affinity.values()) {
            if (a == Affinity.VOID) continue;
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

    /**
     * Names of every editable field in the {@code roll} config section.
     * Tab-suggestion pool for {@code affinities roll set}. See
     * {@link #showRollConfig(CommandContext)} for the matching display order.
     */
    private static final String[] ROLL_FIELDS = {
            "deviantPartialChancePercent",
            "deviantMaxCompletionPercent",
            "partialDeviantWeightPercent",
            "elementalContinueChanceStartPercent",
            "elementalContinueChanceDecayPercent",
            "elementalMaxCompletionPercent",
            "elementalMaxIterations",
            "partialCompletionSlope",
    };

    private static final SuggestionProvider<CommandSourceStack> ROLL_FIELD_SUGGESTIONS = (context, builder) -> {
        for (String name : ROLL_FIELDS) {
            builder.suggest(name);
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

private static int portalSpawnDimension(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<Level> templateKey = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(StringArgumentType.getString(ctx, "dimension"))
        );

        if (!ModLevel.getLevels().contains(templateKey)) {
            ctx.getSource().sendFailure(Component.literal("Invalid Dimension"));
            return 0;
        }

        // SCHOOL_DIMENSION routes directly to itself (no template stem is registered for it;
        // the school portal is reached via the Dragon-spawn path / Dimension Staff, not this
        // debug command). For non-school templates, createDimensionForPortal() clones the
        // template's chunk generator into a fresh realm_<N> and returns that key. We pass
        // null as the portal argument because createDimensionForPortal only uses the portal
        // for its PORTAL_TARGET_LEVEL attachment, and we want the fresh realm key (not a
        // throwaway portal's null attachment) for the spawn.
        ResourceKey<Level> resolvedTarget;
        if (templateKey.equals(ModLevel.SCHOOL_DIMENSION)) {
            resolvedTarget = ModLevel.SCHOOL_DIMENSION;
        } else {
            ResourceKey<Level> created = DynamicDimensionHandler.createDimensionForPortal(
                    player.level().getServer(), null, templateKey);
            if (created == null) {
                ctx.getSource().sendFailure(Component.literal(
                        "Failed to create dimension for template " + templateKey.location()));
                return 0;
            }
            resolvedTarget = created;
        }
        spawnDebugPortal(player, resolvedTarget);
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
                                .suggests(SETTABLE_AFFINITY_SUGGESTIONS)
                                .executes(ElementalRealmsCommand::affinitiesSet))
                        .then(Commands.argument("affinity", StringArgumentType.word())
                                .suggests(SETTABLE_AFFINITY_SUGGESTIONS)
                                .then(Commands.argument("completion", IntegerArgumentType.integer(0, 100))
                                        .executes(ElementalRealmsCommand::affinitiesSet))))
                .then(Commands.literal("clear")
                        .executes(ElementalRealmsCommand::affinitiesClear))
                .then(Commands.literal("reroll")
                        .executes(ElementalRealmsCommand::affinitiesReroll))
                .then(Commands.literal("roll")
                        .then(Commands.literal("show")
                                .executes(ElementalRealmsCommand::showRollConfig))
                        .then(Commands.literal("set")
                                .then(Commands.argument("field", StringArgumentType.word())
                                        .suggests(ROLL_FIELD_SUGGESTIONS)
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(ElementalRealmsCommand::setRollConfig)))));
    }

    private static int affinitiesList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Map<Affinity, Integer> affinities = ModAffinities.getAffinities(player);

        if (affinities.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "You have no affinities. Use a shard, stone, or visit the School staff."
            ), false);
            return 1;
        }

        // Group by tier (ELEMENTAL → DEVIANT → ETERNAL → NONE). Within each tier, sort by
        // completion descending so the strongest affinity in the tier leads the list; ties
        // are broken alphabetically (so the order is stable across rerolls).
        Map<AffinityType, List<Map.Entry<Affinity, Integer>>> grouped = affinities.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getKey().getType(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator
                                                .comparing(Map.Entry<Affinity, Integer>::getValue).reversed()
                                                .thenComparing(e -> e.getKey().name()))
                                        .toList())
                ));
        // Tier display order: skip NONE if no Void row is present.
        List<AffinityType> tierOrder = new ArrayList<>();
        for (AffinityType t : List.of(AffinityType.ELEMENTAL, AffinityType.DEVIANT, AffinityType.ETERNAL)) {
            if (grouped.containsKey(t)) tierOrder.add(t);
        }
        if (grouped.containsKey(AffinityType.NONE)) tierOrder.add(AffinityType.NONE);

        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Your affinities:"), false);
        for (AffinityType tier : tierOrder) {
            src.sendSuccess(() -> Component.literal(" " + tierName(tier) + ":"), false);
            for (Map.Entry<Affinity, Integer> e : grouped.get(tier)) {
                Affinity a = e.getKey();
                int completion = e.getValue();
                String label = a == Affinity.VOID
                        ? "- Void (no affinity)"
                        : "- " + titleCase(a.name()) + " (" + completion + "%)";
                src.sendSuccess(() -> Component.literal("    " + label), false);
            }
        }
        return 1;
    }

    private static String tierName(AffinityType tier) {
        return switch (tier) {
            case ELEMENTAL -> "Elemental";
            case DEVIANT -> "Deviant";
            case ETERNAL -> "Eternal";
            case NONE -> "Other";
        };
    }

    private static String titleCase(String name) {
        if (name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }

    private static int affinitiesSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "affinity");
        // Optional completion arg: if present, set to that %; if absent, default to 100%.
        int completion;
        try {
            completion = IntegerArgumentType.getInteger(ctx, "completion");
        } catch (IllegalArgumentException e) {
            completion = 100;
        }
        final int finalCompletion = completion;

        try {
            Affinity affinity = Affinity.valueOf(name.toUpperCase());
            ModAffinities.setAffinity(player, affinity, finalCompletion);
            final Affinity finalAffinity = affinity;
            ctx.getSource().sendSuccess(
                    () -> Component.literal(
                            "Set " + titleCase(finalAffinity.name()) + " to " + finalCompletion + "%."
                    ), false);
        } catch (IllegalArgumentException e) {
            // Could be Affinity.valueOf (bad name) or setAffinity (VOID / completion range).
            // setAffinity's messages start with "Cannot set VOID" or "Completion must be".
            String msg = e.getMessage();
            if (msg != null && (msg.startsWith("Cannot set VOID") || msg.startsWith("Completion must be"))) {
                ctx.getSource().sendFailure(Component.literal(msg));
            } else {
                ctx.getSource().sendFailure(Component.literal("Invalid affinity: " + name));
            }
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
        for (Map.Entry<Affinity, Integer> entry : AffinitiesRoll.rollAffinities(player).entrySet()) {
            Affinity affinity = entry.getKey();
            if (affinity == Affinity.VOID) continue;
            int completion = entry.getValue();
            try {
                if (completion >= AffinityConfig.maxCompletionPercent()) {
                    ModAffinities.addAffinity(player, affinity);
                } else {
                    ModAffinities.addIncrementAffinity(player, affinity, completion);
                }
            } catch (IllegalStateException e) {
                ElementalRealms.LOGGER.error(
                        "Error rerolling affinity {} for {}: {}",
                        affinity, player.getName().getString(), e.getMessage());
            }
        }
        ctx.getSource().sendSuccess(
                () -> Component.literal("Re-rolled affinities."), false);
        return 1;
    }

    // ---- /elementalrealms affinities roll ---------------------------------------

    /**
     * Prints every field in the {@code roll} section with its current effective value.
     * Pulls from the live {@link AffinityConfig} getters, so it reflects the last reload,
     * not necessarily what's on disk if the player hand-edited the file without reloading.
     */
    private static int showRollConfig(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Affinities roll config (current effective values):"), false);
        for (String field : ROLL_FIELDS) {
            String value = formatRollFieldValue(field);
            src.sendSuccess(() -> Component.literal(" " + field + " = " + value), false);
        }
        return 1;
    }

    private static String formatRollFieldValue(String field) {
        return switch (field) {
            case "deviantPartialChancePercent" -> Integer.toString(AffinityConfig.deviantPartialChancePercent());
            case "deviantMaxCompletionPercent" -> Integer.toString(AffinityConfig.deviantMaxCompletionPercent());
            case "partialDeviantWeightPercent" -> Integer.toString(AffinityConfig.partialDeviantWeightPercent());
            case "elementalContinueChanceStartPercent" -> Integer.toString(AffinityConfig.elementalContinueChanceStartPercent());
            case "elementalContinueChanceDecayPercent" -> Integer.toString(AffinityConfig.elementalContinueChanceDecayPercent());
            case "elementalMaxCompletionPercent" -> Integer.toString(AffinityConfig.elementalMaxCompletionPercent());
            case "elementalMaxIterations" -> Integer.toString(AffinityConfig.elementalMaxIterations());
            case "partialCompletionSlope" -> Double.toString(AffinityConfig.partialCompletionSlope());
            default -> "?";
        };
    }

    /**
     * Mutates a single {@code roll} field on disk, then reloads the in-memory config.
     * <ul>
     *   <li>Int fields must parse cleanly as 0..100 (existing convention for percentages
     *       and counts).</li>
     *   <li>Double {@code partialCompletionSlope} must parse as a double >= 1.0
     *       (slope=1 is uniform, slope>1 is left-skewed).</li>
     * </ul>
     */
    private static int setRollConfig(CommandContext<CommandSourceStack> ctx) {
        String field = StringArgumentType.getString(ctx, "field");
        String rawValue = StringArgumentType.getString(ctx, "value").trim();

        boolean isDouble = field.equals("partialCompletionSlope");
        boolean isInt = !isDouble;

        Object parsed;
        try {
            if (isDouble) {
                double v = Double.parseDouble(rawValue);
                if (v < 1.0) {
                    ctx.getSource().sendFailure(Component.literal(
                            field + " must be >= 1.0 (slope=1 is uniform), got " + v));
                    return 0;
                }
                parsed = v;
            } else {
                int v = Integer.parseInt(rawValue);
                if (v < 0 || v > 100) {
                    ctx.getSource().sendFailure(Component.literal(
                            field + " must be 0..100, got " + v));
                    return 0;
                }
                parsed = v;
            }
        } catch (NumberFormatException e) {
            ctx.getSource().sendFailure(Component.literal(
                    "Bad value for " + field + ": '" + rawValue + "' ("
                            + (isDouble ? "expected double >= 1.0" : "expected integer 0..100") + ")"));
            return 0;
        }

        Path file = Json5ConfigLoader.resolve(AffinityConfig.INSTANCE.configFileName());
        JsonElement root = Json5ConfigLoader.load(file);
        if (root == null || !root.isJsonObject()) {
            ctx.getSource().sendFailure(Component.literal(
                    "Could not load " + file + " \u2014 file missing or unreadable."));
            return 0;
        }
        JsonObject obj = root.getAsJsonObject();
        JsonObject roll;
        if (obj.has("roll") && obj.get("roll").isJsonObject()) {
            roll = obj.getAsJsonObject("roll");
        } else {
            roll = new JsonObject();
            obj.add("roll", roll);
        }

        if (parsed instanceof Double d) {
            roll.addProperty(field, d);
        } else {
            roll.addProperty(field, (Integer) parsed);
        }
        Json5ConfigLoader.save(file, obj);
        AffinityConfig.INSTANCE.reload();

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Set " + field + " = " + formatRollFieldValue(field) + " (saved to " + file + ", reloaded)"), true);
        return 1;
    }
}