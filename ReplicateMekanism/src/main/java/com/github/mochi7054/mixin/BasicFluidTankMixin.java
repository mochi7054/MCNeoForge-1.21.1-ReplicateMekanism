package com.github.mochi7054.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = BasicFluidTank.class, remap = false)
public class BasicFluidTankMixin {

    @ModifyVariable(
        method = "insert(Lnet/neoforged/neoforge/fluids/FluidStack;Lmekanism/api/Action;Lmekanism/api/AutomationType;)Lnet/neoforged/neoforge/fluids/FluidStack;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private FluidStack modifyInsertFluidStack(FluidStack stack) {
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            FluidStack doubled = stack.copy();
            doubled.setAmount(doubled.getAmount() * 2);
            return doubled;
        }
        return stack;
    }
}
