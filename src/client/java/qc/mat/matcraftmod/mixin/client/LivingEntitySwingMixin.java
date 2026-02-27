package qc.mat.matcraftmod.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntitySwingMixin {
	@Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
	private void matcraftmod$clampSwingDuration(CallbackInfoReturnable<Integer> cir) {
		if ((Object) this instanceof LocalPlayer) {
			int duration = cir.getReturnValue();
			if (duration > 4) {
				cir.setReturnValue(4);
			}
		}
	}
}
