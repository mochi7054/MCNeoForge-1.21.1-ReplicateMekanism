package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public class ReplicationMatterHandler implements IMatterHandler {
    private final TileEntityConfigurableMachine tile;
    private final List<SimpleMatterTank> tanks;
    private final Direction side;

    public ReplicationMatterHandler(TileEntityConfigurableMachine tile, List<SimpleMatterTank> tanks, Direction side) {
        this.tile = tile;
        this.tanks = tanks;
        this.side = side;
    }

    private boolean canInput() {
        if (side == null) return true;
        if (tile.getConfig() == null) return true;
        var config = tile.getConfig().getConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID);
        if (config == null) return true;
        var dataType = config.getDataType(mekanism.api.RelativeSide.fromDirections(tile.getDirection(), side));
        return dataType == mekanism.common.tile.component.config.DataType.INPUT || dataType == mekanism.common.tile.component.config.DataType.INPUT_OUTPUT;
    }

    private boolean canOutput() {
        if (side == null) return true;
        if (tile.getConfig() == null) return true;
        var config = tile.getConfig().getConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID);
        if (config == null) return true;
        var dataType = config.getDataType(mekanism.api.RelativeSide.fromDirections(tile.getDirection(), side));
        return dataType == mekanism.common.tile.component.config.DataType.OUTPUT || dataType == mekanism.common.tile.component.config.DataType.INPUT_OUTPUT;
    }

    private SimpleMatterTank getTankForMatter(IMatterType matterType) {
        if (matterType == null) return null;
        String name = matterType.getName();
        if (name == null) return null;
        for (SimpleMatterTank tank : tanks) {
            if (tank.getMatter().getMatterType().getName().equalsIgnoreCase(name)) {
                return tank;
            }
        }
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
        if (!canInput()) return 0;
        if (stack == null || stack.isEmpty()) return 0;
        SimpleMatterTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return 0;
        return tank.fill(stack, action);
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (!canOutput()) return MatterStack.EMPTY;
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        SimpleMatterTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return MatterStack.EMPTY;
        return tank.drain(stack, action);
    }

    @Override
    public MatterStack drain(double amount, FluidAction action) {
        if (!canOutput()) return MatterStack.EMPTY;
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
