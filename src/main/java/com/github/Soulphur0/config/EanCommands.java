package com.github.Soulphur0.config;

import com.github.Soulphur0.ElytraAeronautics;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

/** /ean FlightConfig [option [value]]: shows or changes the flight settings. Operators only. */
public class EanCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ean")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.executes(context -> {
					context.getSource().sendSuccess(() -> Component.literal(
							"Elytra Aeronautics: /ean FlightConfig shows the settings, /ean FlightConfig <option> <value> changes one. Options: "
									+ String.join(", ", Arrays.stream(FlightConfig.Options.values()).map(Enum::name).toList())), false);
					return 1;
				})
				.then(Commands.literal("FlightConfig")
						.executes(context -> {
							FlightConfig config = FlightConfig.get();
							for (FlightConfig.Options option : FlightConfig.Options.values()) {
								context.getSource().sendSuccess(() -> Component.literal(option + " = " + config.getAsString(option)), false);
							}
							return 1;
						})
						.then(Commands.argument("option", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(
										Arrays.stream(FlightConfig.Options.values()).map(Enum::name), builder))
								.executes(context -> {
									FlightConfig.Options option = option(context.getSource(), StringArgumentType.getString(context, "option"));
									if (option == null) {
										return 0;
									}
									context.getSource().sendSuccess(() -> Component.literal(option + " = " + FlightConfig.get().getAsString(option)), false);
									return 1;
								})
								.then(Commands.argument("value", StringArgumentType.word())
										.executes(context -> {
											CommandSourceStack source = context.getSource();
											FlightConfig.Options option = option(source, StringArgumentType.getString(context, "option"));
											if (option == null) {
												return 0;
											}
											String value = StringArgumentType.getString(context, "value");
											FlightConfig changed = FlightConfig.get().copy();
											if (!changed.setFromString(option, value)) {
												source.sendFailure(Component.literal("'" + value + "' is not a valid value for " + option));
												return 0;
											}
											FlightConfig.set(changed);
											FlightConfig.writeToDisk();
											ElytraAeronautics.syncAll(source.getServer());
											source.sendSuccess(() -> Component.literal(option + " set to " + changed.getAsString(option)), true);
											return 1;
										})))));
	}

	private static FlightConfig.Options option(CommandSourceStack source, String name) {
		try {
			return FlightConfig.Options.valueOf(name);
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.literal("Unknown option '" + name + "'"));
			return null;
		}
	}
}
