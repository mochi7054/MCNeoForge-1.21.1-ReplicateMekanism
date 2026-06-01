package com.github.mochi7054;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;

public class ReplicaRecipeTracker {
    public static final Map<CachedRecipe, Object> RECIPE_TO_TILE = Collections.synchronizedMap(new WeakHashMap<>());
    public static final ThreadLocal<Boolean> isReplicaActive = ThreadLocal.withInitial(() -> false);

    public static Object getTile(CachedRecipe recipe) {
        if (recipe == null) return null;
        return RECIPE_TO_TILE.get(recipe);
    }

    public static boolean hasReplicaUpgrade(Object tileObj) {
        if (tileObj instanceof IUpgradeTile upgradeTile) {
            TileComponentUpgrade component = upgradeTile.getComponent();
            if (component != null) {
                return component.isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
            }
        }
        return false;
    }
}
