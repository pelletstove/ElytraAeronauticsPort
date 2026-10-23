package com.github.Soulphur0.config;

import com.github.Soulphur0.ElytraAeronautics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Flight settings, stored in config/ElytraAeronautics/elytra_flight_settings.json (same file and keys as the 1.20 mod).
 * On a multiplayer client the instance is replaced by the server's settings, see FlightConfigPayload.
 */
public class FlightConfig {
	public boolean altitudeDeterminesSpeed = true;
	public double minSpeed = 30.35;
	public double maxSpeed = 257.22;
	public double minHeight = 250.0;
	public double maxHeight = 1000.0;
	public boolean sneakingRealignsPitch = true;
	public float realignAngle = 0.0F;
	public float realignRate = 0.1F;

	/** The option names accepted by /ean FlightConfig. */
	public enum Options {
		altitudeDeterminesSpeed, minSpeed, maxSpeed, minHeight, maxHeight, sneakingRealignsPitch, realignAngle, realignRate
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static FlightConfig instance = new FlightConfig();


	public static FlightConfig get() {
		return instance;
	}

	public static void set(FlightConfig config) {
		instance = config;
	}

	public FlightConfig copy() {
		return GSON.fromJson(GSON.toJson(this), FlightConfig.class);
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("ElytraAeronautics").resolve("elytra_flight_settings.json");
	}

	public static void readFromDisk() {
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			if (!Files.exists(file)) {
				instance = new FlightConfig();
				writeToDisk();
				return;
			}
			try (Reader reader = Files.newBufferedReader(file)) {
				FlightConfig read = GSON.fromJson(reader, FlightConfig.class);
				instance = read == null ? new FlightConfig() : read;
			}
		} catch (Exception e) {
			ElytraAeronautics.LOGGER.warn("Could not read {}, using defaults", file, e);
			instance = new FlightConfig();
		}
	}

	public static void writeToDisk() {
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(instance));
		} catch (Exception e) {
			ElytraAeronautics.LOGGER.warn("Could not write {}", file, e);
		}
	}

	/** Sets one option from its command-line text; returns false if the value does not parse. */
	public boolean setFromString(Options option, String value) {
		try {
			switch (option) {
				case altitudeDeterminesSpeed -> altitudeDeterminesSpeed = parseBoolean(value);
				case minSpeed -> minSpeed = Double.parseDouble(value);
				case maxSpeed -> maxSpeed = Double.parseDouble(value);
				case minHeight -> minHeight = Double.parseDouble(value);
				case maxHeight -> maxHeight = Double.parseDouble(value);
				case sneakingRealignsPitch -> sneakingRealignsPitch = parseBoolean(value);
				case realignAngle -> realignAngle = Float.parseFloat(value);
				case realignRate -> realignRate = Float.parseFloat(value);
			}
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public String getAsString(Options option) {
		return switch (option) {
			case altitudeDeterminesSpeed -> String.valueOf(altitudeDeterminesSpeed);
			case minSpeed -> String.valueOf(minSpeed);
			case maxSpeed -> String.valueOf(maxSpeed);
			case minHeight -> String.valueOf(minHeight);
			case maxHeight -> String.valueOf(maxHeight);
			case sneakingRealignsPitch -> String.valueOf(sneakingRealignsPitch);
			case realignAngle -> String.valueOf(realignAngle);
			case realignRate -> String.valueOf(realignRate);
		};
	}

	private static boolean parseBoolean(String value) {
		if (value.equalsIgnoreCase("true")) {
			return true;
		}
		if (value.equalsIgnoreCase("false")) {
			return false;
		}
		throw new IllegalArgumentException(value);
	}
}
