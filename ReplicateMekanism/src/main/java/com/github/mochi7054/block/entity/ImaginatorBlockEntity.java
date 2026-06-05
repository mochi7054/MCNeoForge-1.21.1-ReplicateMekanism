package com.github.mochi7054.block.entity;

import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.fluid.MatterFluidType;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.MatterValue;
import com.buuz135.replication.calculation.ReplicationCalculation;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImaginatorBlockEntity extends TileEntityConfigurableMachine implements MenuProvider {

    public static final int BASE_TICKS_REQUIRED = 100;
    public static final long BASE_ENERGY_USAGE = 50L;

    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    private MachineEnergyContainer<ImaginatorBlockEntity> energyContainer;
    
    public BasicFluidTank earthTank;
    public BasicFluidTank netherTank;
    public BasicFluidTank organicTank;
    public BasicFluidTank enderTank;
    public BasicFluidTank metallicTank;
    public BasicFluidTank preciousTank;
    public BasicFluidTank livingTank;
    public BasicFluidTank quantumTank;

    private List<BasicFluidTank> getMatterTanks() {
        return List.of(earthTank, netherTank, organicTank, enderTank, metallicTank, preciousTank, livingTank, quantumTank);
    }

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public ImaginatorBlockEntity(BlockPos pos, BlockState state) {
        super(ReplicateMekanism.IMAGINATOR, pos, state);
        configComponent.setupItemIOConfig(null, outputSlot, energySlot);
        
        // Setup fluid config with all 8 tanks
        mekanism.common.tile.component.config.ConfigInfo fluidConfig = configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID);
        if (fluidConfig != null) {
            fluidConfig.addSlotInfo(mekanism.common.tile.component.config.DataType.INPUT, 
                mekanism.common.tile.component.TileComponentConfig.createInfo(
                    mekanism.common.lib.transmitter.TransmissionType.FLUID, true, false,
                    earthTank, netherTank, organicTank, enderTank, metallicTank, preciousTank, livingTank, quantumTank
                )
            );
            fluidConfig.addSlotInfo(mekanism.common.tile.component.config.DataType.OUTPUT, 
                mekanism.common.tile.component.TileComponentConfig.createInfo(
                    mekanism.common.lib.transmitter.TransmissionType.FLUID, false, true,
                    earthTank, netherTank, organicTank, enderTank, metallicTank, preciousTank, livingTank, quantumTank
                )
            );
            fluidConfig.setCanEject(false);
        }

        configComponent.setupInputConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY, energyContainer);
        mekanism.common.tile.component.config.ConfigInfo energyConfig = configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY);
        if (energyConfig != null) {
            for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
                energyConfig.setDataType(mekanism.common.tile.component.config.DataType.INPUT, side);
            }
        }

        ejectorComponent = new mekanism.common.tile.component.TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, mekanism.common.lib.transmitter.TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        energyContainer = MachineEnergyContainer.input(this, listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        earthTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.EARTH_MATTER.source.get(), listener);
        netherTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.NETHER_MATTER.source.get(), listener);
        organicTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.ORGANIC_MATTER.source.get(), listener);
        enderTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.ENDER_MATTER.source.get(), listener);
        metallicTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.METALLIC_MATTER.source.get(), listener);
        preciousTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.PRECIOUS_MATTER.source.get(), listener);
        livingTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.LIVING_MATTER.source.get(), listener);
        quantumTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.QUANTUM_MATTER.source.get(), listener);

        builder.addTank(earthTank);
        builder.addTank(netherTank);
        builder.addTank(organicTank);
        builder.addTank(enderTank);
        builder.addTank(metallicTank);
        builder.addTank(preciousTank);
        builder.addTank(livingTank);
        builder.addTank(quantumTank);

        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        inputSlot = InputInventorySlot.at(stack -> {
            MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
            return compound != null && !compound.getValues().isEmpty();
        }, listener, 75, 35);
        outputSlot = OutputInventorySlot.at(listener, 122, 35);
        energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 35);

        builder.addSlot(inputSlot);
        builder.addSlot(outputSlot);
        builder.addSlot(energySlot);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();

        if (energySlot != null) {
            energySlot.fillContainerOrConvert();
        }

        ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        long energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);

        boolean canOperate = false;
        ItemStack inputStack = inputSlot.getStack();
        MatterCompound recipeCompound = null;

        if (canFunction() && !inputStack.isEmpty()) {
            recipeCompound = ReplicationCalculation.getMatterCompound(inputStack);

            if (recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
                boolean allFluidsAvailable = true;
                for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                    IMatterType neededMatterType = entry.getKey();
                    int neededAmount = (int) Math.ceil(entry.getValue().getAmount());
                    
                    BasicFluidTank matchingTank = null;
                    for (BasicFluidTank tank : getMatterTanks()) {
                        FluidStack storedFluid = tank.getFluid();
                        if (!storedFluid.isEmpty() && storedFluid.getFluid().getFluidType() instanceof MatterFluidType matterFluidType) {
                            IMatterType storedMatterType = matterFluidType.getMatterType();
                            if (storedMatterType != null && storedMatterType.getName().equals(neededMatterType.getName())) {
                                matchingTank = tank;
                                break;
                            }
                        }
                    }
                    
                    if (matchingTank == null || matchingTank.getFluid().getAmount() < neededAmount) {
                        allFluidsAvailable = false;
                        break;
                    }
                }

                if (allFluidsAvailable) {
                    ItemStack outputStack = outputSlot.getStack();
                    ItemStack copyStack = inputStack.copy();
                    copyStack.setCount(1);
                    boolean outputCompatible = outputStack.isEmpty() || (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize());

                    if (outputCompatible) {
                        if (energyContainer.getEnergy() >= energyUsage) {
                            canOperate = true;
                        }
                    }
                }
            }
        }

        boolean wasActive = getActive();
        if (canOperate && recipeCompound != null) {
            setActive(true);
            energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.INTERNAL);
            operatingTicks++;
            if (operatingTicks >= ticksRequired) {
                operatingTicks = 0;
                for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                    IMatterType neededMatterType = entry.getKey();
                    int neededAmount = (int) Math.ceil(entry.getValue().getAmount());
                    
                    for (BasicFluidTank tank : getMatterTanks()) {
                        FluidStack storedFluid = tank.getFluid();
                        if (!storedFluid.isEmpty() && storedFluid.getFluid().getFluidType() instanceof MatterFluidType matterFluidType) {
                            IMatterType storedMatterType = matterFluidType.getMatterType();
                            if (storedMatterType != null && storedMatterType.getName().equals(neededMatterType.getName())) {
                                tank.extract(neededAmount, Action.EXECUTE, AutomationType.INTERNAL);
                                break;
                            }
                        }
                    }
                }

                int outputCount = 1;
                if (getComponent() != null && getComponent().isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
                    outputCount = 2;
                }

                ItemStack outputStack = outputSlot.getStack();
                if (outputStack.isEmpty()) {
                    ItemStack newOutput = inputStack.copy();
                    newOutput.setCount(outputCount);
                    outputSlot.setStack(newOutput);
                } else {
                    outputSlot.growStack(outputCount, Action.EXECUTE);
                }
                sendUpdate = true;
            }
        } else {
            setActive(false);
            if (operatingTicks > 0) {
                operatingTicks = Math.max(0, operatingTicks - 2);
                sendUpdate = true;
            }
        }

        if (wasActive != getActive()) {
            sendUpdate = true;
        }

        return sendUpdate;
    }

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    public MachineEnergyContainer<ImaginatorBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.replicatemekanism.imaginator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.github.mochi7054.inventory.container.ImaginatorMenu(containerId, playerInventory, this);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> operatingTicks, value -> operatingTicks = value));
        container.track(SyncableInt.create(() -> ticksRequired, value -> ticksRequired = value));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.operatingTicks = tag.getInt("operatingTicks");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("operatingTicks", this.operatingTicks);
    }

    public InputInventorySlot getInputSlot() {
        return inputSlot;
    }
}
