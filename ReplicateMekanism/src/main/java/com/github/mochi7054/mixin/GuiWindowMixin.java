package com.github.mochi7054.mixin;

import mekanism.client.gui.element.window.GuiWindow;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import com.github.mochi7054.collapser.CollapserBlockEntity;

@Mixin(value = GuiWindow.class, remap = false)
public class GuiWindowMixin {

    @ModifyVariable(method = "drawTitleText", at = @At("HEAD"), argsOnly = true)
    private Component modifyTitleText(Component text) {
        if (text != null) {
            String str = text.getString();
            boolean isFluidTitle = str.contains("Fluid") || str.contains("流体") || str.contains("マター構成") || str.contains("Matter Configuration");
            
            if (text.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
                if ("gui.configuration.config".equals(translatable.getKey())) {
                    for (Object arg : translatable.getArgs()) {
                        if (arg != null && arg.toString().contains("FLUID")) {
                            isFluidTitle = true;
                            break;
                        }
                    }
                }
            }

            if (isFluidTitle) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof mekanism.client.gui.GuiMekanism<?> gui) {
                    if (gui.getMenu() instanceof mekanism.common.inventory.container.tile.MekanismTileContainer<?> container) {
                        Object tile = container.getTileEntity();
                        if (tile instanceof ImaginatorBlockEntity || tile instanceof CollapserBlockEntity) {
                            return Component.translatable("gui.configuration.config", Component.translatable("replicatemekanism.matter"));
                        }
                    }
                }
            }
        }
        return text;
    }
}
