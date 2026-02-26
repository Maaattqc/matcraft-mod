package qc.mat.factioncore.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qc.mat.factioncore.HudOverlay;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	private static final double MAX_REACH = 3.03;
	private static final long MIN_ATTACK_INTERVAL_MS = 100; // 10 CPS max
	private long lastAttackTime = 0;

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void factioncore$trackReach(Player player, Entity target, CallbackInfo ci) {
		long now = System.currentTimeMillis();
		if (now - lastAttackTime < MIN_ATTACK_INTERVAL_MS) {
			ci.cancel();
			return;
		}

		if (target instanceof LivingEntity) {
			Vec3 eye = player.getEyePosition();
			AABB box = target.getBoundingBox();
			double dx = Math.max(box.minX - eye.x, Math.max(0, eye.x - box.maxX));
			double dy = Math.max(box.minY - eye.y, Math.max(0, eye.y - box.maxY));
			double dz = Math.max(box.minZ - eye.z, Math.max(0, eye.z - box.maxZ));
			double reach = Math.sqrt(dx * dx + dy * dy + dz * dz);
			HudOverlay.setLastReach(reach);
			if (reach > MAX_REACH) {
				ci.cancel();
				return;
			}
		}

		lastAttackTime = now;
	}
}
