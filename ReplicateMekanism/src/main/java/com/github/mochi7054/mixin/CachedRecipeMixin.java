package com.github.mochi7054.mixin;

import mekanism.api.recipes.cache.CachedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = CachedRecipe.class, remap = false)
public abstract class CachedRecipeMixin {

    @Inject(method = "process", at = @At("HEAD"))
    private void onProcessHead(CallbackInfo ci) {
        Object tile = ReplicaRecipeTracker.getTile((CachedRecipe) (Object) this);
        if (tile != null) {
            int mult = ReplicaRecipeTracker.getReplicaMultiplier(tile);
            ReplicaRecipeTracker.currentMultiplier.set(mult);
        } else {
            ReplicaRecipeTracker.currentMultiplier.set(1);
        }
    }

    @Inject(method = "process", at = @At("RETURN"))
    private void onProcessReturn(CallbackInfo ci) {
        ReplicaRecipeTracker.currentMultiplier.remove();
    }
}