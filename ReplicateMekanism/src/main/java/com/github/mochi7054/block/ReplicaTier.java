package com.github.mochi7054.block;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;

public enum ReplicaTier implements ITier {
    STANDARD("standard", null, 1, 10000, 100000L),
    BASIC("basic", BaseTier.BASIC, 3, 20000, 200000L),
    ADVANCED("advanced", BaseTier.ADVANCED, 5, 40000, 400000L),
    ELITE("elite", BaseTier.ELITE, 7, 80000, 800000L),
    ULTIMATE("ultimate", BaseTier.ULTIMATE, 9, 160000, 1600000L);

    private final String name;
    private final BaseTier baseTier;
    private final int slotCount;
    private final int tankCapacity;
    private final long energyCapacity;

    ReplicaTier(String name, BaseTier baseTier, int slotCount, int tankCapacity, long energyCapacity) {
        this.name = name;
        this.baseTier = baseTier;
        this.slotCount = slotCount;
        this.tankCapacity = tankCapacity;
        this.energyCapacity = energyCapacity;
    }

    public String getName() {
        return name;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }
}
