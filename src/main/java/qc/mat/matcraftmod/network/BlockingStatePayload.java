package qc.mat.matcraftmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BlockingStatePayload(int entityId, boolean blocking) implements CustomPacketPayload {
	public static final Type<BlockingStatePayload> TYPE = new Type<>(
		Identifier.fromNamespaceAndPath("matcraftmod", "blocking_state")
	);

	public static final StreamCodec<ByteBuf, BlockingStatePayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, BlockingStatePayload::entityId,
		ByteBufCodecs.BOOL, BlockingStatePayload::blocking,
		BlockingStatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
