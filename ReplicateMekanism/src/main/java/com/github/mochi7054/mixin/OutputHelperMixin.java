package com.github.mochi7054.mixin;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.outputs.OutputHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = OutputHelper.class, remap = false)
public class OutputHelperMixin {

    @ModifyVariable(
        method = "handleOutput(Lmekanism/api/inventory/IInventorySlot;Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private static ItemStack modifyItemStackOutput(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug] modifyItemStackOutput called. stack: {}, count: {}, isReplicaActive: {}", stack.getItem(), stack.getCount(), ReplicaRecipeTracker.isReplicaActive.get());
        }
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            ItemStack doubled = stack.copy();
            doubled.setCount(doubled.getCount() * 2);
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug]   Doubled ItemStack output. New count: {}", doubled.getCount());
            return doubled;
        }
        return stack;
    }

    @ModifyVariable(
        method = "handleOutput(Lmekanism/api/fluid/IExtendedFluidTank;Lnet/neoforged/neoforge/fluids/FluidStack;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private static FluidStack modifyFluidStackOutput(FluidStack stack) {
        if (stack != null && !stack.isEmpty()) {
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug] modifyFluidStackOutput called. amount: {}, isReplicaActive: {}", stack.getAmount(), ReplicaRecipeTracker.isReplicaActive.get());
        }
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            FluidStack doubled = stack.copy();
            doubled.setAmount(doubled.getAmount() * 2);
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug]   Doubled FluidStack output. New amount: {}", doubled.getAmount());
            return doubled;
        }
        return stack;
    }

    @ModifyVariable(
        method = "handleOutput(Lmekanism/api/chemical/IChemicalTank;Lmekanism/api/chemical/ChemicalStack;I)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private static ChemicalStack modifyChemicalStackOutput(ChemicalStack stack) {
        if (stack != null && !stack.isEmpty()) {
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug] modifyChemicalStackOutput called. amount: {}, isReplicaActive: {}", stack.getAmount(), ReplicaRecipeTracker.isReplicaActive.get());
        }
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            ChemicalStack doubled = stack.copy();
            doubled.setAmount(doubled.getAmount() * 2);
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug]   Doubled ChemicalStack output. New amount: {}", doubled.getAmount());
            return doubled;
        }
        return stack;
    }

}
