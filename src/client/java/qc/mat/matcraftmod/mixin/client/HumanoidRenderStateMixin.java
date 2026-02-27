package qc.mat.matcraftmod.mixin.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import qc.mat.matcraftmod.BlockingStateAccess;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements BlockingStateAccess {
	@Unique
	private boolean matcraftmod$blocking;

	@Override
	public boolean matcraftmod$isBlocking() {
		return matcraftmod$blocking;
	}

	@Override
	public void matcraftmod$setBlocking(boolean blocking) {
		this.matcraftmod$blocking = blocking;
	}
}
