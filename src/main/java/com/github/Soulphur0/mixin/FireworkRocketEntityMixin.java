package com.github.Soulphur0.mixin;

import com.github.Soulphur0.behaviour.EanFlightBehaviour;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
	@Shadow
	private LivingEntity attachedToEntity;

	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", ordinal = 0))
	private Vec3 ean$rocketBoost(Vec3 vanilla) {
		return EanFlightBehaviour.rocketBoost(this.attachedToEntity, vanilla);
	}
}
