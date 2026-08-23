package com.github.mochi7054.mixin;

import mekanism.api.Upgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.ReplicateMekanism;
import mekanism.common.tile.interfaces.IUpgradeTile;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = UpgradeUtils.class, remap = false)
public class UpgradeUtilsMixin {

    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private static void onGetItem(Upgrade upgrade, CallbackInfoReturnable<net.minecraft.core.Holder<net.minecraft.world.item.Item>> cir) {
        if (upgrade == ReplicateMekanism.REPLICA_UPGRADE_TYPE) {
            cir.setReturnValue(net.minecraft.core.Holder.direct(ReplicateMekanism.REPLICA_UPGRADE.get()));
        }
    }

    @Inject(method = "getInfo", at = @At("HEAD"), cancellable = true)
    private static void onGetInfo(BlockEntity tile, Upgrade upgrade, CallbackInfoReturnable<List<Component>> cir) {
        if (upgrade == ReplicateMekanism.REPLICA_UPGRADE_TYPE) {
            List<Component> info = new ArrayList<>();
            int installed = 0;
            if (tile instanceof IUpgradeTile upgradeTile) {
                var component = upgradeTile.getComponent();
                if (component != null) {
                    installed = component.getUpgrades(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
                }
            }
            
            // Current scaling: 2^N
            int currentMult = 1 << installed;
            int maxInstalled = upgrade.getMax();
            int maxMult = 1 << maxInstalled;

            info.add(Component.translatable("upgrade.replicatemekanism.replica.effect", currentMult, maxMult));
            cir.setReturnValue(info);
        }
    }
}
