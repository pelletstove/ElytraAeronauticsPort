package com.github.Soulphur0.mixin;

import com.github.Soulphur0.behaviour.EanFlightBehaviour;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	/** Vanilla's gliding velocity for this tick goes in, the Elytra Aeronautics one comes out. */
	@ModifyReturnValue(method = "updateFallFlyingMovement", at = @At("RETURN"))
	private Vec3 ean$flight(Vec3 vanilla) {
		return EanFlightBehaviour.flightBehaviour((LivingEntity) (Object) this, vanilla);
	}
}
