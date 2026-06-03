package com.github.mochi7054.mixin;

import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.api.Action;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.ReplicateMekanism;

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
        if (assemblicator.getComponent() != null && assemblicator.getComponent().isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
            if (stack != null && !stack.isEmpty()) {
                ItemStack doubled = stack.copy();
                doubled.setCount(doubled.getCount() * 2);
                return doubled;
            }
        }
        return stack;
    }
}
