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
            String key = "";
            if (text.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translatable) {
                key = translatable.getKey();
            }
            if ("transmission.mekanism.fluid".equals(key) || 
                text.getString().contains("Fluid Configuration") || 
                text.getString().contains("流体構成") ||
                text.getString().contains("Fluid") ||
                text.getString().contains("流体")) {
                
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.screen instanceof mekanism.client.gui.GuiMekanism<?> gui) {
                    if (gui.getMenu() instanceof mekanism.common.inventory.container.tile.MekanismTileContainer<?> container) {
                        Object tile = container.getTileEntity();
                        if (tile instanceof ImaginatorBlockEntity || tile instanceof CollapserBlockEntity) {
                            String lang = mc.getLanguageManager().getSelected();
                            if (lang != null && lang.contains("ja")) {
                                return Component.literal("マター構成");
                            } else {
                                return Component.literal("Matter Configuration");
                            }
                        }
                    }
                }
            }
        }
        return text;
    }
}
