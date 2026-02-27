package qc.mat.matcraftmod;

import java.util.ArrayDeque;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class HudOverlay {
	private static final ArrayDeque<Long> clickTimestamps = new ArrayDeque<>();
	private static double lastReach = 0.0;
	private static boolean wasLeftClickDown = false;

	// Cached strings, updated in tick (20/sec) instead of render (every frame)
	private static String cachedFps = "0 FPS";
	private static String cachedCps = "0 CPS";
	private static String cachedReach = "0.00 Reach";

	public static void setLastReach(double reach) {
		lastReach = reach;
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null) {
			wasLeftClickDown = false;
			return;
		}

		long window = mc.getWindow().handle();
		boolean leftClickDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

		if (leftClickDown && !wasLeftClickDown) {
			clickTimestamps.add(System.currentTimeMillis());
		}
		wasLeftClickDown = leftClickDown;

		// Purge clicks older than 1 second
		long now = System.currentTimeMillis();
		while (!clickTimestamps.isEmpty() && now - clickTimestamps.peek() > 1000) {
			clickTimestamps.poll();
		}

		// Update cached strings once per tick
		cachedFps = mc.getFps() + " FPS";
		cachedCps = clickTimestamps.size() + " CPS";
		cachedReach = String.format("%.2f Reach", lastReach);
	}

	public static void render(GuiGraphics graphics, float tickDelta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;

		Font font = mc.font;
		int x = 4;
		int y = 4;
		int lineHeight = font.lineHeight + 2;

		graphics.drawString(font, cachedFps, x, y, 0xFFFFFFFF);
		graphics.drawString(font, cachedCps, x, y + lineHeight, 0xFFFFFFFF);
		graphics.drawString(font, cachedReach, x, y + lineHeight * 2, 0xFFFFFFFF);
	}
}
