package com.github.mochi7054.mixin;

import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileComponentUpgrade.class, remap = false)
public abstract class TileComponentUpgradeMixin {
    @Inject(method = "supports", at = @At("HEAD"), cancellable = true)
    private void onSupports(Upgrade upgrade, CallbackInfoReturnable<Boolean> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
    private void onIsSupported(Upgrade upgrade, CallbackInfoReturnable<Boolean> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(true);
        }
    }
}
