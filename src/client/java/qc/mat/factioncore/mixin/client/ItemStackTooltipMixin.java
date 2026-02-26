package qc.mat.factioncore.mixin.client;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {

	@Inject(method = "addAttributeTooltips", at = @At("HEAD"), cancellable = true)
	private void factioncore$hideAttackSpeed(Consumer<Component> consumer, TooltipDisplay tooltipDisplay, Player player, CallbackInfo ci) {
		ci.cancel();

		ItemStack self = (ItemStack) (Object) this;

		for (EquipmentSlotGroup equipmentSlotGroup : EquipmentSlotGroup.values()) {
			MutableBoolean mutableBoolean = new MutableBoolean(true);
			self.forEachModifier(equipmentSlotGroup, (holder, attributeModifier, display) -> {
				if (holder.is(Attributes.ATTACK_SPEED)) return;

				if (display != ItemAttributeModifiers.Display.hidden()) {
					if (mutableBoolean.isTrue()) {
						consumer.accept(CommonComponents.EMPTY);
						consumer.accept(Component.translatable("item.modifiers." + equipmentSlotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
						mutableBoolean.setFalse();
					}

					display.apply(consumer, player, holder, attributeModifier);
				}
			});
		}
	}

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void factioncore$spoofNamespace(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
		List<Component> lines = cir.getReturnValue();
		ItemStack self = (ItemStack) (Object) this;
		Identifier id = BuiltInRegistries.ITEM.getKey(self.getItem());

		if (id != null && !"minecraft".equals(id.getNamespace())) {
			String spoofed = "minecraft:" + id.getPath();
			for (int i = 0; i < lines.size(); i++) {
				String text = lines.get(i).getString();
				if (text.equals(id.toString())) {
					lines.set(i, Component.literal(spoofed).withStyle(ChatFormatting.DARK_GRAY));
					break;
				}
			}
		}
	}
}
