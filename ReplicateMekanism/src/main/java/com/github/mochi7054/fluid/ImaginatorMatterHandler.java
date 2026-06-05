package com.github.mochi7054.fluid;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.entity.ImaginatorBlockEntity;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public class ImaginatorMatterHandler implements IMatterHandler {
    private final ImaginatorBlockEntity tile;
    private final List<BasicFluidTank> tanks;

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

    private BasicFluidTank getTankForMatter(IMatterType matterType) {
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

    private Fluid getFluidForMatter(IMatterType matterType) {
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
        BasicFluidTank tank = tanks.get(tankIndex);
        FluidStack fs = tank.getFluid();
        if (fs.isEmpty()) return MatterStack.EMPTY;
        IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(fs.getFluid());
        if (matterType == com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
            return MatterStack.EMPTY;
        }
        return new MatterStack(matterType, fs.getAmount());
    }

    @Override
    public boolean isMatterValid(int tankIndex, MatterStack stack) {
        if (tankIndex < 0 || tankIndex >= tanks.size()) return false;
        if (stack == null || stack.isEmpty()) return false;
        BasicFluidTank tank = tanks.get(tankIndex);
        Fluid fluid = getFluidForMatter(stack.getMatterType());
        if (fluid == Fluids.EMPTY) return false;
        return tank.isFluidValid(new FluidStack(fluid, (int) Math.round(stack.getAmount())));
    }

    @Override
    public double fill(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return 0;
        BasicFluidTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return 0;
        Fluid fluid = getFluidForMatter(stack.getMatterType());
        if (fluid == Fluids.EMPTY) return 0;
        
        int filled = tank.fill(new FluidStack(fluid, (int) Math.round(stack.getAmount())), action);
        return filled;
    }

    @Override
    public MatterStack drain(MatterStack stack, FluidAction action) {
        if (stack == null || stack.isEmpty()) return MatterStack.EMPTY;
        BasicFluidTank tank = getTankForMatter(stack.getMatterType());
        if (tank == null) return MatterStack.EMPTY;
        Fluid fluid = getFluidForMatter(stack.getMatterType());
        if (fluid == Fluids.EMPTY) return MatterStack.EMPTY;
        
        FluidStack drained = tank.drain(new FluidStack(fluid, (int) Math.round(stack.getAmount())), action);
        if (drained.isEmpty()) return MatterStack.EMPTY;
        return new MatterStack(stack.getMatterType(), drained.getAmount());
    }

    @Override
    public MatterStack drain(double amount, FluidAction action) {
        if (amount <= 0) return MatterStack.EMPTY;
        
        for (int i = 0; i < tanks.size(); i++) {
            BasicFluidTank tank = tanks.get(i);
            FluidStack fs = tank.getFluid();
            if (!fs.isEmpty()) {
                IMatterType matterType = MatterFluidWrapper.getMatterTypeFromFluid(fs.getFluid());
                if (matterType != com.buuz135.replication.ReplicationRegistry.Matter.EMPTY.get()) {
                    FluidStack drained = tank.drain((int) Math.round(amount), action);
                    if (!drained.isEmpty()) {
                        return new MatterStack(matterType, drained.getAmount());
                    }
                }
            }
        }
        return MatterStack.EMPTY;
    }
}
