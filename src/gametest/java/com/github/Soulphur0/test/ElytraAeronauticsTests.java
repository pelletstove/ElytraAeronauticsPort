package com.github.Soulphur0.test;

import com.github.Soulphur0.behaviour.EanFlightBehaviour;
import com.github.Soulphur0.config.FlightConfig;
import com.github.Soulphur0.net.FlightConfigPayload;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraAeronauticsTests {
	/** Runs the body with the given flight settings in place, then puts the old ones back (tests share the static config). */
	private static void with(FlightConfig config, Runnable body) {
		FlightConfig old = FlightConfig.get();
		FlightConfig.set(config);
		try {
			body.run();
		} finally {
			FlightConfig.set(old);
		}
	}

	private static FlightConfig defaults() {
		return new FlightConfig();
	}

	private static LivingEntity glider(GameTestHelper helper, double y, float pitch, double speed) {
		LivingEntity entity = EntityTypes.HUSK.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		entity.setPos(0, y, 0);
		entity.setYRot(0);
		entity.setXRot(pitch);
		// flying along +z (yaw 0 looks along +z)
		entity.setDeltaMovement(new Vec3(0, 0, speed));
		return entity;
	}

	private static void check(GameTestHelper helper, boolean ok, String message) {
		if (!ok) {
			helper.fail(message);
		}
	}

	@GameTest
	public void speedFollowsAltitudeLinearlyAndIsClamped(GameTestHelper helper) {
		FlightConfig c = defaults();
		check(helper, EanFlightBehaviour.speedAtAltitude(c, 0) == c.minSpeed, "below the curve should give minSpeed");
		check(helper, EanFlightBehaviour.speedAtAltitude(c, c.minHeight) == c.minSpeed, "at minHeight should give minSpeed");
		check(helper, Math.abs(EanFlightBehaviour.speedAtAltitude(c, c.maxHeight) - c.maxSpeed) < 1e-9, "at maxHeight should give maxSpeed");
		check(helper, EanFlightBehaviour.speedAtAltitude(c, 5000) == c.maxSpeed, "above the curve should give maxSpeed");
		double mid = EanFlightBehaviour.speedAtAltitude(c, (c.minHeight + c.maxHeight) / 2);
		check(helper, Math.abs(mid - (c.minSpeed + c.maxSpeed) / 2) < 1e-6, "halfway up should be halfway in speed, got " + mid);
		helper.succeed();
	}

	@GameTest
	public void divingIsFasterHighUpThanLow(GameTestHelper helper) {
		with(defaults(), () -> {
			LivingEntity low = glider(helper, 100, 30, 1.0);
			LivingEntity high = glider(helper, 1200, 30, 1.0);
			Vec3 lowVelocity = EanFlightBehaviour.flightBehaviour(low, low.getDeltaMovement());
			Vec3 highVelocity = EanFlightBehaviour.flightBehaviour(high, high.getDeltaMovement());
			check(helper, highVelocity.horizontalDistance() > lowVelocity.horizontalDistance() + 0.01,
					"expected more horizontal speed at 1200 than at 100, got " + highVelocity.horizontalDistance() + " vs " + lowVelocity.horizontalDistance());
		});
		helper.succeed();
	}

	@GameTest
	public void vanillaVelocityPassesThroughWhenSpeedByAltitudeIsOff(GameTestHelper helper) {
		FlightConfig c = defaults();
		c.altitudeDeterminesSpeed = false;
		c.sneakingRealignsPitch = false;
		with(c, () -> {
			LivingEntity e = glider(helper, 1200, 30, 1.0);
			Vec3 vanilla = new Vec3(1, 2, 3);
			check(helper, EanFlightBehaviour.flightBehaviour(e, vanilla) == vanilla, "flightBehaviour should return the vanilla vector");
			check(helper, EanFlightBehaviour.rocketBoost(e, vanilla) == vanilla, "rocketBoost should return the vanilla vector");
		});
		helper.succeed();
	}

	@GameTest
	public void rocketBoostIsStrongerHighUp(GameTestHelper helper) {
		with(defaults(), () -> {
			LivingEntity low = glider(helper, 100, 0, 0.5);
			LivingEntity high = glider(helper, 1200, 0, 0.5);
			Vec3 v = new Vec3(0, 0, 0);
			double lowBoost = EanFlightBehaviour.rocketBoost(low, v).horizontalDistance();
			double highBoost = EanFlightBehaviour.rocketBoost(high, v).horizontalDistance();
			check(helper, highBoost > lowBoost * 2, "expected a much stronger boost at 1200 than at 100: " + highBoost + " vs " + lowBoost);
		});
		helper.succeed();
	}

	@GameTest
	public void sneakingEasesThePitchTowardsTheAlignAngle(GameTestHelper helper) {
		FlightConfig c = defaults();
		c.altitudeDeterminesSpeed = false;
		c.realignAngle = 0.0F;
		c.realignRate = 1.0F;
		with(c, () -> {
			LivingEntity e = glider(helper, 200, 40, 1.0);
			e.setShiftKeyDown(true);
			EanFlightBehaviour.flightBehaviour(e, e.getDeltaMovement());
			check(helper, Math.abs(e.getXRot() - 39.0F) < 1e-4, "pitch should have moved one step from 40 to 39, is " + e.getXRot());
			e.setShiftKeyDown(false);
			EanFlightBehaviour.flightBehaviour(e, e.getDeltaMovement());
			check(helper, Math.abs(e.getXRot() - 39.0F) < 1e-4, "pitch must not change when not sneaking, is " + e.getXRot());
		});
		helper.succeed();
	}

	@GameTest
	public void commandStyleEditsParseAndReject(GameTestHelper helper) {
		FlightConfig c = defaults();
		check(helper, c.setFromString(FlightConfig.Options.maxSpeed, "300.5") && c.maxSpeed == 300.5, "maxSpeed edit failed");
		check(helper, c.setFromString(FlightConfig.Options.sneakingRealignsPitch, "false") && !c.sneakingRealignsPitch, "boolean edit failed");
		check(helper, !c.setFromString(FlightConfig.Options.minHeight, "high") && c.minHeight == 250.0, "a bad number must be rejected and leave the value alone");
		check(helper, !c.setFromString(FlightConfig.Options.altitudeDeterminesSpeed, "yes"), "'yes' is not a boolean");
		FlightConfig copy = c.copy();
		check(helper, copy != c && copy.maxSpeed == 300.5 && !copy.sneakingRealignsPitch, "copy should be an equal, separate object");
		helper.succeed();
	}

	@GameTest
	public void syncPayloadRoundTrips(GameTestHelper helper) {
		FlightConfig c = defaults();
		c.minSpeed = 12.5;
		c.maxHeight = 777;
		c.realignRate = 0.25F;
		c.sneakingRealignsPitch = false;
		io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
		FlightConfigPayload.CODEC.encode(buf, new FlightConfigPayload(c));
		FlightConfig back = FlightConfigPayload.CODEC.decode(buf).config();
		check(helper, back.minSpeed == 12.5 && back.maxHeight == 777 && back.realignRate == 0.25F && !back.sneakingRealignsPitch
				&& back.maxSpeed == c.maxSpeed && back.altitudeDeterminesSpeed, "payload did not round-trip");
		helper.succeed();
	}
}
