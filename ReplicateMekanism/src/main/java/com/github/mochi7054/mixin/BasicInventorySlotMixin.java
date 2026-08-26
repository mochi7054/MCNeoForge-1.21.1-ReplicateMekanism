package com.github.mochi7054.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = BasicInventorySlot.class, remap = false)
public class BasicInventorySlotMixin {

    @Inject(method = "getLimit(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void onGetLimit(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int mult = ReplicaRecipeTracker.currentMultiplier.get();
        if (mult > 1 && stack != null && !stack.isEmpty()) {
            int baseMax = stack.getMaxStackSize();
            long expanded = (long) baseMax * mult;
            cir.setReturnValue((int) Math.min(Integer.MAX_VALUE, expanded));
        }
    }

    @ModifyVariable(
        method = "insertItem(Lnet/minecraft/world/item/ItemStack;Lmekanism/api/Action;Lmekanism/api/AutomationType;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack modifyInsertItemStack(ItemStack stack, ItemStack originalStack, Action action, AutomationType automationType) {
        int mult = ReplicaRecipeTracker.currentMultiplier.get();
        if (action.execute() && mult > 1 && stack != null && !stack.isEmpty()) {
            ItemStack multiplied = stack.copy();
            long newCount = (long) multiplied.getCount() * mult;
            multiplied.setCount((int) Math.min(Integer.MAX_VALUE, newCount));
            return multiplied;
        }
        return stack;
    }
}