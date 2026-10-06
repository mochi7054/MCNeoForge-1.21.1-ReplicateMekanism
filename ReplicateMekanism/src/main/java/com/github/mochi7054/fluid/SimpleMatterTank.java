package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class SimpleMatterTank implements IMatterTank {
    private final IMatterType fixedMatterType;
    private IMatterType matterType;
    private double amount;
    private final double capacity;
    private final java.lang.Runnable onChange;

    public SimpleMatterTank(IMatterType matterType, double capacity, java.lang.Runnable onChange) {
        this.fixedMatterType = matterType;
        this.matterType = matterType;
        this.capacity = capacity;
        this.amount = 0;
        this.onChange = onChange;
    }

    public SimpleMatterTank(double capacity, java.lang.Runnable onChange) {
        this(null, capacity, onChange);
    }

    public IMatterType getMatterType() {
        return matterType;
    }

    public void setMatterType(IMatterType matterType) {
        this.matterType = matterType;
        if (onChange != null) onChange.run();
    }

    public void setAmount(double amount) {
        this.amount = Math.max(0, Math.min(amount, capacity));
        if (this.amount <= 0.00001) {
            this.amount = 0;
            if (this.fixedMatterType == null) {
                this.matterType = null;
            }
        }
        if (onChange != null) onChange.run();
    }

    @Override
    public MatterStack getMatter() {
        if (matterType == null || amount <= 0) return MatterStack.EMPTY;
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
        if (fixedMatterType != null) {
            return fixedMatterType.getName().equalsIgnoreCase(stack.getMatterType().getName());
        }
        if (matterType == null || amount <= 0.00001) return true;
        return matterType.getName().equalsIgnoreCase(stack.getMatterType().getName());
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return 0;
        if (fixedMatterType == null && (matterType == null || amount <= 0.00001)) {
            double toAdd = Math.min(stack.getAmount(), capacity - amount);
            if (action.execute()) {
                this.matterType = stack.getMatterType();
                this.amount += toAdd;
                if (onChange != null) onChange.run();
            }
            return toAdd;
        }
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
        if (matterType == null || amount <= 0) return MatterStack.EMPTY;
        double toDrain = Math.min(toDrainAmount, amount);
        if (action.execute()) {
            amount -= toDrain;
            if (amount <= 0.00001) {
                amount = 0;
                if (fixedMatterType == null) {
                    matterType = null;
                }
            }
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
