package qc.mat.factioncore.mixin.client;

import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void factioncore$removeCopyright(CallbackInfo ci) {
		var toRemove = new ArrayList<PlainTextButton>();
		for (var child : this.children()) {
			if (child instanceof PlainTextButton) {
				toRemove.add((PlainTextButton) child);
			}
		}
		for (var widget : toRemove) {
			this.removeWidget(widget);
		}
	}
}
