package com.github.Soulphur0;

import com.github.Soulphur0.config.EanCommands;
import com.github.Soulphur0.config.FlightConfig;
import com.github.Soulphur0.net.FlightConfigPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElytraAeronautics implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("ElytraAeronautics");

	@Override
	public void onInitialize() {
		FlightConfig.readFromDisk();

		PayloadTypeRegistry.clientboundPlay().register(FlightConfigPayload.TYPE, FlightConfigPayload.CODEC);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EanCommands.register(dispatcher));

		// a joining client has to fly by the server's rules; a single-player world re-reads the file first
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!server.isDedicatedServer()) {
				FlightConfig.readFromDisk();
			}
			syncTo(handler.player);
		});

		LOGGER.info("Elytra Aeronautics initialized! Have a good flight!");
	}

	public static void syncTo(ServerPlayer player) {
		ServerPlayNetworking.send(player, new FlightConfigPayload(FlightConfig.get().copy()));
	}

	public static void syncAll(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			syncTo(player);
		}
	}
}
