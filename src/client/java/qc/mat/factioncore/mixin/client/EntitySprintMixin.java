package qc.mat.factioncore.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.CombatStateTracker;

@Mixin(Entity.class)
public class EntitySprintMixin {
	@Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
	private void factioncore$preventSprintWhileBlocking(boolean sprinting, CallbackInfo ci) {
		if (sprinting && (Object) this instanceof LocalPlayer && CombatStateTracker.isBlocking()) {
			ci.cancel();
		}
	}
}
