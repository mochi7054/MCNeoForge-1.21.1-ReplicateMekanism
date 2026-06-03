package com.github.mochi7054.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = BasicInventorySlot.class, remap = false)
public class BasicInventorySlotMixin {

    @ModifyVariable(
        method = "insertItem(Lnet/minecraft/world/item/ItemStack;Lmekanism/api/Action;Lmekanism/api/AutomationType;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack modifyInsertItemStack(ItemStack stack) {
        if (ReplicaRecipeTracker.isReplicaActive.get() == Boolean.TRUE && stack != null && !stack.isEmpty()) {
            ItemStack doubled = stack.copy();
            doubled.setCount(doubled.getCount() * 2);
            return doubled;
        }
        return stack;
    }
}
