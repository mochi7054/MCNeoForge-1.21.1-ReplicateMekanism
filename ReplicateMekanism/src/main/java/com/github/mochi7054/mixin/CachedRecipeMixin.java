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
            com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug] process() head. Found tile associated: {}", tile.getClass().getName());
            if (ReplicaRecipeTracker.hasReplicaUpgrade(tile)) {
                ReplicaRecipeTracker.isReplicaActive.set(true);
                com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug]   isReplicaActive set to true.");
            } else {
                ReplicaRecipeTracker.isReplicaActive.set(false);
            }
        } else {
            // Un-comment to reduce noise if needed, but useful for initial check
            // com.github.mochi7054.ReplicateMekanism.LOGGER.info("[RMDebug] process() head. No tile associated with recipe.");
            ReplicaRecipeTracker.isReplicaActive.set(false);
        }
    }


    @Inject(method = "process", at = @At("RETURN"))
    private void onProcessReturn(CallbackInfo ci) {
        ReplicaRecipeTracker.isReplicaActive.remove();
    }
}
