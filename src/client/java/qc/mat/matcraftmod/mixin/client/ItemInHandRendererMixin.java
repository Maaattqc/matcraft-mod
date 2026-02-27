package qc.mat.matcraftmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Mth;
import qc.mat.matcraftmod.BlockingPoseSettings;
import qc.mat.matcraftmod.CombatStateTracker;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow
	private void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {}

	@Shadow
	private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f) {}

	@Shadow
	public abstract void renderItem(
		LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext,
		PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i
	);

	@Shadow
	private void swingArm(float f, PoseStack poseStack, int i, HumanoidArm humanoidArm) {}

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void matcraftmod$renderBlockingPose(
		AbstractClientPlayer player, float partialTick, float pitch,
		InteractionHand hand, float swingProgress, ItemStack itemStack,
		float equipProgress, PoseStack poseStack, SubmitNodeCollector collector, int light,
		CallbackInfo ci
	) {
		if (hand != InteractionHand.MAIN_HAND) return;
		if (!CombatStateTracker.canBlock(itemStack)) return;
		if (player.isScoping()) return;
		if (!CombatStateTracker.isBlocking()) return;

		ci.cancel();

		HumanoidArm arm = player.getMainArm();
		boolean isRightHand = arm == HumanoidArm.RIGHT;
		int direction = isRightHand ? 1 : -1;

		poseStack.pushPose();

		// Base arm positioning (equipProgress only, no swing — matches 1.7.10 behavior)
		this.applyItemArmTransform(poseStack, arm, equipProgress);

		// Vanilla 1.21.11 BLOCK transforms for non-shield items
		poseStack.translate(direction * -0.14142136F, 0.08F + BlockingPoseSettings.fpHeightOffset, 0.14142136F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Axis.YP.rotationDegrees(direction * 13.365F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(direction * 78.05F));

		if (BlockingPoseSettings.fpTiltDeg != 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(BlockingPoseSettings.fpTiltDeg));
		}

		if (swingProgress > 0.0F) {
			float swingAmount = Mth.sin((float) Math.pow(swingProgress, 0.65) * (float) Math.PI);
			poseStack.mulPose(Axis.XP.rotationDegrees(swingAmount * BlockingPoseSettings.swingPitchDeg));
			poseStack.mulPose(Axis.YP.rotationDegrees(direction * swingAmount * BlockingPoseSettings.swingYawDeg));
			poseStack.translate(0.0F, 0.0F, swingAmount * BlockingPoseSettings.swingPushZ);
		}

		this.renderItem(
			player, itemStack,
			isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
			poseStack, collector, light
		);

		poseStack.popPose();
	}

	// Layer swing animation on top of eating animation (1.7.10 behavior)
	@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		ordinal = 1))
	private void matcraftmod$mergeEatingAndSwing(
		AbstractClientPlayer player, float partialTick, float pitch,
		InteractionHand hand, float swingProgress, ItemStack itemStack,
		float equipProgress, PoseStack poseStack, SubmitNodeCollector collector, int light,
		CallbackInfo ci
	) {
		if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0
			&& player.getUsedItemHand() == hand && swingProgress > 0.0F) {
			HumanoidArm arm = (hand == InteractionHand.MAIN_HAND)
				? player.getMainArm() : player.getMainArm().getOpposite();
			int direction = (arm == HumanoidArm.RIGHT) ? 1 : -1;
			this.swingArm(swingProgress * BlockingPoseSettings.eatSwingScale, poseStack, direction, arm);
		}
	}
}
