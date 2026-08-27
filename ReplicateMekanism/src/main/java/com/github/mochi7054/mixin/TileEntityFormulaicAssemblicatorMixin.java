package com.github.mochi7054.mixin;

import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.api.Action;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = TileEntityFormulaicAssemblicator.class, remap = false)
public class TileEntityFormulaicAssemblicatorMixin {

    @ModifyVariable(
        method = "tryMoveToOutput",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack modifyCraftedOutput(ItemStack stack, ItemStack originalStack, Action action) {
        TileEntityFormulaicAssemblicator assemblicator = (TileEntityFormulaicAssemblicator) (Object) this;
        int mult = ReplicaRecipeTracker.getReplicaMultiplier(assemblicator);
        if (action.execute() && mult > 1 && stack != null && !stack.isEmpty()) {
            ItemStack multiplied = stack.copy();
            long newCount = (long) multiplied.getCount() * mult;
            multiplied.setCount((int) Math.min(Integer.MAX_VALUE, newCount));
            return multiplied;
        }
        return stack;
    }
}