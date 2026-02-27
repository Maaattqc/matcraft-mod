package qc.mat.matcraftmod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.matcraftmod.BlockingStateAccess;
import qc.mat.matcraftmod.CombatStateTracker;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
	private void matcraftmod$trackEntityBlocking(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		boolean isLocalPlayer = entity == Minecraft.getInstance().player;
		boolean entityBlocking;

		if (isLocalPlayer) {
			entityBlocking = CombatStateTracker.isBlocking();
		} else {
			entityBlocking = CombatStateTracker.isRemoteBlocking(entity.getId());
		}

		if (state instanceof BlockingStateAccess blockingState) {
			blockingState.matcraftmod$setBlocking(entityBlocking);
		}
	}
}
