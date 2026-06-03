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
    private ChemicalStack modifyInsertChemicalStack(ChemicalStack stack) {
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            ChemicalStack doubled = stack.copy();
            doubled.setAmount(doubled.getAmount() * 2);
            return doubled;
        }
        return stack;
    }
}
