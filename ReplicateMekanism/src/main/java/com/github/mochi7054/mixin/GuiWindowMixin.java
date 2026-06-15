package com.github.mochi7054.mixin;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.imaginator.ImaginatorScreen;
import com.github.mochi7054.collapser.CollapserScreen;

@Mixin(GuiWindow.class)
public class GuiWindowMixin {

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetResource(CallbackInfoReturnable<ResourceLocation> cir) {
        GuiWindow self = (GuiWindow) (Object) this;
        IGuiWrapper guiObj = self.gui();
        if (guiObj instanceof ImaginatorScreen || guiObj instanceof CollapserScreen) {
            cir.setReturnValue(ResourceLocation.fromNamespaceAndPath("replicatemekanism", "textures/gui/window.png"));
        }
    }
}
