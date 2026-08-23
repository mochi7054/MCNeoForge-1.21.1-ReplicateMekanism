package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class SimpleMatterTank implements IMatterTank {
    private final IMatterType matterType;
    private double amount;
    private final double capacity;
    private final java.lang.Runnable onChange;

    public SimpleMatterTank(IMatterType matterType, double capacity, java.lang.Runnable onChange) {
        this.matterType = matterType;
        this.capacity = capacity;
        this.amount = 0;
        this.onChange = onChange;
    }

    public void setAmount(double amount) {
        this.amount = Math.max(0, Math.min(amount, capacity));
        if (onChange != null) onChange.run();
    }

    @Override
    public MatterStack getMatter() {
        return new MatterStack(matterType, amount);
    }

    @Override
    public double getMatterAmount() {
        return amount;
    }

    @Override
    public double getCapacity() {
        return capacity;
    }

    @Override
    public boolean isMatterValid(MatterStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return matterType.getName().equals(stack.getMatterType().getName());
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (!isMatterValid(stack)) return 0;
        double toAdd = Math.min(stack.getAmount(), capacity - amount);
        if (action.execute()) {
            amount += toAdd;
            if (onChange != null) onChange.run();
        }
        return toAdd;
    }

    @Override
    public MatterStack drain(double toDrainAmount, FluidAction action) {
        double toDrain = Math.min(toDrainAmount, amount);
        if (action.execute()) {
            amount -= toDrain;
            if (onChange != null) onChange.run();
        }
        return new MatterStack(matterType, toDrain);
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (!isMatterValid(stack)) return MatterStack.EMPTY;
        return drain(stack.getAmount(), action);
    }
}
