package com.github.mochi7054.mixin;

import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BasicEnergyContainer.class, remap = false)
public abstract class BasicEnergyContainerMixin implements com.github.mochi7054.IOwnerTrackedContainer {

    private TileEntityMekanism replicateMekanism$owner;

    @Override
    public TileEntityMekanism getReplicateMekanism$owner() {
        return this.replicateMekanism$owner;
    }

    @Override
    public void setReplicateMekanism$owner(TileEntityMekanism owner) {
        this.replicateMekanism$owner = owner;
    }

    @ModifyVariable(
        method = "insert",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private long modifyInsertAmount(long amount, long originalAmount, Action action, AutomationType automationType) {
        if (automationType == AutomationType.INTERNAL && replicateMekanism$owner instanceof mekanism.generators.common.tile.TileEntityGenerator tile) {
            if (tile.getComponent() != null && tile.getComponent().isUpgradeInstalled(mekanism.api.Upgrade.valueOf("REPLICA"))) {
                System.out.println("[ReplicateMekanismDebug] Doubling energy generation! Old amount: " + amount + ", New: " + (amount * 2));
                return amount * 2;
            }
        }
        return amount;
    }
}
