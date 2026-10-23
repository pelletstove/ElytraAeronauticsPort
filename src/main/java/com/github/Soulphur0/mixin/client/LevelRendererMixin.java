package com.github.Soulphur0.mixin.client;

import com.github.Soulphur0.client.CloudLayers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	/** The clouds pass: when layers are configured they are drawn instead of vanilla's single layer. */
	@WrapOperation(method = "lambda$addCloudsPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CloudRenderer;render(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V"))
	private void ean$layers(CloudRenderer vanilla, int color, CloudStatus status, float height, int distance, Vec3 camera, long time, float partial, Operation<Void> original) {
		if (CloudLayers.enabled()) {
			CloudLayers.render(vanilla, color, status, height, distance, camera, time, partial);
		} else {
			original.call(vanilla, color, status, height, distance, camera, time, partial);
		}
	}
}
