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
    public static final ThreadLocal<Boolean> isReplicaActive = ThreadLocal.withInitial(() -> false);

    public static Object getTile(CachedRecipe recipe) {
        if (recipe == null) return null;
        return RECIPE_TO_TILE.get(recipe);
    }

    public static boolean hasReplicaUpgrade(Object tileObj) {
        if (tileObj == null) return false;
        ReplicateMekanism.LOGGER.info("[RMDebug] hasReplicaUpgrade check for class: {}", tileObj.getClass().getName());
        if (tileObj instanceof IUpgradeTile upgradeTile) {
            TileComponentUpgrade component = upgradeTile.getComponent();
            if (component != null) {
                boolean installed = component.isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
                ReplicateMekanism.LOGGER.info("[RMDebug]   Component found. Installed status for REPLICA: {}", installed);
                return installed;
            } else {
                ReplicateMekanism.LOGGER.info("[RMDebug]   Component is null.");
            }
        } else {
            ReplicateMekanism.LOGGER.info("[RMDebug]   tileObj is not an instance of IUpgradeTile.");
        }
        return false;
    }

}
