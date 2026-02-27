package qc.mat.factioncore.mixin.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(Minecraft.class)
public abstract class WindowBrandingMixin {

	@Shadow
	public abstract Window getWindow();

	@Unique
	private boolean factioncore$iconSet = false;

	@Inject(method = "updateTitle", at = @At("TAIL"))
	private void factioncore$setCustomTitle(CallbackInfo ci) {
		getWindow().setTitle("MatCraft " + SharedConstants.getCurrentVersion().name());

		if (!factioncore$iconSet) {
			factioncore$iconSet = true;
			factioncore$setWindowIcon();
		}
	}

	@Unique
	private void factioncore$setWindowIcon() {
		try (InputStream stream = WindowBrandingMixin.class.getResourceAsStream("/assets/factioncore/icon.png")) {
			if (stream == null) return;
			byte[] bytes = stream.readAllBytes();

			ByteBuffer buf = BufferUtils.createByteBuffer(bytes.length);
			buf.put(bytes).flip();

			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer w = stack.mallocInt(1);
				IntBuffer h = stack.mallocInt(1);
				IntBuffer comp = stack.mallocInt(1);

				ByteBuffer pixels = STBImage.stbi_load_from_memory(buf, w, h, comp, 4);
				if (pixels != null) {
					GLFWImage.Buffer icons = GLFWImage.malloc(1);
					icons.position(0).width(w.get(0)).height(h.get(0)).pixels(pixels);
					GLFW.glfwSetWindowIcon(getWindow().handle(), icons);
					STBImage.stbi_image_free(pixels);
					icons.free();
				}
			}
		} catch (Exception ignored) {
		}
	}
}
