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
        int tanks = matterHandler.getTanks();
        ReplicateMekanism.LOGGER.info("[RMWrapper] getTanks() called, returning: {}", tanks);
        return tanks;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        MatterStack matter = matterHandler.getMatterInTank(tank);
        if (matter == null || matter.isEmpty()) {
            ReplicateMekanism.LOGGER.info("[RMWrapper] getFluidInTank(tank: {}) is empty", tank);
            return FluidStack.EMPTY;
        }
        Fluid fluid = getFluidFromMatterType(matter.getMatterType());
        if (fluid == Fluids.EMPTY) {
            ReplicateMekanism.LOGGER.info("[RMWrapper] getFluidInTank(tank: {}) has matter {} but mapped fluid is EMPTY", tank, matter.getMatterType().getName());
            return FluidStack.EMPTY;
        }
        ReplicateMekanism.LOGGER.info("[RMWrapper] getFluidInTank(tank: {}) has {} of fluid {}", tank, matter.getAmount(), fluid);
        return new FluidStack(fluid, (int) Math.round(matter.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        int capacity = (int) Math.round(matterHandler.getTankCapacity(tank));
        ReplicateMekanism.LOGGER.info("[RMWrapper] getTankCapacity(tank: {}) is {}", tank, capacity);
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
            ReplicateMekanism.LOGGER.info("[RMWrapper] isFluidValid(tank: {}, fluid: {}) is false (empty matter mapping)", tank, stack.getFluid().toString());
            return false;
        }
        boolean valid = matterHandler.isMatterValid(tank, new MatterStack(matterType, stack.getAmount()));
        ReplicateMekanism.LOGGER.info("[RMWrapper] isFluidValid(tank: {}, fluid: {}) returns: {}", tank, stack.getFluid().toString(), valid);
        return valid;
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        ReplicateMekanism.LOGGER.info("[RMWrapper] fill called for stack {} (amount: {}), action: {}", stack.getFluid().toString(), stack.getAmount(), action);
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
            ReplicateMekanism.LOGGER.info("[RMWrapper]   mapped matterType is EMPTY, returning 0");
            return 0;
        }
        double filled = matterHandler.fill(new MatterStack(matterType, stack.getAmount()), action);
        ReplicateMekanism.LOGGER.info("[RMWrapper]   filled: {}", filled);
        return (int) Math.round(filled);
    }

    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        ReplicateMekanism.LOGGER.info("[RMWrapper] drain(FluidStack) called for stack {} (amount: {}), action: {}", stack.getFluid().toString(), stack.getAmount(), action);
        IMatterType matterType = getMatterTypeFromFluid(stack.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) return FluidStack.EMPTY;
        MatterStack drained = matterHandler.drain(new MatterStack(matterType, stack.getAmount()), action);
        if (drained == null || drained.isEmpty()) return FluidStack.EMPTY;
        return new FluidStack(stack.getFluid(), (int) Math.round(drained.getAmount()));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        ReplicateMekanism.LOGGER.info("[RMWrapper] drain(maxDrain: {}) called, action: {}", maxDrain, action);
        MatterStack drained = matterHandler.drain(maxDrain, action);
        if (drained == null || drained.isEmpty()) {
            ReplicateMekanism.LOGGER.info("[RMWrapper]   drained matter is null or empty");
            return FluidStack.EMPTY;
        }
        Fluid fluid = getFluidFromMatterType(drained.getMatterType());
        if (fluid == Fluids.EMPTY) {
            ReplicateMekanism.LOGGER.info("[RMWrapper]   drained fluid mapping is EMPTY for matter {}", drained.getMatterType().getName());
            return FluidStack.EMPTY;
        }
        ReplicateMekanism.LOGGER.info("[RMWrapper]   successfully drained: {} of fluid {}", drained.getAmount(), fluid);
        return new FluidStack(fluid, (int) Math.round(drained.getAmount()));
    }
}
