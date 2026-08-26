package com.github.mochi7054.mixin;

import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

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
        if (action.execute() && automationType == AutomationType.INTERNAL && replicateMekanism$owner instanceof mekanism.generators.common.tile.TileEntityGenerator tile) {
            int mult = ReplicaRecipeTracker.getReplicaMultiplier(tile);
            if (mult > 1) {
                return amount * mult;
            }
        }
        return amount;
    }
}