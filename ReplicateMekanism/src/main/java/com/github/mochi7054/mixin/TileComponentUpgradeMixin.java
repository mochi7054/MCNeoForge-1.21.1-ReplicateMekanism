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
    private mekanism.common.tile.base.TileEntityMekanism tile;

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

    @Inject(method = "addUpgrades(Lmekanism/api/Upgrade;I)I", at = @At("RETURN"))
    private void onAddUpgrades(Upgrade upgrade, int installed, CallbackInfoReturnable<Integer> cir) {
        if (upgrade == com.github.mochi7054.ReplicateMekanism.REPLICA_UPGRADE_TYPE) {
            if (this.tile != null && this.tile.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                var pos = this.tile.getBlockPos();
                for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
                    if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 256.0) {
                        com.github.mochi7054.ReplicateMekanism.checkAndAwardCheatedAdvancement(player);
                    }
                }
            }
        }
    }
}
