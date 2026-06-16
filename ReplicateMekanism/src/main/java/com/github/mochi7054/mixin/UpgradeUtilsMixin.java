package com.github.mochi7054.mixin;

import java.util.Collections;
import java.util.List;
import mekanism.api.Upgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.ReplicateMekanism;

@Mixin(value = UpgradeUtils.class, remap = false)
public class UpgradeUtilsMixin {
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private static void onGetItem(Upgrade upgrade, CallbackInfoReturnable<Holder<?>> cir) {
        if (cir.isCancelled()) {
            return;
        }
        if (upgrade != null) {
            if (upgrade.name().equals("REPLICA")) {
                cir.setReturnValue(ReplicateMekanism.REPLICA_UPGRADE);
            } else {
                String name = upgrade.name();
                if (!name.equals("SPEED") && !name.equals("ENERGY") && !name.equals("FILTER") && 
                    !name.equals("MUFFLING") && !name.equals("CHEMICAL") && !name.equals("ANCHOR") && 
                    !name.equals("STONE_GENERATOR")) {
                    cir.setReturnValue(mekanism.common.registries.MekanismItems.SPEED_UPGRADE);
                }
            }
        }
    }

    @Inject(method = "getStack(Lmekanism/api/Upgrade;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void onGetStackSingle(Upgrade upgrade, CallbackInfoReturnable<net.minecraft.world.item.ItemStack> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(new net.minecraft.world.item.ItemStack(ReplicateMekanism.REPLICA_UPGRADE.get()));
        }
    }

    @Inject(method = "getStack(Lmekanism/api/Upgrade;I)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void onGetStackCount(Upgrade upgrade, int count, CallbackInfoReturnable<net.minecraft.world.item.ItemStack> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(new net.minecraft.world.item.ItemStack(ReplicateMekanism.REPLICA_UPGRADE.get(), count));
        }
    }

    @Inject(method = "getInfo", at = @At("HEAD"), cancellable = true)
    private static void onGetInfo(net.minecraft.world.level.block.entity.BlockEntity tile, Upgrade upgrade, CallbackInfoReturnable<List<?>> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(Collections.singletonList(
                Component.translatable("gui.mekanism.upgrades.effect", "2")
            ));
        }
    }

    @Inject(method = "getExpScaledInfo", at = @At("HEAD"), cancellable = true)
    private static void onGetExpScaledInfo(mekanism.common.tile.interfaces.IUpgradeTile tile, Upgrade upgrade, CallbackInfoReturnable<List<?>> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(Collections.singletonList(
                Component.translatable("gui.mekanism.upgrades.effect", "2")
            ));
        }
    }

    @Inject(method = "getMultScaledInfo", at = @At("HEAD"), cancellable = true)
    private static void onGetMultScaledInfo(mekanism.common.tile.interfaces.IUpgradeTile tile, Upgrade upgrade, CallbackInfoReturnable<List<?>> cir) {
        if (upgrade != null && upgrade.name().equals("REPLICA")) {
            cir.setReturnValue(Collections.singletonList(
                Component.translatable("gui.mekanism.upgrades.effect", "2")
            ));
        }
    }
}
