package com.github.mochi7054.block.entity;

import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.ReplicaTier;
import com.github.mochi7054.block.CollapserBlock;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.MatterValue;
import com.buuz135.replication.calculation.ReplicationCalculation;
import com.github.mochi7054.fluid.MatterFluidWrapper;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class CollapserBlockEntity extends TileEntityConfigurableMachine implements MenuProvider {

    public static final int BASE_TICKS_REQUIRED = 100;
    public static final long BASE_ENERGY_USAGE = 50L;

    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    private MachineEnergyContainer<CollapserBlockEntity> energyContainer;

    // Replication Network elements
    private com.buuz135.replication.network.DefaultMatterNetworkElement networkElement = null;
    private com.buuz135.replication.network.MatterNetwork currentNetwork = null;

    // 8種類のマタータンク (出力)
    public BasicFluidTank earthTank;
    public BasicFluidTank netherTank;
    public BasicFluidTank organicTank;
    public BasicFluidTank enderTank;
    public BasicFluidTank metallicTank;
    public BasicFluidTank preciousTank;
    public BasicFluidTank livingTank;
    public BasicFluidTank quantumTank;

    private List<BasicFluidTank> getMatterTanks() {
        return List.of(earthTank, netherTank, organicTank, enderTank,
                metallicTank, preciousTank, livingTank, quantumTank);
    }

    public List<InputInventorySlot> inputSlots;
    private EnergyInventorySlot energySlot;

    public ReplicaTier getTier() {
        if (getBlockState().getBlock() instanceof CollapserBlock collapserBlock) {
            return collapserBlock.getTier();
        }
        return ReplicaTier.STANDARD;
    }

    private ReplicaTier getTierSafe() {
        try {
            BlockState state = getBlockState();
            if (state != null && state.getBlock() instanceof CollapserBlock collapserBlock) {
                return collapserBlock.getTier();
            }
        } catch (Exception e) {
            // Ignore
        }
        return ReplicaTier.STANDARD;
    }

    public CollapserBlockEntity(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);
        
        // ITEM config: input only, no output
        configComponent.setupItemIOConfig(
            new ArrayList<>(inputSlots),
            Collections.emptyList(),
            energySlot,
            false
        );

        // 流体設定: タンクは出力のみ
        mekanism.common.tile.component.config.ConfigInfo fluidConfig =
                configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID);
        if (fluidConfig != null) {
            fluidConfig.addSlotInfo(mekanism.common.tile.component.config.DataType.OUTPUT,
                    mekanism.common.tile.component.TileComponentConfig.createInfo(
                            mekanism.common.lib.transmitter.TransmissionType.FLUID, false, true,
                            earthTank, netherTank, organicTank, enderTank,
                            metallicTank, preciousTank, livingTank, quantumTank
                    )
            );
            fluidConfig.setCanEject(true);
            for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
                fluidConfig.setDataType(mekanism.common.tile.component.config.DataType.OUTPUT, side);
            }
        }

        configComponent.setupInputConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY, energyContainer);
        mekanism.common.tile.component.config.ConfigInfo energyConfig =
                configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY);
        if (energyConfig != null) {
            for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
                energyConfig.setDataType(mekanism.common.tile.component.config.DataType.INPUT, side);
            }
        }

        ejectorComponent = new mekanism.common.tile.component.TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, mekanism.common.lib.transmitter.TransmissionType.FLUID);
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
        int capacity = getTierSafe().getTankCapacity();
        earthTank    = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.EARTH_MATTER.source.get(), listener);
        netherTank   = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.NETHER_MATTER.source.get(), listener);
        organicTank  = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.ORGANIC_MATTER.source.get(), listener);
        enderTank    = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.ENDER_MATTER.source.get(), listener);
        metallicTank = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.METALLIC_MATTER.source.get(), listener);
        preciousTank = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.PRECIOUS_MATTER.source.get(), listener);
        livingTank   = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.LIVING_MATTER.source.get(), listener);
        quantumTank  = BasicFluidTank.create(capacity, (stack, automation) -> true, (stack, automation) -> automation == AutomationType.INTERNAL, stack -> stack.getFluid() == ReplicateMekanism.QUANTUM_MATTER.source.get(), listener);

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
        ReplicaTier tier = getTierSafe();
        int slotCount = tier.getSlotCount();
        
        int[][] inputCoords = new int[slotCount][2];
        int energyX;
        int energyY;
        if (tier == ReplicaTier.STANDARD) {
            inputCoords[0][0] = 16;
            inputCoords[0][1] = 35;
            energyX = 141;
            energyY = 35;
        } else {
            int startX = 88 - (18 * slotCount) / 2 + 1;
            for (int i = 0; i < slotCount; i++) {
                inputCoords[i][0] = startX + i * 18;
                inputCoords[i][1] = 29;
            }
            energyX = 153;
            energyY = 11;
        }

        if (inputSlots == null) {
            inputSlots = new java.util.ArrayList<>();
        } else {
            inputSlots.clear();
        }

        for (int i = 0; i < slotCount; i++) {
            InputInventorySlot inputSlot = InputInventorySlot.at(stack -> {
                MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
                return compound != null && !compound.getValues().isEmpty();
            }, listener, inputCoords[i][0], inputCoords[i][1]);
            inputSlots.add(inputSlot);
            builder.addSlot(inputSlot);
        }

        energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, energyX, energyY);
        builder.addSlot(energySlot);
        return builder.build();
    }

    @Nullable
    public com.buuz135.replication.network.MatterNetwork getNetwork() {
        if (this.level == null) return null;
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = this.worldPosition.relative(dir);
            net.minecraft.world.level.block.entity.BlockEntity adjacentBe = this.level.getBlockEntity(adjacent);
            if (adjacentBe instanceof com.buuz135.replication.block.tile.NetworkBlockEntity<?> networkBe) {
                com.buuz135.replication.network.MatterNetwork net = networkBe.getNetwork();
                if (net != null) {
                    return net;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();

        if (energySlot != null) {
            energySlot.fillContainerOrConvert();
        }

        com.buuz135.replication.network.MatterNetwork network = getNetwork();

        if (network != currentNetwork) {
            if (networkElement != null) {
                try {
                    networkElement.leaveNetwork();
                } catch (Exception e) {
                    // Ignore
                }
                networkElement = null;
            }
            currentNetwork = network;
            if (currentNetwork != null) {
                networkElement = new com.buuz135.replication.network.DefaultMatterNetworkElement(level, worldPosition);
                networkElement.joinNetwork(currentNetwork);
            }
        }

        ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        long energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);

        boolean canOperate = false;
        int activeSlotIndex = -1;
        MatterCompound recipeCompound = null;

        if (canFunction()) {
            for (int i = 0; i < inputSlots.size(); i++) {
                ItemStack inputStack = inputSlots.get(i).getStack();
                if (!inputStack.isEmpty()) {
                    MatterCompound compound = ReplicationCalculation.getMatterCompound(inputStack);

                    if (compound != null && !compound.getValues().isEmpty()) {
                        boolean allTanksHaveSpace = true;
                        for (Map.Entry<IMatterType, MatterValue> entry : compound.getValues().entrySet()) {
                            IMatterType matterType = entry.getKey();
                            int amount = (int) Math.ceil(entry.getValue().getAmount());
                            BasicFluidTank targetTank = getTankForMatterType(matterType);
                            if (targetTank == null || targetTank.getCapacity() - targetTank.getFluidAmount() < amount) {
                                allTanksHaveSpace = false;
                                break;
                            }
                        }

                        if (allTanksHaveSpace && energyContainer.getEnergy() >= energyUsage) {
                            canOperate = true;
                            activeSlotIndex = i;
                            recipeCompound = compound;
                            break;
                        }
                    }
                }
            }
        }

        boolean wasActive = getActive();
        if (canOperate) {
            setActive(true);
            energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.INTERNAL);
            operatingTicks++;

            if (operatingTicks >= ticksRequired) {
                operatingTicks = 0;
                if (recipeCompound != null && activeSlotIndex != -1) {
                    for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                        IMatterType matterType = entry.getKey();
                        int amount = (int) Math.ceil(entry.getValue().getAmount());
                        BasicFluidTank targetTank = getTankForMatterType(matterType);
                        if (targetTank != null) {
                            Fluid fluid = MatterFluidWrapper.getFluidFromMatterType(matterType);
                            if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                                targetTank.insert(new FluidStack(fluid, amount), Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        }
                    }
                    inputSlots.get(activeSlotIndex).extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
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

    @Nullable
    private BasicFluidTank getTankForMatterType(IMatterType matterType) {
        String name = matterType.getName().toLowerCase();
        return switch (name) {
            case "earth"    -> earthTank;
            case "nether"   -> netherTank;
            case "organic"  -> organicTank;
            case "ender"    -> enderTank;
            case "metallic" -> metallicTank;
            case "precious" -> preciousTank;
            case "living"   -> livingTank;
            case "quantum"  -> quantumTank;
            default         -> null;
        };
    }

    public double getScaledProgress() {
        return (double) operatingTicks / (double) ticksRequired;
    }

    public MachineEnergyContainer<CollapserBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.replicatemekanism.collapser_" + getTier().getName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.github.mochi7054.inventory.container.CollapserMenu(containerId, playerInventory, this);
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

    // ITierUpgradable Implementation
    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, mekanism.common.upgrade.IUpgradeData upgradeData) {
        if (upgradeData instanceof CollapserUpgradeData data) {
            this.energyContainer.setEnergy(data.energy);
            for (int i = 0; i < Math.min(this.inputSlots.size(), data.inputStacks.size()); i++) {
                this.inputSlots.get(i).setStack(data.inputStacks.get(i));
            }
            this.energySlot.setStack(data.energySlotStack);
            
            var tanks = this.getFluidTanks(null);
            for (int i = 0; i < Math.min(tanks.size(), data.fluidStacks.size()); i++) {
                tanks.get(i).setStack(data.fluidStacks.get(i));
            }
            
            for (mekanism.common.tile.component.ITileComponent component : this.getComponents()) {
                component.read(data.componentNbt, provider);
            }
            
            this.operatingTicks = data.operatingTicks;
        }
    }

    @Override
    public mekanism.common.upgrade.IUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        List<ItemStack> inputs = new java.util.ArrayList<>();
        for (InputInventorySlot slot : this.inputSlots) {
            inputs.add(slot.getStack().copy());
        }
        ItemStack energyStack = this.energySlot.getStack().copy();
        
        List<FluidStack> fluids = new java.util.ArrayList<>();
        for (mekanism.api.fluid.IExtendedFluidTank tank : this.getFluidTanks(null)) {
            fluids.add(tank.getFluid().copy());
        }
        
        CompoundTag componentsTag = new CompoundTag();
        for (mekanism.common.tile.component.ITileComponent component : this.getComponents()) {
            component.write(componentsTag, provider);
        }
        
        return new CollapserUpgradeData(
            this.energyContainer.getEnergy(),
            inputs,
            energyStack,
            fluids,
            componentsTag,
            this.operatingTicks
        );
    }

    public static class CollapserUpgradeData implements mekanism.common.upgrade.IUpgradeData {
        public final long energy;
        public final List<ItemStack> inputStacks;
        public final ItemStack energySlotStack;
        public final List<FluidStack> fluidStacks;
        public final CompoundTag componentNbt;
        public final int operatingTicks;
        
        public CollapserUpgradeData(long energy, List<ItemStack> inputStacks, ItemStack energySlotStack, List<FluidStack> fluidStacks, CompoundTag componentNbt, int operatingTicks) {
            this.energy = energy;
            this.inputStacks = inputStacks;
            this.energySlotStack = energySlotStack;
            this.fluidStacks = fluidStacks;
            this.componentNbt = componentNbt;
            this.operatingTicks = operatingTicks;
        }
    }

    @Override
    public void setRemoved() {
        if (networkElement != null) {
            try {
                networkElement.leaveNetwork();
            } catch (Exception e) {
                // Ignore
            }
            networkElement = null;
            currentNetwork = null;
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (networkElement != null) {
            try {
                networkElement.leaveNetwork();
            } catch (Exception e) {
                // Ignore
            }
            networkElement = null;
            currentNetwork = null;
        }
        super.onChunkUnloaded();
    }
}
