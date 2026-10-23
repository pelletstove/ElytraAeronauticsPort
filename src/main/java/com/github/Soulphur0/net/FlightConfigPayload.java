package com.github.Soulphur0.net;

import com.github.Soulphur0.config.FlightConfig;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: the flight settings the client must use, since the server decides how gliding works. */
public record FlightConfigPayload(FlightConfig config) implements CustomPacketPayload {
	public static final Type<FlightConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ean", "sync_config"));

	public static final StreamCodec<io.netty.buffer.ByteBuf, FlightConfigPayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				FlightConfig c = payload.config;
				buf.writeBoolean(c.altitudeDeterminesSpeed);
				buf.writeDouble(c.minSpeed);
				buf.writeDouble(c.maxSpeed);
				buf.writeDouble(c.minHeight);
				buf.writeDouble(c.maxHeight);
				buf.writeBoolean(c.sneakingRealignsPitch);
				buf.writeFloat(c.realignAngle);
				buf.writeFloat(c.realignRate);
			},
			buf -> {
				FlightConfig c = new FlightConfig();
				c.altitudeDeterminesSpeed = buf.readBoolean();
				c.minSpeed = buf.readDouble();
				c.maxSpeed = buf.readDouble();
				c.minHeight = buf.readDouble();
				c.maxHeight = buf.readDouble();
				c.sneakingRealignsPitch = buf.readBoolean();
				c.realignAngle = buf.readFloat();
				c.realignRate = buf.readFloat();
				return new FlightConfigPayload(c);
			});

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
