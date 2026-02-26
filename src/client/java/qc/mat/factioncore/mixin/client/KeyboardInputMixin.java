package qc.mat.factioncore.mixin.client;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.CombatStateTracker;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
	@Inject(method = "tick", at = @At("TAIL"))
	private void factioncore$slowWhenBlocking(CallbackInfo ci) {
		if (CombatStateTracker.isBlocking()) {
			this.moveVector = this.moveVector.scale(0.3f);
		}
	}
}
