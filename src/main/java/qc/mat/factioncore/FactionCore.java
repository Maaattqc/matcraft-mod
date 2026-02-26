package qc.mat.factioncore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import qc.mat.factioncore.network.BlockingStatePayload;

public class FactionCore implements ModInitializer {
	private static final Identifier ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath("factioncore", "no_cooldown");

	@Override
	public void onInitialize() {
		// Register payloads (server-side)
		PayloadTypeRegistry.playC2S().register(BlockingStatePayload.TYPE, BlockingStatePayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BlockingStatePayload.TYPE, BlockingStatePayload.STREAM_CODEC);

		// Remove attack cooldown when player joins
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
			if (attackSpeed != null) {
				attackSpeed.removeModifier(ATTACK_SPEED_ID);
				attackSpeed.addPermanentModifier(new AttributeModifier(
					ATTACK_SPEED_ID, 1020.0, AttributeModifier.Operation.ADD_VALUE
				));
			}
		});

		// Receive blocking state from clients
		ServerPlayNetworking.registerGlobalReceiver(BlockingStatePayload.TYPE, (payload, context) -> {
			ServerPlayer sender = context.player();
			boolean blocking = payload.blocking();

			ServerBlockingTracker.setBlocking(sender.getUUID(), blocking);

			// Cancel sprint on server when blocking
			if (blocking && sender.isSprinting()) {
				sender.setSprinting(false);
			}

			// Broadcast to nearby players so they see the blocking pose
			BlockingStatePayload broadcast = new BlockingStatePayload(sender.getId(), blocking);
			ServerLevel level = (ServerLevel) sender.level();
			for (ServerPlayer other : level.players()) {
				if (other != sender && other.distanceToSqr(sender) < 64 * 64) {
					other.connection.send(new ClientboundCustomPayloadPacket(broadcast));
				}
			}
		});

		// Clean up on disconnect
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID uuid = handler.getPlayer().getUUID();
			ServerBlockingTracker.remove(uuid);
			AttackTracker.removePlayer(uuid);
		});
	}
}
