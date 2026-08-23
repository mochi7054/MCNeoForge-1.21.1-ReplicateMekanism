package com.github.mochi7054.imaginator;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.github.mochi7054.fluid.SimpleMatterTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public class ImaginatorMatterHandler implements IMatterHandler {
    private final ImaginatorBlockEntity tile;
    private final List<SimpleMatterTank> tanks;

    public ImaginatorMatterHandler(ImaginatorBlockEntity tile) {
        this.tile = tile;
        this.tanks = List.of(
            tile.earthTank,
            tile.netherTank,
            tile.organicTank,
            tile.enderTank,
            tile.metallicTank,
            tile.preciousTank,
            tile.livingTank,
            tile.quantumTank
        );
    }

    private SimpleMatterTank getTankForMatter(IMatterType matterType) {
        if (matterType == null) return null;
        String name = matterType.getName();
        if (name == null) return null;
        if (name.equalsIgnoreCase("earth")) return tile.earthTank;
        if (name.equalsIgnoreCase("nether")) return tile.netherTank;
        if (name.equalsIgnoreCase("organic")) return tile.organicTank;
        if (name.equalsIgnoreCase("ender")) return tile.enderTank;
        if (name.equalsIgnoreCase("metallic")) return tile.metallicTank;
        if (name.equalsIgnoreCase("precious")) return tile.preciousTank;
        if (name.equalsIgnoreCase("living")) return tile.livingTank;
        if (name.equalsIgnoreCase("quantum")) return tile.quantumTank;
        return null;
    }

    @Override
    public int getTanks() {
        return tanks.size();
    }

    @Override
    public double getTankCapacity(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return 0;
        return tanks.get(tankIndex).getCapacity();
    }

    @Override
    public MatterStack getMatterInTank(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return MatterStack.EMPTY;
        return tanks.get(tankIndex).getMatter();
    }

    @Override
    public boolean isMatterValid(int tankIndex, MatterStack stack) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return false;
        if (stack == null || stack.isEmpty()) return false;
        return tanks.get(tankIndex).isMatterValid(stack);
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return 0;
        SimpleMatterTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return 0;
        return tank.fill(stack, action);
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        SimpleMatterTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return MatterStack.EMPTY;
        return tank.drain(stack, action);
    }

    @Override
    public MatterStack drain(double amount, FluidAction action) {
        if (amount <= 0) return MatterStack.EMPTY;
        
        for (int i = 0; i < tanks.size(); i++) {
            SimpleMatterTank tank = tanks.get(i);
            MatterStack stored = tank.getMatter();
            if (stored != null && !stored.isEmpty() && stored.getAmount() > 0) {
                MatterStack drained = tank.drain(amount, action);
                if (drained != null && !drained.isEmpty()) {
                    return drained;
                }
            }
        }
        return MatterStack.EMPTY;
    }
}
