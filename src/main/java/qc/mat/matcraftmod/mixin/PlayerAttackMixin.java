package qc.mat.matcraftmod.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.matcraftmod.AttackTracker;

@Mixin(Player.class)
public class PlayerAttackMixin {

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void matcraftmod$cpsCap(Entity target, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		if (!(self instanceof ServerPlayer)) return;

		if (!AttackTracker.tryAttack(self.getUUID())) {
			ci.cancel();
		}
	}
}
