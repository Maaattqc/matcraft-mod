package qc.mat.factioncore.mixin.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import qc.mat.factioncore.BlockingStateAccess;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements BlockingStateAccess {
	@Unique
	private boolean factioncore$blocking;

	@Override
	public boolean factioncore$isBlocking() {
		return factioncore$blocking;
	}

	@Override
	public void factioncore$setBlocking(boolean blocking) {
		this.factioncore$blocking = blocking;
	}
}
