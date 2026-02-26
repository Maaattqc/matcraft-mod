package qc.mat.factioncore.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qc.mat.factioncore.CombatStateTracker;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow public HitResult hitResult;
	@Shadow @Final public Options options;
	@Shadow public LocalPlayer player;
	@Shadow public MultiPlayerGameMode gameMode;

	// Prevent entity attacks and air swings while blocking (only block breaking allowed)
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void factioncore$preventEntityAttackWhileBlocking(CallbackInfoReturnable<Boolean> cir) {
		if (CombatStateTracker.isBlocking() && !(this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK)) {
			cir.setReturnValue(false);
		}
	}

	// Allow block breaking while eating — manually process attacks at HEAD of handleKeybinds
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void factioncore$breakBlocksWhileEating(CallbackInfo ci) {
		if (this.player == null || this.gameMode == null) return;
		if (!this.player.isUsingItem()) return;
		if (this.hitResult == null || this.hitResult.getType() != HitResult.Type.BLOCK) return;

		BlockHitResult blockHit = (BlockHitResult) this.hitResult;
		BlockPos blockPos = blockHit.getBlockPos();
		Direction direction = blockHit.getDirection();

		// Handle new attack clicks
		while (this.options.keyAttack.consumeClick()) {
			this.gameMode.startDestroyBlock(blockPos, direction);
			this.player.swing(InteractionHand.MAIN_HAND);
		}

		// Handle held attack (continue destroying)
		if (this.options.keyAttack.isDown()) {
			this.gameMode.continueDestroyBlock(blockPos, direction);
			this.player.swing(InteractionHand.MAIN_HAND);
		}
	}
}
