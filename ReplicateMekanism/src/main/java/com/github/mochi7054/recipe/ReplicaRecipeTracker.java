package com.github.mochi7054.recipe;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import com.github.mochi7054.ReplicateMekanism;

public class ReplicaRecipeTracker {
    public static final Map<CachedRecipe, Object> RECIPE_TO_TILE = Collections.synchronizedMap(new WeakHashMap<>());
    public static final ThreadLocal<Integer> currentMultiplier = ThreadLocal.withInitial(() -> 1);

    public static Object getTile(CachedRecipe recipe) {
        if (recipe == null) return null;
        return RECIPE_TO_TILE.get(recipe);
    }

    public static int getReplicaMultiplier(Object tileObj) {
        if (tileObj instanceof IUpgradeTile upgradeTile) {
            TileComponentUpgrade component = upgradeTile.getComponent();
            if (component != null && component.isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
                int installed = component.getUpgrades(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
                if (installed > 0) {
                    return 1 << Math.min(installed, 30); // 2^installed (2, 4, 8, 16, 32, 64, 128, 256)
                }
            }
        }
        return 1;
    }

    public static boolean hasReplicaUpgrade(Object tileObj) {
        return getReplicaMultiplier(tileObj) > 1;
    }
}