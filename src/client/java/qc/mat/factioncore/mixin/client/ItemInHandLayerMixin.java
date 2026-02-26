package qc.mat.factioncore.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.BlockingPoseSettings;
import qc.mat.factioncore.BlockingStateAccess;
import qc.mat.factioncore.CombatStateTracker;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
	@Inject(
		method = "submitArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
		)
	)
	private void factioncore$applyBlockingSwordTransform(
		ArmedEntityRenderState state,
		ItemStackRenderState itemStackRenderState,
		ItemStack itemStack,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int i,
		CallbackInfo ci
	) {
		if (!(state instanceof BlockingStateAccess blockingState) || !blockingState.factioncore$isBlocking()) return;
		if (state.mainArm != humanoidArm) return;
		if (!CombatStateTracker.canBlock(itemStack)) return;

		int dir = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;

		poseStack.translate(dir * BlockingPoseSettings.transX, BlockingPoseSettings.transY, BlockingPoseSettings.transZ);
		// Move origin to sword grip, rotate, then move back
		poseStack.translate(dir * BlockingPoseSettings.pivotX, BlockingPoseSettings.pivotY, BlockingPoseSettings.pivotZ);
		poseStack.mulPose(Axis.ZP.rotationDegrees(dir * BlockingPoseSettings.rotZ));
		poseStack.mulPose(Axis.YP.rotationDegrees(dir * BlockingPoseSettings.rotY));
		poseStack.mulPose(Axis.XP.rotationDegrees(BlockingPoseSettings.rotX));
		poseStack.mulPose(Axis.YP.rotationDegrees(BlockingPoseSettings.rotSpin));
		poseStack.translate(dir * -BlockingPoseSettings.pivotX, -BlockingPoseSettings.pivotY, -BlockingPoseSettings.pivotZ);
		// Push sword forward to render in front of body
		poseStack.translate(0.0F, 0.0F, -BlockingPoseSettings.depthOffset);
	}
}
