package com.github.mochi7054.mixin;

import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import com.github.mochi7054.collapser.CollapserBlockEntity;
import java.util.Map;

@Mixin(value = GuiConfigTypeTab.class, remap = false)
public class GuiConfigTypeTabMixin {

    @Shadow private TransmissionType transmission;
    @Shadow private GuiSideConfiguration<?> config;
    @Shadow private Map<TransmissionType, Tooltip> typeTooltips;

    @Inject(method = "updateTooltip", at = @At("HEAD"))
    private void onUpdateTooltip(int x, int y, CallbackInfo ci) {
        if (transmission == TransmissionType.FLUID) {
            try {
                java.lang.reflect.Field tileField = GuiSideConfiguration.class.getDeclaredField("tile");
                tileField.setAccessible(true);
                Object tile = tileField.get(config);
                if (tile instanceof ImaginatorBlockEntity || tile instanceof CollapserBlockEntity) {
                    typeTooltips.put(TransmissionType.FLUID, Tooltip.create(Component.translatable("replicatemekanism.matter")));
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
