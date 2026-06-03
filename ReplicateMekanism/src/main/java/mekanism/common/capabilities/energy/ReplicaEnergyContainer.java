package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tile.base.TileEntityMekanism;

public class ReplicaEnergyContainer extends BasicEnergyContainer {
    
    private final TileEntityMekanism tile;

    public ReplicaEnergyContainer(long maxEnergy, IContentsListener listener, TileEntityMekanism tile) {
        super(maxEnergy, ConstantPredicates.alwaysTrue(), BasicEnergyContainer.internalOnly, listener);
        this.tile = tile;
    }

    @Override
    public long insert(long amount, Action action, AutomationType automationType) {
        if (tile != null && tile.getComponent() != null && tile.getComponent().isUpgradeInstalled(mekanism.api.Upgrade.valueOf("REPLICA"))) {
            if (automationType == AutomationType.INTERNAL) {
                amount = amount * 2;
            }
        }
        return super.insert(amount, action, automationType);
    }
}
