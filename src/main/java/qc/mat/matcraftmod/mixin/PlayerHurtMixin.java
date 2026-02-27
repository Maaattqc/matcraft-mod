package qc.mat.matcraftmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import qc.mat.matcraftmod.ServerBlockingTracker;

@Mixin(Player.class)
public class PlayerHurtMixin {

	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float matcraftmod$reduceDamageWhenBlocking(float amount, ServerLevel serverLevel, DamageSource source) {
		Player self = (Player) (Object) this;
		if (!(self instanceof ServerPlayer)) return amount;
		if (!ServerBlockingTracker.isBlocking(self.getUUID())) return amount;

		Entity attacker = source.getEntity();
		if (!(attacker instanceof LivingEntity)) return amount;

		// Direction check: only reduce if attacker is in front (dot > 0)
		Vec3 lookDir = self.getLookAngle();
		Vec3 toAttacker = attacker.position().subtract(self.position()).normalize();
		double dot = lookDir.x * toAttacker.x + lookDir.z * toAttacker.z;
		if (dot > 0) {
			return amount * 0.5f;
		}

		return amount;
	}
}
