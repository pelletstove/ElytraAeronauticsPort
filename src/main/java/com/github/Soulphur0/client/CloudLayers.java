package com.github.Soulphur0.client;

import com.github.Soulphur0.ElytraAeronautics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extra cloud layers, read from the same files as the 1.20 mod (cloud_settings.json, cloud_layers.json). Each layer gets
 * its own vanilla CloudRenderer so it keeps its own mesh, and shares vanilla's cloud texture.
 *
 * Used: altitude, cloudType (FANCY = 3D clouds, anything else = flat), horizontalRenderDistance, cloudSpeed, cloudColor,
 * cloudOpacity. Not used (vanilla fixes them): cloudThickness, shading, skyEffects, lodRenderDistance, verticalRenderDistance.
 */
public class CloudLayers {
	public static class Layer {
		public double altitude = 192.0;
		public String cloudType = "FANCY";
		public double verticalRenderDistance = 1000.0;
		public int horizontalRenderDistance = 15;
		public double lodRenderDistance = 150.0;
		public double cloudSpeed = 1.0;
		public double cloudThickness = 4.0;
		public int cloudColor = 0xFFFFFF;
		public double cloudOpacity = 0.8;
		public boolean shading = true;
		public boolean skyEffects = true;

		transient CloudRenderer renderer;
	}

	private static class Settings {
		int numberOfLayers = 3;
		boolean useEanClouds = true;
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean enabled = false;
	private static List<Layer> layers = List.of();

	public static boolean enabled() {
		return enabled && !layers.isEmpty();
	}

	public static List<Layer> layers() {
		return layers;
	}

	public static void load() {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve("ElytraAeronautics");
		Path settingsFile = dir.resolve("cloud_settings.json");
		Path layersFile = dir.resolve("cloud_layers.json");
		// Missing files mean vanilla clouds: nothing is written for people who never had the old cloud files.
		if (!Files.exists(settingsFile) || !Files.exists(layersFile)) {
			enabled = false;
			return;
		}
		try (Reader a = Files.newBufferedReader(settingsFile); Reader b = Files.newBufferedReader(layersFile)) {
			Settings settings = GSON.fromJson(a, Settings.class);
			List<Layer> read = GSON.fromJson(b, new TypeToken<List<Layer>>() {}.getType());
			if (settings == null || read == null) {
				enabled = false;
				return;
			}
			int count = Math.max(0, Math.min(settings.numberOfLayers, read.size()));
			layers = new ArrayList<>(read.subList(0, count));
			enabled = settings.useEanClouds;
		} catch (Exception e) {
			ElytraAeronautics.LOGGER.warn("Could not read the cloud layer files, using vanilla clouds: {}", e.toString());
			enabled = false;
		}
	}

	/** Draws every configured layer instead of vanilla's single one. */
	public static void render(CloudRenderer vanilla, int color, CloudStatus status, float vanillaHeight, int distance, Vec3 camera, long gameTime, float partialTicks) {
		for (Layer layer : layers) {
			if (layer.renderer == null) {
				layer.renderer = new CloudRenderer();
			}
			((EanCloudAccess) layer.renderer).ean$copyTextureFrom(vanilla);
			boolean fancy = "FANCY".equalsIgnoreCase(layer.cloudType);
			CloudStatus layerStatus = status == CloudStatus.OFF ? status : fancy ? CloudStatus.FANCY : CloudStatus.FAST;
			int layerDistance = layer.horizontalRenderDistance > 0 ? layer.horizontalRenderDistance : distance;
			layer.renderer.render(tint(color, layer), layerStatus, (float) layer.altitude, layerDistance, camera, (long) (gameTime * layer.cloudSpeed), partialTicks);
		}
	}

	/** Vanilla's colour (it follows the time of day) times the layer's colour, with the layer's opacity on its alpha. */
	static int tint(int vanillaColor, Layer layer) {
		int r = ARGB.red(vanillaColor) * ARGB.red(layer.cloudColor | 0xFF000000) / 255;
		int g = ARGB.green(vanillaColor) * ARGB.green(layer.cloudColor | 0xFF000000) / 255;
		int b = ARGB.blue(vanillaColor) * ARGB.blue(layer.cloudColor | 0xFF000000) / 255;
		int a = (int) Math.round(ARGB.alpha(vanillaColor) * Math.max(0.0, Math.min(1.0, layer.cloudOpacity)));
		return ARGB.color(a, r, g, b);
	}

	public static void markForRebuild() {
		for (Layer l : layers) {
			if (l.renderer != null) l.renderer.markForRebuild();
		}
	}

	public static void endFrame() {
		for (Layer l : layers) {
			if (l.renderer != null) l.renderer.endFrame();
		}
	}
}
