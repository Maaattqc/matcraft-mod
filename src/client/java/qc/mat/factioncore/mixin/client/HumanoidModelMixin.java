package qc.mat.factioncore.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.BlockingPoseSettings;
import qc.mat.factioncore.BlockingStateAccess;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
	@Shadow public ModelPart rightArm;
	@Shadow public ModelPart leftArm;

	@Redirect(
		method = "setupAttackAnimation",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Ease;outQuart(F)F"
		)
	)
	private float factioncore$useClassicSwingCurve(float f) {
		// Replace new outQuart easing with classic sqrt curve — instant swing, no follow-through
		return Mth.sqrt(f);
	}

	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void factioncore$poseBlockingArm(HumanoidRenderState state, CallbackInfo ci) {
		if (!(state instanceof BlockingStateAccess blockingState) || !blockingState.factioncore$isBlocking()) return;

		ModelPart arm = state.mainArm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
		arm.xRot += BlockingPoseSettings.tpArmRaiseDeg * (float) (Math.PI / 180.0);
	}
}
