package qc.mat.matcraftmod.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.matcraftmod.HudOverlay;

@Mixin(Gui.class)
public class GuiMixin {
	@Redirect(
		method = "renderCrosshair",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F")
	)
	private float matcraftmod$hideCrosshairIndicator(LocalPlayer instance, float partialTick) {
		return 1.0f;
	}

	@Redirect(
		method = "renderItemHotbar",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F")
	)
	private float matcraftmod$hideHotbarIndicator(LocalPlayer instance, float partialTick) {
		return 1.0f;
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void matcraftmod$renderHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		HudOverlay.render(graphics, deltaTracker.getGameTimeDeltaTicks());
	}
}
