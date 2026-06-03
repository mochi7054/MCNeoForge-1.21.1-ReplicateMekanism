package com.github.mochi7054.mixin;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.recipe.ReplicaRecipeTracker;

@Mixin(value = RecipeCacheLookupMonitor.class, remap = false)
public abstract class RecipeCacheLookupMonitorMixin {

    @Shadow
    @Final
    private IRecipeLookupHandler handler;

    @Inject(method = "createNewCachedRecipe", at = @At("RETURN"))
    private void onCreateNewCachedRecipe(MekanismRecipe recipe, int cacheIndex, CallbackInfoReturnable<CachedRecipe> cir) {
        CachedRecipe cachedRecipe = cir.getReturnValue();
        if (cachedRecipe != null && handler != null) {
            ReplicaRecipeTracker.RECIPE_TO_TILE.put(cachedRecipe, handler);
        }
    }
}
