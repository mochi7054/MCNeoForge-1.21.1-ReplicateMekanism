package com.github.mochi7054.fluid;

import com.github.mochi7054.ReplicateMekanism;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class MatterFluidWrapper implements IFluidHandler {
    private final IMatterHandler matterHandler;

    public MatterFluidWrapper(IMatterHandler matterHandler) {
        this.matterHandler = matterHandler;
    }

    public static IMatterType getMatterTypeFromFluid(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get();
        if (fluid == ReplicateMekanism.EARTH_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.EARTH.get();
        if (fluid == ReplicateMekanism.NETHER_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.NETHER.get();
        if (fluid == ReplicateMekanism.ORGANIC_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.ORGANIC.get();
        if (fluid == ReplicateMekanism.ENDER_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.ENDER.get();
        if (fluid == ReplicateMekanism.METALLIC_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.METALLIC.get();
        if (fluid == ReplicateMekanism.PRECIOUS_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.PRECIOUS.get();
        if (fluid == ReplicateMekanism.LIVING_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.LIVING.get();
        if (fluid == ReplicateMekanism.QUANTUM_MATTER.source.get()) return com.buuz135.replication.ReplicationRegistry.Matter.QUANTUM.get();
        return com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get();
    }

    public static Fluid getFluidFromMatterType(IMatterType matterType) {
        if (matterType == null) return Fluids.EMPTY;
        String name = matterType.getName();
        if (name == null) return Fluids.EMPTY;
        if (name.equalsIgnoreCase("earth")) return ReplicateMekanism.EARTH_MATTER.source.get();
        if (name.equalsIgnoreCase("nether")) return ReplicateMekanism.NETHER_MATTER.source.get();
        if (name.equalsIgnoreCase("organic")) return ReplicateMekanism.ORGANIC_MATTER.source.get();
        if (name.equalsIgnoreCase("ender")) return ReplicateMekanism.ENDER_MATTER.source.get();
        if (name.equalsIgnoreCase("metallic")) return ReplicateMekanism.METALLIC_MATTER.source.get();
        if (name.equalsIgnoreCase("precious")) return ReplicateMekanism.PRECIOUS_MATTER.source.get();
        if (name.equalsIgnoreCase("living")) return ReplicateMekanism.LIVING_MATTER.source.get();
        if (name.equalsIgnoreCase("quantum")) return ReplicateMekanism.QUANTUM_MATTER.source.get();
        return Fluids.EMPTY;
    }

    @Override
    public int getTanks() {
        return matterHandler.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        MatterStack matter = matterHandler.getMatterInTank(tank);
        if (matter == null || matter.isEmpty()) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = getFluidFromMatterType(matter.getMatterType());
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, (int) Math.round(matter.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) Math.round(matterHandler.getTankCapacity(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
            return false;
        }
        return matterHandler.isMatterValid(tank, new MatterStack(matterType, stack.getAmount()));
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
            return 0;
        }
        double filled = matterHandler.fill(new MatterStack(matterType, stack.getAmount()), action);
        return (int) Math.round(filled);
    }

    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) return FluidStack.EMPTY;
        MatterStack drained = matterHandler.drain(new MatterStack(matterType, stack.getAmount()), action);
        if (drained == null || drained.isEmpty()) return FluidStack.EMPTY;
        return new FluidStack(stack.getFluid(), (int) Math.round(drained.getAmount()));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        MatterStack drained = matterHandler.drain(maxDrain, action);
        if (drained == null || drained.isEmpty()) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = getFluidFromMatterType(drained.getMatterType());
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, (int) Math.round(drained.getAmount()));
    }
}
