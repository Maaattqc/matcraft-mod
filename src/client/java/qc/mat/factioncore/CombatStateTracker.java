package qc.mat.factioncore;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import qc.mat.factioncore.network.BlockingStatePayload;

public class CombatStateTracker {
	private static boolean blocking = false;
	private static boolean lastSentBlocking = false;
	private static final Set<Integer> remoteBlockingEntities = new HashSet<>();

	public static boolean canBlock(ItemStack stack) {
		return stack.is(ItemTags.SWORDS)
			|| stack.is(ItemTags.AXES)
			|| stack.is(ItemTags.PICKAXES)
			|| stack.is(ItemTags.SHOVELS)
			|| stack.is(ItemTags.HOES);
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player == null) {
			blocking = false;
			lastSentBlocking = false;
			remoteBlockingEntities.clear();
			return;
		}

		ItemStack mainHand = player.getMainHandItem();
		boolean holdingSword = canBlock(mainHand);
		// Check raw mouse button state — keyUse.isDown() doesn't work when looking at air
		long window = mc.getWindow().handle();
		boolean rightClickHeld = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

		boolean wasBlocking = blocking;
		blocking = holdingSword && rightClickHeld && mc.screen == null;

		// Cancel sprint instantly on client when blocking starts
		if (blocking && player.isSprinting()) {
			player.setSprinting(false);
		}

		if (blocking != lastSentBlocking) {
			lastSentBlocking = blocking;
			var connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				connection.send(new ServerboundCustomPayloadPacket(new BlockingStatePayload(0, blocking)));
			}
		}
	}

	public static boolean isBlocking() {
		return blocking;
	}

	public static void setRemoteBlocking(int entityId, boolean blocking) {
		if (blocking) {
			remoteBlockingEntities.add(entityId);
		} else {
			remoteBlockingEntities.remove(entityId);
		}
	}

	public static boolean isRemoteBlocking(int entityId) {
		return remoteBlockingEntities.contains(entityId);
	}
}
