package com.github.mochi7054.mixin;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.chemical.RMChemical;

@Mixin(MekanismRenderer.class)
public class MekanismRendererMixin {

    @Inject(method = "getTint", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetTint(Holder<Chemical> holder, CallbackInfoReturnable<Integer> cir) {
        if (holder != null && holder.value() == RMChemical.REPLICA.get()) {
            cir.setReturnValue(0xFFFFFF);
        }
    }

    @Inject(method = "getColorARGB(Lnet/minecraft/core/Holder;F)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetColorARGB(Holder<Chemical> holder, float alpha, CallbackInfoReturnable<Integer> cir) {
        if (holder != null && holder.value() == RMChemical.REPLICA.get()) {
            int argb = MekanismRenderer.getColorARGB(0xFFFFFF, alpha);
            cir.setReturnValue(argb);
        }
    }

    @Inject(method = "getColorARGB(Lmekanism/api/chemical/ChemicalStack;F)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetColorARGBStack(ChemicalStack stack, float alpha, CallbackInfoReturnable<Integer> cir) {
        if (stack != null && !stack.isEmpty() && stack.getChemicalHolder().value() == RMChemical.REPLICA.get()) {
            int argb = MekanismRenderer.getColorARGB(0xFFFFFF, alpha);
            cir.setReturnValue(argb);
        }
    }

    @Inject(method = "color(Lnet/minecraft/client/gui/GuiGraphics;Lmekanism/api/chemical/ChemicalStack;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onColorStack(net.minecraft.client.gui.GuiGraphics guiGraphics, ChemicalStack stack, CallbackInfo ci) {
        if (stack != null && !stack.isEmpty() && stack.getChemicalHolder().value() == RMChemical.REPLICA.get()) {
            MekanismRenderer.color(guiGraphics, 0xFFFFFF, 1.0f);
            ci.cancel();
        }
    }
}
