package com.github.mochi7054.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientLanguage.class, remap = true)
public class ClientLanguageMixin {

    @Inject(method = "getOrDefault(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    public void onGetOrDefault(String key, CallbackInfoReturnable<String> cir) {
        if ("transmission.mekanism.fluid".equals(key)) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.screen != null) {
                String className = mc.screen.getClass().getName();
                if (className.contains("ImaginatorScreen") || className.contains("CollapserScreen")) {
                    String lang = mc.getLanguageManager().getSelected();
                    if (lang != null && lang.contains("ja")) {
                        cir.setReturnValue("マター");
                    } else {
                        cir.setReturnValue("Matter");
                    }
                }
            }
        }
    }
}
