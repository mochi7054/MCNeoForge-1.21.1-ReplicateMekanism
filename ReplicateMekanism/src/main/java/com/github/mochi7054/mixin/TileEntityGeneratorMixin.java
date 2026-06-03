package com.github.mochi7054.mixin;

import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityGenerator.class, remap = false)
public abstract class TileEntityGeneratorMixin extends TileEntityMekanism {

    @Shadow
    private BasicEnergyContainer energyContainer;

    public TileEntityGeneratorMixin(net.minecraft.core.Holder<net.minecraft.world.level.block.Block> blockProvider, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(blockProvider, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            java.lang.reflect.Field supportsUpgradesField = TileEntityMekanism.class.getDeclaredField("supportsUpgrades");
            supportsUpgradesField.setAccessible(true);
            supportsUpgradesField.setBoolean(this, true);

            java.lang.reflect.Field canBeUpgradedField = TileEntityMekanism.class.getDeclaredField("canBeUpgraded");
            canBeUpgradedField.setAccessible(true);
            canBeUpgradedField.setBoolean(this, true);

            java.lang.reflect.Field upgradeComponentField = TileEntityMekanism.class.getDeclaredField("upgradeComponent");
            upgradeComponentField.setAccessible(true);
            
            TileComponentUpgrade upgradeComp = (TileComponentUpgrade) upgradeComponentField.get(this);
            if (upgradeComp == null) {
                upgradeComp = new TileComponentUpgrade(this);
                upgradeComponentField.set(this, upgradeComp);
                this.addComponent(upgradeComp);
            }
            if (this.energyContainer instanceof com.github.mochi7054.IOwnerTrackedContainer tracker) {
                tracker.setReplicateMekanism$owner(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
