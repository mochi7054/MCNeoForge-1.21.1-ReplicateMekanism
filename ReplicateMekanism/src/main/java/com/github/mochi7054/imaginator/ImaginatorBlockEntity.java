package com.github.mochi7054.imaginator;

import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.ReplicaTier;
import com.github.mochi7054.imaginator.ImaginatorBlock;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.MatterValue;
import com.buuz135.replication.calculation.ReplicationCalculation;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class ImaginatorBlockEntity extends TileEntityConfigurableMachine implements MenuProvider {

    public static final int BASE_TICKS_REQUIRED = 100;
    public static final long BASE_ENERGY_USAGE = 50L;

    public int[] operatingTicks;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    // Replication Task variables
    public String activeTaskUuid = null;
    public com.buuz135.replication.api.task.IReplicationTask activeTask = null;
    public ItemStack activeCraftingStack = ItemStack.EMPTY;

    // Parallel task variables (active if sorting is true)
    public boolean sorting = false;
    public String[] activeTaskUuids;
    public com.buuz135.replication.api.task.IReplicationTask[] activeTasks;
    public ItemStack[] activeCraftingStacks;

    public void setSorting(boolean value) {
        this.sorting = value;
        setChanged();
    }

    // Replication Network elements
    private com.buuz135.replication.network.DefaultMatterNetworkElement networkElement = null;
    private com.buuz135.replication.network.MatterNetwork currentNetwork = null;

    private MachineEnergyContainer<ImaginatorBlockEntity> energyContainer;
    
    public com.github.mochi7054.fluid.SimpleMatterTank earthTank;
    public com.github.mochi7054.fluid.SimpleMatterTank netherTank;
    public com.github.mochi7054.fluid.SimpleMatterTank organicTank;
    public com.github.mochi7054.fluid.SimpleMatterTank enderTank;
    public com.github.mochi7054.fluid.SimpleMatterTank metallicTank;
    public com.github.mochi7054.fluid.SimpleMatterTank preciousTank;
    public com.github.mochi7054.fluid.SimpleMatterTank livingTank;
    public com.github.mochi7054.fluid.SimpleMatterTank quantumTank;
    public mekanism.common.capabilities.fluid.BasicFluidTank dummyFluidTank;

    public List<com.github.mochi7054.fluid.SimpleMatterTank> getMatterTanks() {
        return List.of(earthTank, netherTank, organicTank, enderTank, metallicTank, preciousTank, livingTank, quantumTank);
    }

    public List<InputInventorySlot> inputSlots;
    public List<OutputInventorySlot> outputSlots;
    private EnergyInventorySlot energySlot;

    public ReplicaTier getTier() {
        if (getBlockState().getBlock() instanceof ImaginatorBlock imaginatorBlock) {
            return imaginatorBlock.getTier();
        }
        return ReplicaTier.STANDARD;
    }

    private ReplicaTier getTierSafe() {
        try {
            BlockState state = getBlockState();
            if (state != null && state.getBlock() instanceof ImaginatorBlock imaginatorBlock) {
                return imaginatorBlock.getTier();
            }
        } catch (Exception e) {
            // Ignore
        }
        return ReplicaTier.STANDARD;
    }

    public ImaginatorBlockEntity(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);
        
        // ITEM config: all input slots and output slots
        configComponent.setupItemIOConfig(
            new ArrayList<>(inputSlots),
            new ArrayList<>(outputSlots),
            energySlot,
            false
        );

        mekanism.common.tile.component.config.ConfigInfo itemConfig =
                configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.setCanEject(true);
            itemConfig.setEjecting(true);
            for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
                itemConfig.setDataType(mekanism.common.tile.component.config.DataType.INPUT_OUTPUT, side);
            }
        }

        configComponent.setupInputConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY, energyContainer);
        mekanism.common.tile.component.config.ConfigInfo energyConfig = configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.ENERGY);
        if (energyConfig != null) {
            for (mekanism.api.RelativeSide side : mekanism.api.RelativeSide.values()) {
                energyConfig.setDataType(mekanism.common.tile.component.config.DataType.INPUT, side);
            }
        }

        // Add FLUID configuration (reused for Matter)
        configComponent.setupInputConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID, dummyFluidTank);

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
        int capacity = getTierSafe().getTankCapacity();
        earthTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.EARTH.get(), capacity, this::setChanged);
        netherTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.NETHER.get(), capacity, this::setChanged);
        organicTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.ORGANIC.get(), capacity, this::setChanged);
        enderTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.ENDER.get(), capacity, this::setChanged);
        metallicTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.METALLIC.get(), capacity, this::setChanged);
        preciousTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.PRECIOUS.get(), capacity, this::setChanged);
        livingTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.LIVING.get(), capacity, this::setChanged);
        quantumTank = new com.github.mochi7054.fluid.SimpleMatterTank(com.buuz135.replication.ReplicationRegistry.Matter.QUANTUM.get(), capacity, this::setChanged);

        dummyFluidTank = mekanism.common.capabilities.fluid.BasicFluidTank.create(0, (fluid) -> false, (fluid) -> false, listener);
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(dummyFluidTank);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        ReplicaTier tier = getTierSafe();
        int slotCount = tier.getSlotCount();
        this.operatingTicks = new int[slotCount];
        
        // Initialize parallel task arrays
        this.activeTaskUuids = new String[slotCount];
        this.activeTasks = new com.buuz135.replication.api.task.IReplicationTask[slotCount];
        this.activeCraftingStacks = new ItemStack[slotCount];
        for (int i = 0; i < slotCount; i++) {
            this.activeCraftingStacks[i] = ItemStack.EMPTY;
        }
        
        int[][] inputCoords = new int[slotCount][2];
        int[][] outputCoords = new int[slotCount][2];
        int energyX;
        int energyY;
        if (tier == ReplicaTier.STANDARD) {
            inputCoords[0][0] = 75;
            inputCoords[0][1] = 40;
            outputCoords[0][0] = 122;
            outputCoords[0][1] = 40;
            energyX = 141;
            energyY = 40;
        } else {
            int startX;
            int gap;
            if (tier == ReplicaTier.BASIC) {
                startX = 55;
                gap = 38;
            } else if (tier == ReplicaTier.ADVANCED) {
                startX = 35;
                gap = 26;
            } else if (tier == ReplicaTier.ELITE) {
                startX = 32;
                gap = 19;
            } else { // ULTIMATE
                startX = 30;
                gap = 19;
            }
            for (int i = 0; i < slotCount; i++) {
                inputCoords[i][0] = startX + i * gap;
                inputCoords[i][1] = 17;
                outputCoords[i][0] = startX + i * gap;
                outputCoords[i][1] = 58;
            }
            energyX = 10;
            energyY = 17;
        }

        if (inputSlots == null) {
            inputSlots = new java.util.ArrayList<>();
        } else {
            inputSlots.clear();
        }
        if (outputSlots == null) {
            outputSlots = new java.util.ArrayList<>();
        } else {
            outputSlots.clear();
        }

        for (int i = 0; i < slotCount; i++) {
            InputInventorySlot inputSlot = InputInventorySlot.at(stack -> {
                MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
                return compound != null && !compound.getValues().isEmpty();
            }, listener, inputCoords[i][0], inputCoords[i][1]);
            inputSlots.add(inputSlot);
            builder.addSlot(inputSlot);
        }

        for (int i = 0; i < slotCount; i++) {
            OutputInventorySlot outputSlot = OutputInventorySlot.at(listener, outputCoords[i][0], outputCoords[i][1]);
            outputSlots.add(outputSlot);
            builder.addSlot(outputSlot);
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

    private void cancelActiveTask(int idx) {
        if (this.activeCraftingStacks != null && idx >= 0 && idx < this.activeCraftingStacks.length) {
            ItemStack activeStack = this.activeCraftingStacks[idx];
            if (!activeStack.isEmpty() && inputSlots != null && idx < inputSlots.size()) {
                InputInventorySlot inputSlot = inputSlots.get(idx);
                ItemStack inputStack = inputSlot.getStack();
                if (ItemStack.isSameItemSameComponents(inputStack, activeStack)) {
                    inputSlot.setStackUnchecked(ItemStack.EMPTY);
                }
            }
            
            com.buuz135.replication.api.task.IReplicationTask task = this.activeTasks[idx];
            if (task != null) {
                boolean shared = false;
                for (int j = 0; j < this.activeTasks.length; j++) {
                    if (j != idx && this.activeTasks[j] != null && this.activeTasks[j].getUuid().equals(task.getUuid())) {
                        shared = true;
                        break;
                    }
                }
                if (!shared) {
                    task.getReplicatorsOnTask().remove(getBlockPos().asLong());
                    task.getStoredMatterStack().remove(getBlockPos().asLong());
                    com.buuz135.replication.network.MatterNetwork network = getNetwork();
                    if (network != null && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        network.onTaskValueChanged(task, serverLevel);
                    }
                }
            }

            this.activeTasks[idx] = null;
            this.activeTaskUuids[idx] = null;
            this.activeCraftingStacks[idx] = ItemStack.EMPTY;
        }
        if (idx == 0) {
            this.activeTask = null;
            this.activeTaskUuid = null;
            this.activeCraftingStack = ItemStack.EMPTY;
        }
        if (this.operatingTicks != null && idx < this.operatingTicks.length) {
            this.operatingTicks[idx] = 0;
        }
    }

    private void cancelActiveTask() {
        if (this.activeCraftingStacks != null) {
            for (int i = 0; i < this.activeCraftingStacks.length; i++) {
                cancelActiveTask(i);
            }
        } else {
            cancelActiveTask(0);
        }
    }

    private void pullMatterFromNetwork(com.buuz135.replication.network.MatterNetwork network) {
        java.util.List<com.hrznstudio.titanium.block_network.element.NetworkElement> sources = new java.util.ArrayList<>();
        sources.addAll(network.getMatterStacksSuppliers());
        sources.addAll(network.getMatterStacksHolders());
        if (sources.isEmpty()) return;

        for (com.hrznstudio.titanium.block_network.element.NetworkElement element : sources) {
            if (element.getPos().equals(worldPosition)) continue;

            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(element.getPos());
            if (!(be instanceof com.buuz135.replication.api.network.IMatterTanksSupplier supplier)) continue;

            for (com.buuz135.replication.api.matter_fluid.IMatterTank sourceTank : supplier.getTanks()) {
                com.buuz135.replication.api.matter_fluid.MatterStack matter = sourceTank.getMatter();
                if (matter == null || matter.isEmpty() || matter.getAmount() <= 0) continue;

                com.github.mochi7054.fluid.SimpleMatterTank localTank = getMatchingTank(matter.getMatterType());
                if (localTank == null) continue;

                double space = localTank.getCapacity() - localTank.getMatterAmount();
                if (space <= 0) continue;

                double toTransfer = Math.min(space, matter.getAmount());
                if (toTransfer <= 0) continue;

                com.buuz135.replication.api.matter_fluid.MatterStack simDrain =
                    sourceTank.drain(toTransfer, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                if (simDrain == null || simDrain.isEmpty() || simDrain.getAmount() <= 0) continue;

                double filled = localTank.fill(simDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    sourceTank.drain(filled, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    private com.github.mochi7054.fluid.SimpleMatterTank getMatchingTank(IMatterType neededMatterType) {
        for (com.github.mochi7054.fluid.SimpleMatterTank tank : getMatterTanks()) {
            if (tank.getMatter().getMatterType().getName().equals(neededMatterType.getName())) {
                return tank;
            }
        }
        return null;
    }

    private void performReplication(int activeSlotIndex, MatterCompound recipeCompound, com.buuz135.replication.network.MatterNetwork network) {
        if (recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
            for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                IMatterType neededMatterType = entry.getKey();
                double neededAmount = entry.getValue().getAmount();

                com.github.mochi7054.fluid.SimpleMatterTank matchingTank = getMatchingTank(neededMatterType);
                if (matchingTank != null) {
                    matchingTank.drain(neededAmount, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                }
            }

            com.buuz135.replication.api.task.IReplicationTask task = null;
            ItemStack craftingStack = ItemStack.EMPTY;
            if (this.activeTasks != null && activeSlotIndex < this.activeTasks.length) {
                task = this.activeTasks[activeSlotIndex];
                craftingStack = this.activeCraftingStacks[activeSlotIndex];
            } else if (activeSlotIndex == 0) {
                task = this.activeTask;
                craftingStack = this.activeCraftingStack;
            }

            if (task != null) {
                task.finalizeReplication(level, getBlockPos(), network);
                network.onTaskValueChanged(task, (net.minecraft.server.level.ServerLevel) level);

                BlockPos source = task.getSource();
                ItemStack copyStack = craftingStack.copyWithCount(1);

                if (!getBlockPos().equals(source)) {
                    net.neoforged.neoforge.items.IItemHandler itemHandler = level.getCapability(
                        net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                        source,
                        Direction.UP
                    );
                    if (itemHandler != null) {
                        ItemStack remaining = net.neoforged.neoforge.items.ItemHandlerHelper.insertItem(itemHandler, copyStack, false);
                        if (!remaining.isEmpty()) {
                            OutputInventorySlot outputSlot = outputSlots.get(activeSlotIndex);
                            ItemStack outputStack = outputSlot.getStack();
                            if (outputStack.isEmpty()) {
                                outputSlot.setStack(remaining);
                            } else if (ItemStack.isSameItemSameComponents(outputStack, remaining) && outputStack.getCount() + remaining.getCount() <= outputStack.getMaxStackSize()) {
                                outputSlot.growStack(remaining.getCount(), Action.EXECUTE);
                            } else {
                                net.minecraft.world.Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ(), remaining);
                            }
                        }
                    } else {
                        OutputInventorySlot outputSlot = outputSlots.get(activeSlotIndex);
                        ItemStack outputStack = outputSlot.getStack();
                        if (outputStack.isEmpty()) {
                            outputSlot.setStack(copyStack);
                        } else if (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize()) {
                            outputSlot.growStack(1, Action.EXECUTE);
                        } else {
                            net.minecraft.world.Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ(), copyStack);
                        }
                    }
                } else {
                    OutputInventorySlot outputSlot = outputSlots.get(activeSlotIndex);
                    ItemStack outputStack = outputSlot.getStack();
                    if (outputStack.isEmpty()) {
                        outputSlot.setStack(copyStack);
                    } else if (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize()) {
                        outputSlot.growStack(1, Action.EXECUTE);
                    } else {
                        net.minecraft.world.Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ(), copyStack);
                    }
                }

                cancelActiveTask(activeSlotIndex);
            } else {
                int outputCount = 1;
                if (getComponent() != null) {
                    int upgradeCount = getComponent().getUpgrades(ReplicateMekanism.REPLICA_UPGRADE_TYPE);
                    outputCount = 1 << upgradeCount;
                }

                OutputInventorySlot outputSlot = outputSlots.get(activeSlotIndex);
                ItemStack sourceStack = inputSlots.get(activeSlotIndex).getStack();
                ItemStack outputStack = outputSlot.getStack();
                if (outputStack.isEmpty()) {
                    ItemStack newOutput = sourceStack.copy();
                    newOutput.setCount(outputCount);
                    outputSlot.setStack(newOutput);
                } else {
                    outputSlot.growStack(outputCount, Action.EXECUTE);
                }
            }
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();

        if (energySlot != null) {
            energySlot.fillContainerOrConvert();
        }

        com.buuz135.replication.network.MatterNetwork pullNetwork = getNetwork();
        if (pullNetwork != null && this.ticker % 10 == 0) {
            pullMatterFromNetwork(pullNetwork);
        }

        int slotCount = inputSlots.size();
        if (this.activeTaskUuids == null) {
            this.activeTaskUuids = new String[slotCount];
            this.activeTasks = new com.buuz135.replication.api.task.IReplicationTask[slotCount];
            this.activeCraftingStacks = new ItemStack[slotCount];
            for (int i = 0; i < slotCount; i++) {
                this.activeCraftingStacks[i] = ItemStack.EMPTY;
            }
        }

        // sorting=false: all slots accept different orders in parallel
        // sorting=true:  only slot 0 is used, processing one order serially
        boolean sortingActive = !this.sorting && slotCount > 1;

        for (int i = 0; i < slotCount; i++) {
            if (i > 0) {
                boolean shouldCancel = false;
                if (!sortingActive) {
                    // Auto sort ON (Sharing mode)
                    if (this.activeTasks[0] == null) {
                        shouldCancel = true;
                    } else if (this.activeTasks[i] != null && !this.activeTasks[i].getUuid().equals(this.activeTasks[0].getUuid())) {
                        shouldCancel = true;
                    } else if (this.activeTasks[i] != null) {
                        // Check remaining count constraint
                        com.buuz135.replication.api.task.IReplicationTask task0 = this.activeTasks[0];
                        int remaining = task0.isInfinteMode() ? 999 : (task0.getTotalAmount() - task0.getCurrentAmount());
                        int assignedCount = 0;
                        for (int j = 0; j < i; j++) {
                            if (this.activeTasks[j] != null && this.activeTasks[j].getUuid().equals(task0.getUuid())) {
                                assignedCount++;
                            }
                        }
                        if (assignedCount >= remaining) {
                            shouldCancel = true;
                        }
                    }
                } else {
                    // Auto sort OFF (Independent mode)
                    if (this.activeTasks[i] != null && this.activeTasks[0] != null && this.activeTasks[i].getUuid().equals(this.activeTasks[0].getUuid())) {
                        shouldCancel = true;
                    }
                }

                if (shouldCancel) {
                    if (this.activeTasks[i] != null || !this.activeCraftingStacks[i].isEmpty()) {
                        cancelActiveTask(i);
                        sendUpdate = true;
                    }
                    continue;
                }
            }

            com.buuz135.replication.api.task.IReplicationTask task = this.activeTasks[i];
            ItemStack craftingStack = this.activeCraftingStacks[i];

            if (task != null) {
                InputInventorySlot inputSlot = inputSlots.get(i);
                ItemStack currentInput = inputSlot.getStack();
                if (currentInput.isEmpty() || !ItemStack.isSameItemSameComponents(currentInput, craftingStack)) {
                    cancelActiveTask(i);
                    sendUpdate = true;
                } else if (currentInput.getCount() != 1) {
                    currentInput.setCount(1);
                }
            }
        }

        ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        long energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);

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

        if (network != null) {
            for (int i = 0; i < slotCount; i++) {
                String uuidStr = this.activeTaskUuids[i];
                if (uuidStr != null && this.activeTasks[i] == null) {
                    com.buuz135.replication.api.task.IReplicationTask task = network.getTaskManager().getPendingTasks().get(uuidStr);
                    if (task != null) {
                        this.activeTasks[i] = task;
                        this.activeCraftingStacks[i] = task.getReplicatingStack();
                        inputSlots.get(i).setStackUnchecked(this.activeCraftingStacks[i].copyWithCount(1));
                        if (i == 0) {
                            this.activeTask = task;
                            this.activeCraftingStack = this.activeCraftingStacks[0];
                        }
                    } else {
                        cancelActiveTask(i);
                        sendUpdate = true;
                    }
                }
            }
        }

        if (sortingActive && this.ticker % 100 == 0) {
            ReplicateMekanism.LOGGER.info("DEBUG RM: Task sharing mode inactive. sorting={}", this.sorting);
        }

        boolean[] canOperate = new boolean[inputSlots.size()];
        MatterCompound[] slotCompounds = new MatterCompound[inputSlots.size()];

        if (canFunction()) {
            for (int i = 0; i < inputSlots.size(); i++) {
                com.buuz135.replication.api.task.IReplicationTask task = this.activeTasks[i];
                ItemStack checkStack = ItemStack.EMPTY;
                if (task != null) {
                    checkStack = this.activeCraftingStacks[i];
                } else {
                    checkStack = inputSlots.get(i).getStack();
                }

                if (!checkStack.isEmpty()) {
                    MatterCompound recipeCompound = ReplicationCalculation.getMatterCompound(checkStack);
                    if (recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
                        boolean allFluidsAvailable = true;
                        for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                            IMatterType neededMatterType = entry.getKey();
                            double neededAmount = entry.getValue().getAmount();
                            com.github.mochi7054.fluid.SimpleMatterTank matchingTank = getMatchingTank(neededMatterType);
                            if (matchingTank == null || matchingTank.getMatterAmount() < neededAmount) {
                                allFluidsAvailable = false;
                                break;
                            }
                        }

                        if (allFluidsAvailable) {
                            OutputInventorySlot outputSlot = outputSlots.get(i);
                            ItemStack outputStack = outputSlot.getStack();
                            ItemStack copyStack = checkStack.copyWithCount(1);
                            boolean outputCompatible = outputStack.isEmpty() || (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize());
                            if (outputCompatible) {
                                canOperate[i] = true;
                                slotCompounds[i] = recipeCompound;
                            }
                        }
                    }
                }
            }

            // Automatic task dispatch (when idle / slots available)
            if (network != null && this.ticker % 4 == 0) {
                for (int i = 0; i < slotCount; i++) {
                    if (this.activeTasks[i] == null) {
                        OutputInventorySlot outputSlot = outputSlots.get(i);
                        ItemStack outputStack = outputSlot.getStack();
                        if (outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize()) {

                            com.buuz135.replication.api.task.IReplicationTask task = null;
                            if (i == 0 || sortingActive) {
                                // First try to find a unique task not already accepted by any of our slots
                                for (com.buuz135.replication.api.task.IReplicationTask candidate :
                                        network.getTaskManager().getPendingTasks().values()) {

                                    boolean alreadyAcceptedByUs = false;
                                    for (int j = 0; j < slotCount; j++) {
                                        if (this.activeTasks[j] != null && this.activeTasks[j].getUuid().equals(candidate.getUuid())) {
                                            alreadyAcceptedByUs = true;
                                            break;
                                        }
                                    }
                                    if (alreadyAcceptedByUs) continue;

                                    if (candidate.canAcceptReplicator(getBlockPos(), 1)) {
                                        task = candidate;
                                        break;
                                    }
                                }
                            }

                            if (task != null) {
                                // Found a unique task: register and assign
                                task.acceptReplicator(getBlockPos());
                                this.activeTasks[i] = task;
                                this.activeTaskUuids[i] = task.getUuid().toString();
                                this.activeCraftingStacks[i] = task.getReplicatingStack().copy();
                                inputSlots.get(i).setStackUnchecked(this.activeCraftingStacks[i].copyWithCount(1));
                                if (i == 0) {
                                    this.activeTask = task;
                                    this.activeTaskUuid = this.activeTaskUuids[0];
                                    this.activeCraftingStack = this.activeCraftingStacks[0];
                                }
                                network.onTaskValueChanged(task, (net.minecraft.server.level.ServerLevel) level);
                                sendUpdate = true;
                            } else if (i > 0 && !sortingActive && this.activeTasks[0] != null) {
                                // Auto sort ON (Sharing mode): share slot 0's task for parallel processing
                                // Check remaining count constraint
                                com.buuz135.replication.api.task.IReplicationTask task0 = this.activeTasks[0];
                                int remaining = task0.isInfinteMode() ? 999 : (task0.getTotalAmount() - task0.getCurrentAmount());
                                int assignedCount = 0;
                                for (int j = 0; j < i; j++) {
                                    if (this.activeTasks[j] != null && this.activeTasks[j].getUuid().equals(task0.getUuid())) {
                                        assignedCount++;
                                    }
                                }
                                if (assignedCount < remaining) {
                                    this.activeTasks[i] = this.activeTasks[0];
                                    this.activeTaskUuids[i] = this.activeTaskUuids[0];
                                    this.activeCraftingStacks[i] = this.activeCraftingStacks[0].copy();
                                    inputSlots.get(i).setStackUnchecked(this.activeCraftingStacks[i].copyWithCount(1));
                                    sendUpdate = true;
                                }
                            }
                        }
                    }
                }
            }

        }

        boolean anyOperating = false;
        boolean wasActive = getActive();
        for (int i = 0; i < inputSlots.size(); i++) {
            if (canOperate[i]) {
                if (energyContainer.getEnergy() >= energyUsage) {
                    energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.INTERNAL);
                    operatingTicks[i]++;
                    anyOperating = true;
                    if (operatingTicks[i] >= ticksRequired) {
                        operatingTicks[i] = 0;
                        performReplication(i, slotCompounds[i], network);
                        sendUpdate = true;
                    }
                } else {
                    if (operatingTicks[i] > 0) {
                        operatingTicks[i] = Math.max(0, operatingTicks[i] - 2);
                        sendUpdate = true;
                    }
                }
            } else {
                if (operatingTicks[i] > 0) {
                    operatingTicks[i] = Math.max(0, operatingTicks[i] - 2);
                    sendUpdate = true;
                }
            }
        }

        setActive(anyOperating);

        if (wasActive != getActive()) {
            sendUpdate = true;
        }

        // 自動搬出 (Auto-Eject): configComponentでFLUIDの自動排出が有効、またはOUTPUTに設定されている面にプッシュ
        if (configComponent != null) {
            for (Direction dir : Direction.values()) {
                var config = configComponent.getConfig(mekanism.common.lib.transmitter.TransmissionType.FLUID);
                if (config != null) {
                    var dataType = config.getDataType(mekanism.api.RelativeSide.fromDirections(getDirection(), dir));
                    if (dataType == mekanism.common.tile.component.config.DataType.OUTPUT || 
                        dataType == mekanism.common.tile.component.config.DataType.INPUT_OUTPUT) {
                        
                        BlockPos adjacent = worldPosition.relative(dir);
                        if (level != null && level.isLoaded(adjacent)) {
                            net.minecraft.world.level.block.entity.BlockEntity adjacentBE = level.getBlockEntity(adjacent);
                            boolean ejected = false;

                            // 最優先: 隣接ブロックが NetworkBlockEntity (Replicationパイプ・タンク等) なら直接タンクへFill
                            if (adjacentBE instanceof com.buuz135.replication.block.tile.NetworkBlockEntity<?> networkBE) {
                                for (com.github.mochi7054.fluid.SimpleMatterTank myTank : getMatterTanks()) {
                                    com.buuz135.replication.api.matter_fluid.MatterStack stored = myTank.getMatter();
                                    if (stored != null && !stored.isEmpty() && stored.getAmount() > 0) {
                                        for (var component : networkBE.getMatterTankComponents()) {
                                            com.buuz135.replication.api.matter_fluid.MatterStack compStored = component.getMatter();
                                            if (compStored == null || compStored.isEmpty() || 
                                                (compStored.getMatterType() != null && stored.getMatterType() != null &&
                                                 compStored.getMatterType().getName().equalsIgnoreCase(stored.getMatterType().getName()))) {
                                                double filled = component.fill(stored, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                                                if (filled > 0) {
                                                    myTank.drain(filled, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                                                    component.fill(new com.buuz135.replication.api.matter_fluid.MatterStack(stored.getMatterType(), filled), 
                                                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                                                    sendUpdate = true;
                                                    ejected = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (ejected) break;
                                    }
                                }
                            }

                            if (!ejected) {
                                // 次: IMatterHandler Capability (他のMod対応)
                                com.buuz135.replication.api.matter_fluid.IMatterHandler targetHandler = 
                                    level.getCapability(com.buuz135.replication.ReplicationRegistry.Capabilities.MATTER_HANDLER, adjacent, dir.getOpposite());
                                
                                if (targetHandler != null) {
                                    for (com.github.mochi7054.fluid.SimpleMatterTank tank : getMatterTanks()) {
                                        com.buuz135.replication.api.matter_fluid.MatterStack stored = tank.getMatter();
                                        if (stored != null && !stored.isEmpty() && stored.getAmount() > 0) {
                                            double filled = targetHandler.fill(stored, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                                            if (filled > 0) {
                                                tank.drain(filled, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                                                targetHandler.fill(
                                                    new com.buuz135.replication.api.matter_fluid.MatterStack(stored.getMatterType(), filled), 
                                                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                                                );
                                                sendUpdate = true;
                                            }
                                        }
                                    }
                                } else {
                                    // Fallback: IFluidHandler push (for AE2 Pattern Provider etc.)
                                    net.neoforged.neoforge.fluids.capability.IFluidHandler targetFluidHandler = 
                                        level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, adjacent, dir.getOpposite());
                                    if (targetFluidHandler != null) {
                                        for (com.github.mochi7054.fluid.SimpleMatterTank tank : getMatterTanks()) {
                                            com.buuz135.replication.api.matter_fluid.MatterStack stored = tank.getMatter();
                                            if (stored != null && !stored.isEmpty() && stored.getAmount() > 0) {
                                                net.minecraft.world.level.material.Fluid fluid = com.github.mochi7054.fluid.ReplicationFluidHandler.getFluidFromMatter(stored.getMatterType());
                                                if (fluid != null) {
                                                    int mBAmount = (int) Math.round(stored.getAmount() * 1000.0);
                                                    net.neoforged.neoforge.fluids.FluidStack fluidStack = new net.neoforged.neoforge.fluids.FluidStack(fluid, mBAmount);
                                                    int filled = targetFluidHandler.fill(fluidStack, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                                                    if (filled > 0) {
                                                        double drainedMatter = filled / 1000.0;
                                                        tank.drain(drainedMatter, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                                                        targetFluidHandler.fill(
                                                            new net.neoforged.neoforge.fluids.FluidStack(fluid, filled), 
                                                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                                                        );
                                                        sendUpdate = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return sendUpdate;
    }

    public double getScaledProgress(int slotIndex) {
        if (this.operatingTicks == null || slotIndex < 0 || slotIndex >= this.operatingTicks.length) {
            return 0;
        }
        return (double) this.operatingTicks[slotIndex] / (double) ticksRequired;
    }

    public double getScaledProgress() {
        return getScaledProgress(0);
    }

    public MachineEnergyContainer<ImaginatorBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.replicatemekanism.imaginator_" + getTier().getName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.github.mochi7054.imaginator.ImaginatorMenu(containerId, playerInventory, this);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (this.operatingTicks != null) {
            for (int i = 0; i < this.operatingTicks.length; i++) {
                final int idx = i;
                container.track(SyncableInt.create(() -> this.operatingTicks[idx], value -> this.operatingTicks[idx] = value));
            }
        }
        container.track(SyncableInt.create(() -> ticksRequired, value -> ticksRequired = value));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(() -> this.sorting, value -> this.sorting = value));

        for (com.github.mochi7054.fluid.SimpleMatterTank tank : getMatterTanks()) {
            container.track(mekanism.common.inventory.container.sync.SyncableDouble.create(tank::getMatterAmount, tank::setAmount));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int slotCount = getTierSafe().getSlotCount();
        if (this.activeTaskUuids == null) {
            this.activeTaskUuids = new String[slotCount];
            this.activeTasks = new com.buuz135.replication.api.task.IReplicationTask[slotCount];
            this.activeCraftingStacks = new ItemStack[slotCount];
            for (int i = 0; i < slotCount; i++) {
                this.activeCraftingStacks[i] = ItemStack.EMPTY;
            }
        }

        if (tag.contains("sorting", Tag.TAG_BYTE)) {
            this.sorting = tag.getBoolean("sorting");
        }

        if (this.operatingTicks != null) {
            if (tag.contains("operatingTicksArray", Tag.TAG_INT_ARRAY)) {
                int[] saved = tag.getIntArray("operatingTicksArray");
                System.arraycopy(saved, 0, this.operatingTicks, 0, Math.min(this.operatingTicks.length, saved.length));
            } else if (tag.contains("operatingTicks", Tag.TAG_INT)) {
                int legacy = tag.getInt("operatingTicks");
                if (this.operatingTicks.length > 0) {
                    this.operatingTicks[0] = legacy;
                }
            }
        }
        if (tag.contains("activeTaskUuid")) {
            this.activeTaskUuid = tag.getString("activeTaskUuid");
            this.activeTaskUuids[0] = this.activeTaskUuid;
        } else {
            this.activeTaskUuid = null;
            this.activeTaskUuids[0] = null;
        }
        if (tag.contains("activeCraftingStack")) {
            this.activeCraftingStack = ItemStack.parse(registries, tag.getCompound("activeCraftingStack")).orElse(ItemStack.EMPTY);
            this.activeCraftingStacks[0] = this.activeCraftingStack;
        } else {
            this.activeCraftingStack = ItemStack.EMPTY;
            this.activeCraftingStacks[0] = ItemStack.EMPTY;
        }

        if (tag.contains("activeTaskUuidsList", Tag.TAG_LIST)) {
            ListTag list = tag.getList("activeTaskUuidsList", Tag.TAG_STRING);
            for (int i = 0; i < Math.min(list.size(), this.activeTaskUuids.length); i++) {
                String val = list.getString(i);
                this.activeTaskUuids[i] = val.isEmpty() ? null : val;
            }
        }
        if (tag.contains("activeCraftingStacksList", Tag.TAG_LIST)) {
            ListTag list = tag.getList("activeCraftingStacksList", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(list.size(), this.activeCraftingStacks.length); i++) {
                this.activeCraftingStacks[i] = ItemStack.parse(registries, list.getCompound(i)).orElse(ItemStack.EMPTY);
            }
        }

        this.activeTaskUuid = this.activeTaskUuids[0];
        this.activeCraftingStack = this.activeCraftingStacks[0];

        if (tag.contains("matterTanks", Tag.TAG_COMPOUND)) {
            CompoundTag tanksTag = tag.getCompound("matterTanks");
            earthTank.setAmount(tanksTag.getDouble("earth"));
            netherTank.setAmount(tanksTag.getDouble("nether"));
            organicTank.setAmount(tanksTag.getDouble("organic"));
            enderTank.setAmount(tanksTag.getDouble("ender"));
            metallicTank.setAmount(tanksTag.getDouble("metallic"));
            preciousTank.setAmount(tanksTag.getDouble("precious"));
            livingTank.setAmount(tanksTag.getDouble("living"));
            quantumTank.setAmount(tanksTag.getDouble("quantum"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("sorting", this.sorting);

        if (this.activeTaskUuids != null && this.activeTaskUuids.length > 0) {
            this.activeTaskUuid = this.activeTaskUuids[0];
            this.activeCraftingStack = this.activeCraftingStacks[0];
        }

        if (this.operatingTicks != null) {
            tag.putIntArray("operatingTicksArray", this.operatingTicks);
            if (this.operatingTicks.length > 0) {
                tag.putInt("operatingTicks", this.operatingTicks[0]);
            }
        }
        if (this.activeTaskUuid != null) {
            tag.putString("activeTaskUuid", this.activeTaskUuid);
        }
        if (!this.activeCraftingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            this.activeCraftingStack.save(registries, stackTag);
            tag.put("activeCraftingStack", stackTag);
        }

        if (this.activeTaskUuids != null) {
            ListTag listUuids = new ListTag();
            for (String uuid : this.activeTaskUuids) {
                listUuids.add(net.minecraft.nbt.StringTag.valueOf(uuid != null ? uuid : ""));
            }
            tag.put("activeTaskUuidsList", listUuids);

            ListTag listStacks = new ListTag();
            for (ItemStack stack : this.activeCraftingStacks) {
                CompoundTag stackTag = new CompoundTag();
                if (!stack.isEmpty()) {
                    stack.save(registries, stackTag);
                }
                listStacks.add(stackTag);
            }
            tag.put("activeCraftingStacksList", listStacks);
        }

        CompoundTag tanksTag = new CompoundTag();
        tanksTag.putDouble("earth", earthTank.getMatterAmount());
        tanksTag.putDouble("nether", netherTank.getMatterAmount());
        tanksTag.putDouble("organic", organicTank.getMatterAmount());
        tanksTag.putDouble("ender", enderTank.getMatterAmount());
        tanksTag.putDouble("metallic", metallicTank.getMatterAmount());
        tanksTag.putDouble("precious", preciousTank.getMatterAmount());
        tanksTag.putDouble("living", livingTank.getMatterAmount());
        tanksTag.putDouble("quantum", quantumTank.getMatterAmount());
        tag.put("matterTanks", tanksTag);
    }

    // ITierUpgradable Implementation
    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, mekanism.common.upgrade.IUpgradeData upgradeData) {
        if (upgradeData instanceof ImaginatorUpgradeData data) {
            this.energyContainer.setEnergy(data.energy);
            this.sorting = data.sorting;
            for (int i = 0; i < Math.min(this.inputSlots.size(), data.inputStacks.size()); i++) {
                this.inputSlots.get(i).setStack(data.inputStacks.get(i));
            }
            for (int i = 0; i < Math.min(this.outputSlots.size(), data.outputStacks.size()); i++) {
                this.outputSlots.get(i).setStack(data.outputStacks.get(i));
            }
            this.energySlot.setStack(data.energySlotStack);
            
            var tanks = this.getMatterTanks();
            for (int i = 0; i < Math.min(tanks.size(), data.matterAmounts.size()); i++) {
                tanks.get(i).setAmount(data.matterAmounts.get(i));
            }
            
            for (mekanism.common.tile.component.ITileComponent component : this.getComponents()) {
                component.read(data.componentNbt, provider);
            }
            
            if (data.operatingTicks != null && this.operatingTicks != null) {
                System.arraycopy(data.operatingTicks, 0, this.operatingTicks, 0, Math.min(this.operatingTicks.length, data.operatingTicks.length));
            }
        }
    }

    @Override
    public mekanism.common.upgrade.IUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        List<ItemStack> inputs = new java.util.ArrayList<>();
        for (InputInventorySlot slot : this.inputSlots) {
            inputs.add(slot.getStack().copy());
        }
        List<ItemStack> outputs = new java.util.ArrayList<>();
        for (OutputInventorySlot slot : this.outputSlots) {
            outputs.add(slot.getStack().copy());
        }
        ItemStack energyStack = this.energySlot.getStack().copy();
        
        List<Double> matterAmounts = new java.util.ArrayList<>();
        for (com.github.mochi7054.fluid.SimpleMatterTank tank : this.getMatterTanks()) {
            matterAmounts.add(tank.getMatterAmount());
        }
        
        CompoundTag componentsTag = new CompoundTag();
        for (mekanism.common.tile.component.ITileComponent component : this.getComponents()) {
            component.write(componentsTag, provider);
        }
        
        return new ImaginatorUpgradeData(
            this.energyContainer.getEnergy(),
            inputs,
            outputs,
            energyStack,
            matterAmounts,
            componentsTag,
            this.operatingTicks != null ? this.operatingTicks.clone() : new int[0],
            this.sorting
        );
    }

    public static class ImaginatorUpgradeData implements mekanism.common.upgrade.IUpgradeData {
        public final long energy;
        public final List<ItemStack> inputStacks;
        public final List<ItemStack> outputStacks;
        public final ItemStack energySlotStack;
        public final List<Double> matterAmounts;
        public final CompoundTag componentNbt;
        public final int[] operatingTicks;
        public final boolean sorting;
        
        public ImaginatorUpgradeData(long energy, List<ItemStack> inputStacks, List<ItemStack> outputStacks, ItemStack energySlotStack, List<Double> matterAmounts, CompoundTag componentNbt, int[] operatingTicks, boolean sorting) {
            this.energy = energy;
            this.inputStacks = inputStacks;
            this.outputStacks = outputStacks;
            this.energySlotStack = energySlotStack;
            this.matterAmounts = matterAmounts;
            this.componentNbt = componentNbt;
            this.operatingTicks = operatingTicks;
            this.sorting = sorting;
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
