package com.github.Soulphur0.mixin.client;

import com.github.Soulphur0.client.CloudLayers;
import com.github.Soulphur0.client.EanCloudAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin implements EanCloudAccess {
	@Shadow
	private CloudRenderer.TextureData texture;

	@Override
	public void ean$copyTextureFrom(CloudRenderer other) {
		this.texture = ((CloudRendererMixin) (Object) other).texture;
	}

	/** Vanilla's renderer is the one the level renderer drives; the extra layers' renderers follow it. */
	@Inject(method = "endFrame", at = @At("TAIL"))
	private void ean$endFrame(CallbackInfo ci) {
		if ((Object) this == Minecraft.getInstance().levelRenderer.cloudRenderer()) {
			CloudLayers.endFrame();
		}
	}

	@Inject(method = "markForRebuild", at = @At("TAIL"))
	private void ean$rebuild(CallbackInfo ci) {
		if ((Object) this == Minecraft.getInstance().levelRenderer.cloudRenderer()) {
			CloudLayers.markForRebuild();
		}
	}
}
