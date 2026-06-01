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

    @Inject(method = "getSupportedTypes", at = @At("RETURN"), cancellable = true)
    private void onGetSupportedTypes(CallbackInfoReturnable<Set<Upgrade>> cir) {
        Set<Upgrade> supported = cir.getReturnValue();
        if (supported != null && !supported.contains(com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
            Set<Upgrade> mutable = new java.util.HashSet<>(supported);
            mutable.add(com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE);
            cir.setReturnValue(java.util.Collections.unmodifiableSet(mutable));
        }
    }
}
