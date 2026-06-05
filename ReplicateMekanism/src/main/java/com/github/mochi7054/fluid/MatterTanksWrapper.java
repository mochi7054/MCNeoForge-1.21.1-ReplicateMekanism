package com.github.mochi7054.fluid;

import com.github.mochi7054.ReplicateMekanism;
import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.matter_fluid.component.MatterTankComponent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;

public class MatterTanksWrapper implements IMatterHandler {
    private final List<IMatterTank> tanks;

    public MatterTanksWrapper(List<IMatterTank> tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return this.tanks.size();
    }

    @Override
    public double getTankCapacity(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= this.tanks.size()) return 0;
        return this.tanks.get(tankIndex).getCapacity();
    }

    @Override
    public MatterStack getMatterInTank(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= this.tanks.size()) return MatterStack.EMPTY;
        return this.tanks.get(tankIndex).getMatter();
    }

    @Override
    public boolean isMatterValid(int tankIndex, MatterStack stack) {
        if (tankIndex < 0 || tankIndex >= this.tanks.size()) return false;
        return this.tanks.get(tankIndex).isMatterValid(stack);
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return 0;
        double filled = 0;
        MatterStack copy = stack.copy();
        for (int i = 0; i < this.tanks.size(); i++) {
            IMatterTank tank = this.tanks.get(i);
            if (tank.isMatterValid(copy)) {
                double added;
                if (tank instanceof MatterTankComponent component) {
                    added = component.fillForced(copy, action);
                } else {
                    added = tank.fill(copy, action);
                }
                filled += added;
                copy.setAmount(copy.getAmount() - added);
                if (copy.isEmpty()) break;
            }
        }
        return filled;
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        MatterStack totalDrained = MatterStack.EMPTY;
        MatterStack target = stack.copy();
        for (int i = 0; i < this.tanks.size(); i++) {
            IMatterTank tank = this.tanks.get(i);
            MatterStack drained;
            if (tank instanceof MatterTankComponent component) {
                drained = component.drainForced(target, action);
            } else {
                drained = tank.drain(target, action);
            }
            
            if (drained != null && !drained.isEmpty()) {
                if (totalDrained.isEmpty()) {
                    totalDrained = drained.copy();
                } else {
                    totalDrained.setAmount(totalDrained.getAmount() + drained.getAmount());
                }
                target.setAmount(target.getAmount() - drained.getAmount());
                if (target.isEmpty()) break;
            }
        }
        return totalDrained;
    }

    @Override
    public MatterStack drain(double amount, FluidAction action) {
        if (amount <= 0) return MatterStack.EMPTY;
        MatterStack totalDrained = MatterStack.EMPTY;
        double targetAmount = amount;
        for (int i = 0; i < this.tanks.size(); i++) {
            IMatterTank tank = this.tanks.get(i);
            MatterStack inTank = tank.getMatter();
            
            if (inTank == null || inTank.isEmpty()) {
                continue;
            }
            
            if (!totalDrained.isEmpty() && !totalDrained.getMatterType().equals(inTank.getMatterType())) {
                continue;
            }
            
            MatterStack toDrain = new MatterStack(inTank.getMatterType(), targetAmount);
            MatterStack drained;
            if (tank instanceof MatterTankComponent component) {
                drained = component.drainForced(toDrain, action);
            } else {
                drained = tank.drain(toDrain, action);
            }
            
            if (drained != null && !drained.isEmpty()) {
                if (totalDrained.isEmpty()) {
                    totalDrained = drained.copy();
                } else {
                    totalDrained.setAmount(totalDrained.getAmount() + drained.getAmount());
                }
                targetAmount -= drained.getAmount();
                if (targetAmount <= 0) break;
            }
        }
        return totalDrained;
    }
}
