package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.List;

public class ReplicationFluidHandler implements IFluidHandler {
    private final TileEntityConfigurableMachine tile;
    private final List<SimpleMatterTank> tanks;
    private final Direction side;

    public ReplicationFluidHandler(TileEntityConfigurableMachine tile, List<SimpleMatterTank> tanks, Direction side) {
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

    public static Fluid getFluidFromMatter(IMatterType type) {
        if (type == null) return null;
        String name = type.getName().toLowerCase();
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("replication", "matter_" + name);
        return BuiltInRegistries.FLUID.get(rl);
    }

    public static IMatterType getMatterFromFluid(Fluid fluid) {
        if (fluid == null) return null;
        ResourceLocation rl = BuiltInRegistries.FLUID.getKey(fluid);
        if (rl != null && "replication".equals(rl.getNamespace()) && rl.getPath().startsWith("matter_")) {
            String name = rl.getPath().substring("matter_".length());
            if (com.buuz135.replication.ReplicationRegistry.MATTER_TYPES_REGISTRY != null) {
                ResourceLocation matterRl = ResourceLocation.fromNamespaceAndPath("replication", name.toLowerCase());
                return com.buuz135.replication.ReplicationRegistry.MATTER_TYPES_REGISTRY.get(matterRl);
            }
        }
        return null;
    }

    private SimpleMatterTank getTankForFluid(Fluid fluid) {
        IMatterType matterType = getMatterFromFluid(fluid);
        if (matterType == null) return null;
        String name = matterType.getName();
        for (SimpleMatterTank tank : tanks) {
            if (tank.getMatterType() != null && tank.getMatterType().getName().equalsIgnoreCase(name)) {
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
    public FluidStack getFluidInTank(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return FluidStack.EMPTY;
        SimpleMatterTank tank = tanks.get(tankIndex);
        Fluid fluid = getFluidFromMatter(tank.getMatterType());
        if (fluid == null) return FluidStack.EMPTY;
        int amount = (int) Math.round(tank.getMatterAmount() * 1000.0);
        if (amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
    }

    @Override
    public int getTankCapacity(int tankIndex) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return 0;
        return (int) Math.round(tanks.get(tankIndex).getCapacity() * 1000.0);
    }

    @Override
    public boolean isFluidValid(int tankIndex, FluidStack stack) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return false;
        if (stack == null || stack.isEmpty()) return false;
        SimpleMatterTank tank = tanks.get(tankIndex);
        IMatterType targetType = getMatterFromFluid(stack.getFluid());
        if (targetType == null) return false;
        return tank.getMatterType() != null && tank.getMatterType().getName().equalsIgnoreCase(targetType.getName());
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        if (!canInput()) return 0;
        if (stack == null || stack.isEmpty()) return 0;
        SimpleMatterTank tank = getTankForFluid(stack.getFluid());
        if (tank == null) return 0;
        
        double matterAmount = stack.getAmount() / 1000.0;
        com.buuz135.replication.api.matter_fluid.MatterStack matterStack = 
            new com.buuz135.replication.api.matter_fluid.MatterStack(tank.getMatterType(), matterAmount);
        double filledMatter = tank.fill(matterStack, action);
        return (int) Math.round(filledMatter * 1000.0);
    }

    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        if (!canOutput()) return FluidStack.EMPTY;
        if (stack == null || stack.isEmpty()) return FluidStack.EMPTY;
        SimpleMatterTank tank = getTankForFluid(stack.getFluid());
        if (tank == null) return FluidStack.EMPTY;
        
        double matterAmount = stack.getAmount() / 1000.0;
        com.buuz135.replication.api.matter_fluid.MatterStack matterStack = 
            new com.buuz135.replication.api.matter_fluid.MatterStack(tank.getMatterType(), matterAmount);
        com.buuz135.replication.api.matter_fluid.MatterStack drainedMatter = tank.drain(matterStack, action);
        if (drainedMatter == null || drainedMatter.isEmpty()) return FluidStack.EMPTY;
        
        Fluid fluid = getFluidFromMatter(tank.getMatterType());
        if (fluid == null) return FluidStack.EMPTY;
        return new FluidStack(fluid, (int) Math.round(drainedMatter.getAmount() * 1000.0));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!canOutput()) return FluidStack.EMPTY;
        if (maxDrain <= 0) return FluidStack.EMPTY;
        
        for (int i = 0; i < tanks.size(); i++) {
            SimpleMatterTank tank = tanks.get(i);
            if (tank.getMatterAmount() > 0) {
                double matterAmount = maxDrain / 1000.0;
                com.buuz135.replication.api.matter_fluid.MatterStack drainedMatter = tank.drain(matterAmount, action);
                if (drainedMatter != null && !drainedMatter.isEmpty() && drainedMatter.getAmount() > 0) {
                    Fluid fluid = getFluidFromMatter(tank.getMatterType());
                    if (fluid != null) {
                        return new FluidStack(fluid, (int) Math.round(drainedMatter.getAmount() * 1000.0));
                    }
                }
            }
        }
        return FluidStack.EMPTY;
    }
}
