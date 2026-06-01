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
    @Shadow
    private Set<Upgrade> supported;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(mekanism.common.tile.base.TileEntityMekanism tile, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.supported == null) {
            this.supported = java.util.EnumSet.of(com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE);
        } else {
            try {
                this.supported.add(com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE);
            } catch (UnsupportedOperationException e) {
                Set<Upgrade> mutable = new java.util.HashSet<>(this.supported);
                mutable.add(com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE);
                this.supported = java.util.Collections.unmodifiableSet(mutable);
            }
        }
    }

    @Inject(method = "supports", at = @At("HEAD"), cancellable = true)
    private void onSupports(Upgrade upgrade, CallbackInfoReturnable<Boolean> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(true);
        }
    }
}
