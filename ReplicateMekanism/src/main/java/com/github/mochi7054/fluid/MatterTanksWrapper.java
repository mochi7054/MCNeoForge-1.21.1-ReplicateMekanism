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
        ReplicateMekanism.LOGGER.info("[RMTanksWrapper] drain(MatterStack: {}, action: {}) called for {} tanks", stack.getAmount(), action, this.tanks.size());
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        MatterStack totalDrained = MatterStack.EMPTY;
        MatterStack target = stack.copy();
        for (int i = 0; i < this.tanks.size(); i++) {
            IMatterTank tank = this.tanks.get(i);
            ReplicateMekanism.LOGGER.info("[RMTanksWrapper]   Tank {} class: {}, current matter: {}, capacity: {}", 
                    i, tank.getClass().getName(), 
                    tank.getMatter() != null ? tank.getMatter().getAmount() : 0,
                    tank.getCapacity());
            
            MatterStack drained;
            if (tank instanceof MatterTankComponent component) {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank is MatterTankComponent. Action: {}", component.getTankAction());
                drained = component.drainForced(target, action);
            } else {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank is NOT MatterTankComponent, calling normal drain.");
                drained = tank.drain(target, action);
            }
            
            ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     drained result: {}", drained != null ? drained.getAmount() : "null");
            
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
        ReplicateMekanism.LOGGER.info("[RMTanksWrapper] drain(amount: {}, action: {}) called for {} tanks", amount, action, this.tanks.size());
        if (amount <= 0) return MatterStack.EMPTY;
        MatterStack totalDrained = MatterStack.EMPTY;
        double targetAmount = amount;
        for (int i = 0; i < this.tanks.size(); i++) {
            IMatterTank tank = this.tanks.get(i);
            MatterStack inTank = tank.getMatter();
            ReplicateMekanism.LOGGER.info("[RMTanksWrapper]   Tank {} class: {}, current matter: {}, capacity: {}", 
                    i, tank.getClass().getName(), 
                    inTank != null ? inTank.getAmount() : 0,
                    tank.getCapacity());
            
            if (inTank == null || inTank.isEmpty()) {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank is empty, skipping.");
                continue;
            }
            
            if (!totalDrained.isEmpty() && !totalDrained.getMatterType().equals(inTank.getMatterType())) {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank matter type {} does not match already drained type {}, skipping.", 
                        inTank.getMatterType().getName(), totalDrained.getMatterType().getName());
                continue;
            }
            
            MatterStack toDrain = new MatterStack(inTank.getMatterType(), targetAmount);
            MatterStack drained;
            if (tank instanceof MatterTankComponent component) {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank is MatterTankComponent. Action: {}", component.getTankAction());
                drained = component.drainForced(toDrain, action);
            } else {
                ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     Tank is NOT MatterTankComponent, calling normal drain.");
                drained = tank.drain(toDrain, action);
            }
            
            ReplicateMekanism.LOGGER.info("[RMTanksWrapper]     drained result: {}", drained != null ? drained.getAmount() : "null");
            
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
