package com.github.mochi7054.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = BasicChemicalTank.class, remap = false)
public class BasicChemicalTankMixin {

    @ModifyVariable(
        method = "insert(Lmekanism/api/chemical/ChemicalStack;Lmekanism/api/Action;Lmekanism/api/AutomationType;)Lmekanism/api/chemical/ChemicalStack;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ChemicalStack modifyInsertChemicalStack(ChemicalStack stack, ChemicalStack originalStack, Action action, AutomationType automationType) {
        int mult = ReplicaRecipeTracker.currentMultiplier.get();
        if (action.execute() && mult > 1 && stack != null && !stack.isEmpty()) {
            ChemicalStack multiplied = stack.copy();
            long newAmount = multiplied.getAmount() * mult;
            multiplied.setAmount(newAmount);
            return multiplied;
        }
        return stack;
    }
}