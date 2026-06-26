package de.piggidragon.elementalrealms.registries.commands;

import com.mojang.brigadier.context.CommandContext;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.configs.ConfigReloadListener;
import de.piggidragon.elementalrealms.registries.configs.Json5Reloadable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers the {@code /elementalrealms} meta-command. Currently exposes:
 * <ul>
 *   <li>{@code /elementalrealms reload} — re-read all JSON5 config files from disk.</li>
 *   <li>{@code /elementalrealms list} — list loaded JSON5 config files + their schema version.</li>
 * </ul>
 * Subcommands for tuning / dev tools can be added behind the
 * {@link de.piggidragon.elementalrealms.registries.configs.CommonConfig#enableDevTools} gate.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class ElementalRealmsCommand {

    private ElementalRealmsCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("elementalrealms")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(ElementalRealmsCommand::reload))
                        .then(Commands.literal("list")
                                .executes(ElementalRealmsCommand::list))
        );
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigReloadListener.reloadAllJson5();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reloaded " + Json5Reloadable.all().size() + " JSON5 config file(s)."
        ), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("Loaded JSON5 config files:"), false);
        for (Json5Reloadable loader : Json5Reloadable.all()) {
            src.sendSuccess(() -> Component.literal(" - " + loader.configFileName()), false);
        }
        return 1;
    }
}