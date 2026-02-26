package qc.mat.factioncore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.CombatStateTracker;

@Mixin(LocalPlayer.class)
public class LocalPlayerSprintMixin {
	@Shadow private int sprintTriggerTime;

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void factioncore$preventSprintInputWhileBlocking(CallbackInfo ci) {
		if (CombatStateTracker.isBlocking()) {
			this.sprintTriggerTime = 0;
			// Consume sprint key toggle so it doesn't queue up
			Minecraft.getInstance().options.keySprint.consumeClick();
		}
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void factioncore$forceNoSprintWhileBlocking(CallbackInfo ci) {
		if (CombatStateTracker.isBlocking() && ((LocalPlayer) (Object) this).isSprinting()) {
			((LocalPlayer) (Object) this).setSprinting(false);
		}
	}
}
