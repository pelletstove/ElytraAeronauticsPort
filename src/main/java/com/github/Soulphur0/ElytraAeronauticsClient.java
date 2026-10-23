package com.github.Soulphur0;

import com.github.Soulphur0.config.FlightConfig;
import com.github.Soulphur0.net.FlightConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ElytraAeronauticsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		com.github.Soulphur0.client.CloudLayers.load();
		ClientPlayNetworking.registerGlobalReceiver(FlightConfigPayload.TYPE,
				(payload, context) -> context.client().execute(() -> FlightConfig.set(payload.config())));
	}
}
