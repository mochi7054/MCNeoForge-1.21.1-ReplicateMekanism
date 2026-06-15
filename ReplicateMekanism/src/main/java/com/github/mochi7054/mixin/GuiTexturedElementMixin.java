package com.github.mochi7054.mixin;

import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.imaginator.ImaginatorScreen;
import com.github.mochi7054.collapser.CollapserScreen;

@Mixin(value = GuiTexturedElement.class, remap = false)
public class GuiTexturedElementMixin {

    private static final ResourceLocation CUSTOM_SLOT_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/slot.png");

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    private void onGetResource(CallbackInfoReturnable<ResourceLocation> cir) {
        if ((Object) this instanceof GuiSlot) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof ImaginatorScreen || mc.screen instanceof CollapserScreen) {
                cir.setReturnValue(CUSTOM_SLOT_TEXTURE);
            }
        }
    }
}
