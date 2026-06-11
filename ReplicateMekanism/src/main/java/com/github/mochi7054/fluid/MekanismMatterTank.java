package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class MekanismMatterTank implements IMatterTank {

    private final BasicFluidTank tank;
    private final Fluid matterFluid;

    public MekanismMatterTank(BasicFluidTank tank, Fluid matterFluid) {
        this.tank = tank;
        this.matterFluid = matterFluid;
    }

    @Override
    public MatterStack getMatter() {
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(matterFluid);
        FluidStack fs = tank.getFluid();
        double amount = fs.isEmpty() ? 0 : fs.getAmount();
        // Always return a typed stack (even at 0 amount) so BiPredicate type matching works in network transfer
        return new MatterStack(matterType, amount);
    }

    @Override
    public double getMatterAmount() {
        return tank.getFluidAmount();
    }

    @Override
    public double getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public boolean isMatterValid(MatterStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(matterFluid);
        if (!matterType.getName().equals(stack.getMatterType().getName())) return false;
        return tank.isFluidValid(new FluidStack(matterFluid, (int) Math.round(stack.getAmount())));
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return 0;
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(matterFluid);
        if (!matterType.getName().equals(stack.getMatterType().getName())) return 0;
        return tank.fill(new FluidStack(matterFluid, (int) Math.round(stack.getAmount())), action);
    }

    @Override
    public MatterStack drain(double amount, FluidAction action) {
        if (amount <= 0) return MatterStack.EMPTY;
        FluidStack fs = tank.getFluid();
        if (fs.isEmpty()) return MatterStack.EMPTY;
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(fs.getFluid());
        FluidStack drained = tank.drain((int) Math.round(amount), action);
        if (drained.isEmpty()) return MatterStack.EMPTY;
        return new MatterStack(matterType, drained.getAmount());
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(matterFluid);
        if (!matterType.getName().equals(stack.getMatterType().getName())) return MatterStack.EMPTY;
        FluidStack drained = tank.drain((int) Math.round(stack.getAmount()), action);
        if (drained.isEmpty()) return MatterStack.EMPTY;
        return new MatterStack(stack.getMatterType(), drained.getAmount());
    }
}
