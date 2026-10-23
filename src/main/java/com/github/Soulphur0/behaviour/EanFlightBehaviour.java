package com.github.Soulphur0.behaviour;

import com.github.Soulphur0.config.FlightConfig;
import com.github.Soulphur0.utility.EanMath;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class EanFlightBehaviour {
	/** The speed (blocks per second, roughly) the config asks for at this altitude. */
	public static double speedAtAltitude(FlightConfig config, double altitude) {
		double minSpeed = config.minSpeed;
		double maxSpeed = config.maxSpeed;
		if (!config.altitudeDeterminesSpeed) {
			return minSpeed;
		}
		return Mth.clamp(EanMath.getLinealValue(config.minHeight, minSpeed, config.maxHeight, maxSpeed, altitude), minSpeed, maxSpeed);
	}

	/**
	 * Called with the velocity vanilla worked out for this tick of gliding. Returns the replacement velocity, or the
	 * vanilla one when altitude-based speed is off. Also eases the pitch back while sneaking, if configured.
	 */
	public static Vec3 flightBehaviour(LivingEntity player, Vec3 vanilla) {
		FlightConfig config = FlightConfig.get();

		if (config.sneakingRealignsPitch && player.isShiftKeyDown()) {
			float pitch = player.getXRot();
			float alignmentAngle = config.realignAngle;
			float alignmentRate = config.realignRate;
			if (Math.abs(pitch) <= alignmentRate * 2) {
				player.setXRot(alignmentAngle);
			} else if (pitch > alignmentAngle) {
				player.setXRot(pitch - alignmentRate);
			} else {
				player.setXRot(pitch + alignmentRate);
			}
		}

		if (!config.altitudeDeterminesSpeed) {
			return vanilla;
		}

		double altitudeSpeed = speedAtAltitude(config, player.position().y);
		Vec3 movement = calcFlightMovementVector(player, altitudeSpeed);
		return movement.multiply(0.99F, 0.98F, 0.99F);
	}

	private static Vec3 calcFlightMovementVector(LivingEntity player, double altitudeCalculatedSpeed) {
		double fallSpeedConstant = 0.08;

		Vec3 movementVector = player.getDeltaMovement();
		if (movementVector.y > -0.5) {
			player.fallDistance = 1.0F;
		}

		Vec3 rotationVector = player.getLookAngle();
		float pitchInRadians = player.getXRot() * ((float) Math.PI / 180);
		double angleToTheGround = Math.sqrt(rotationVector.x * rotationVector.x + rotationVector.z * rotationVector.z);
		double speed = movementVector.horizontalDistance();
		double rotationVectorLength = rotationVector.length();

		// the steeper the dive, the faster the fall
		float fallSpeedMultiplier = Mth.cos(pitchInRadians);
		fallSpeedMultiplier = (float) ((double) fallSpeedMultiplier * ((double) fallSpeedMultiplier * Math.min(1.0, rotationVectorLength / 0.4)));

		// the player always loses some altitude
		movementVector = player.getDeltaMovement().add(0.0, fallSpeedConstant * (-1.0 + (double) fallSpeedMultiplier * 0.75), 0.0);

		// looking under the horizon: dive speed plus the configured speed (minus vanilla's)
		if (movementVector.y < 0.0 && angleToTheGround > 0.0) {
			double verticalSpeedValue = movementVector.y * -0.1 * (double) fallSpeedMultiplier;
			double totalSpeed = altitudeCalculatedSpeed - 30.1298D;
			double horizontalSpeedValue = verticalSpeedValue + totalSpeed * 0.0005584565076792029D;
			movementVector = movementVector.add(rotationVector.x * horizontalSpeedValue / angleToTheGround, verticalSpeedValue, rotationVector.z * horizontalSpeedValue / angleToTheGround);
		}

		// looking over the horizon: trade speed for height
		if (pitchInRadians < 0.0F && angleToTheGround > 0.0) {
			double verticalSpeedValue = speed * (double) (-Mth.sin(pitchInRadians)) * 0.04;
			movementVector = movementVector.add(-rotationVector.x * verticalSpeedValue / angleToTheGround, Math.min(verticalSpeedValue * 3.2, 0.1D), -rotationVector.z * verticalSpeedValue / angleToTheGround);
		}

		if (angleToTheGround > 0.0) {
			movementVector = movementVector.add((rotationVector.x / angleToTheGround * speed - movementVector.x) * 0.1, 0.0, (rotationVector.z / angleToTheGround * speed - movementVector.z) * 0.1);
		}

		return movementVector;
	}

	/** The velocity a firework gives its glider; the vanilla one is passed through when altitude-based speed is off. */
	public static Vec3 rocketBoost(LivingEntity shooter, Vec3 vanilla) {
		FlightConfig config = FlightConfig.get();
		if (!config.altitudeDeterminesSpeed || shooter == null) {
			return vanilla;
		}
		Vec3 rotation = shooter.getLookAngle();
		Vec3 velocity = shooter.getDeltaMovement();
		double altitudeSpeed = speedAtAltitude(config, shooter.position().y);

		// fitted curve: which multiplier gives roughly this speed
		double speedMultiplier = 0.0000006453840919839 * Math.pow(altitudeSpeed, 2) + 0.0508467 * altitudeSpeed - 0.202377;

		return velocity.add(
				rotation.x * 0.1 + (rotation.x * speedMultiplier - velocity.x) * 0.5,
				rotation.y * 0.1 + (rotation.y * speedMultiplier - velocity.y) * 0.5,
				rotation.z * 0.1 + (rotation.z * speedMultiplier - velocity.z) * 0.5);
	}
}
