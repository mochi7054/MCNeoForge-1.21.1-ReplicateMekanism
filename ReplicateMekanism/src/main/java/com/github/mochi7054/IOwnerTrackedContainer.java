package com.github.mochi7054;

import mekanism.common.tile.base.TileEntityMekanism;

public interface IOwnerTrackedContainer {
    TileEntityMekanism getReplicateMekanism$owner();
    void setReplicateMekanism$owner(TileEntityMekanism owner);
}
