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

import java.util.List;
import java.util.Map;


public class ImaginatorBlockEntity extends TileEntityConfigurableMachine implements MenuProvider {

    public static final int BASE_TICKS_REQUIRED = 100;
    public static final long BASE_ENERGY_USAGE = 50L;

    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    // Replication Task variables
    public String activeTaskUuid = null;
    public com.buuz135.replication.api.task.IReplicationTask activeTask = null;
    public ItemStack activeCraftingStack = ItemStack.EMPTY;

    // Replication Network elements
    private com.buuz135.replication.network.DefaultMatterNetworkElement networkElement = null;
    private com.buuz135.replication.network.MatterNetwork currentNetwork = null;

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
        // ITEM config: no item input (null は内部NPEの可能性ありのでList版を使用)
        configComponent.setupItemIOConfig(
            java.util.Collections.emptyList(),   // no item input
            java.util.Collections.singletonList(outputSlot), // item output
            energySlot,
            false
        );
        
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

    private void cancelActiveTask() {
        if (!activeCraftingStack.isEmpty() && inputSlot != null) {
            ItemStack inputStack = inputSlot.getStack();
            if (ItemStack.isSameItemSameComponents(inputStack, activeCraftingStack)) {
                inputSlot.setStackUnchecked(ItemStack.EMPTY);
            }
        }
        this.activeTask = null;
        this.activeTaskUuid = null;
        this.activeCraftingStack = ItemStack.EMPTY;
        this.operatingTicks = 0;
    }

    /**
     * ネットワーク上のSupplier/Holderから各種マターを能動的にpullしてローカルタンクに充填する。
     * MatterNetwork.update()のBiPredicateに依存しない確実な搬入方式。
     */
    private void pullMatterFromNetwork(com.buuz135.replication.network.MatterNetwork network) {
        // Pull from both pure suppliers and holders (both implement IMatterTanksSupplier)
        java.util.List<com.hrznstudio.titanium.block_network.element.NetworkElement> sources = new java.util.ArrayList<>();
        sources.addAll(network.getMatterStacksSuppliers());
        sources.addAll(network.getMatterStacksHolders());
        if (sources.isEmpty()) return;

        for (com.hrznstudio.titanium.block_network.element.NetworkElement element : sources) {
            // Skip self
            if (element.getPos().equals(worldPosition)) continue;

            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(element.getPos());
            if (!(be instanceof com.buuz135.replication.api.network.IMatterTanksSupplier supplier)) continue;

            for (com.buuz135.replication.api.matter_fluid.IMatterTank sourceTank : supplier.getTanks()) {
                com.buuz135.replication.api.matter_fluid.MatterStack matter = sourceTank.getMatter();
                if (matter == null || matter.isEmpty()) continue;

                // Determine the fluid corresponding to this matter type
                net.minecraft.world.level.material.Fluid targetFluid =
                    com.github.mochi7054.fluid.MatterFluidWrapper.getFluidFromMatterType(matter.getMatterType());
                if (targetFluid == net.minecraft.world.level.material.Fluids.EMPTY) continue;

                // Find the local tank that accepts this fluid
                for (BasicFluidTank localTank : getMatterTanks()) {
                    int space = localTank.getCapacity() - localTank.getFluidAmount();
                    if (space <= 0) continue;

                    // Check if this tank accepts the fluid (uses the == validator set in getInitialFluidTanks)
                    net.neoforged.neoforge.fluids.FluidStack probe =
                        new net.neoforged.neoforge.fluids.FluidStack(targetFluid, 1);
                    if (!localTank.isFluidValid(probe)) continue;

                    // How much to transfer
                    int toTransfer = Math.min(space, (int) Math.ceil(matter.getAmount()));
                    if (toTransfer <= 0) continue;

                    // Simulate drain from source, then fill + drain execute
                    com.buuz135.replication.api.matter_fluid.MatterStack simDrain =
                        sourceTank.drain(toTransfer, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE);
                    if (simDrain == null || simDrain.isEmpty() || simDrain.getAmount() <= 0) break;

                    int drainAmt = (int) Math.ceil(simDrain.getAmount());
                    net.neoforged.neoforge.fluids.FluidStack fillStack =
                        new net.neoforged.neoforge.fluids.FluidStack(targetFluid, drainAmt);
                    int filled = localTank.fill(fillStack, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) {
                        sourceTank.drain(filled, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    }
                    break; // Found matching local tank — no need to check others for this source tank
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

        // タンクに空きがある場合、ネットワークからマターをpull（10tick毎）
        com.buuz135.replication.network.MatterNetwork pullNetwork = getNetwork();
        if (pullNetwork != null && this.ticker % 10 == 0) {
            pullMatterFromNetwork(pullNetwork);
        }


        if (activeTask != null && inputSlot != null) {
            ItemStack currentInput = inputSlot.getStack();
            if (currentInput.isEmpty() || !ItemStack.isSameItemSameComponents(currentInput, activeCraftingStack)) {
                cancelActiveTask();
                sendUpdate = true;
            } else if (currentInput.getCount() != 1) {
                currentInput.setCount(1);
            }
        }

        ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        long energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);

        boolean canOperate = false;
        ItemStack inputStack = inputSlot.getStack();
        // sourceStack: the item being replicated (from inputSlot for manual, activeCraftingStack for task)
        ItemStack sourceStack = activeTask != null ? activeCraftingStack : inputStack;
        MatterCompound recipeCompound = null;

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

        // Restore active task from UUID if level loaded/reloaded
        if (network != null && activeTaskUuid != null && activeTask == null) {
            com.buuz135.replication.api.task.IReplicationTask task = network.getTaskManager().getPendingTasks().get(activeTaskUuid);
            if (task != null) {
                activeTask = task;
                activeCraftingStack = task.getReplicatingStack();
                if (inputSlot != null) {
                    inputSlot.setStackUnchecked(activeCraftingStack.copyWithCount(1));
                }
            } else {
                cancelActiveTask();
                sendUpdate = true;
            }
        }

        if (canFunction()) {
            if (!inputStack.isEmpty() && activeTask == null) {
                // Manual Replication Mode (no active network task)
                recipeCompound = ReplicationCalculation.getMatterCompound(inputStack);
            } else if (network != null && this.ticker % 4 == 0) {
                // Automatic Replication Network Mode - task management
                if (activeTask == null) {
                    ItemStack outputStack = outputSlot.getStack();
                    if (outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize()) {
                        // findTaskForReplicator の計算が不安定なため直接イテレート
                        com.buuz135.replication.api.task.IReplicationTask task = null;
                        for (com.buuz135.replication.api.task.IReplicationTask candidate :
                                network.getTaskManager().getPendingTasks().values()) {
                            // canAcceptReplicator(pos, 1): 既にこのposが割当済みでなく、かつ未割当のタスクを取得
                            if (candidate.canAcceptReplicator(getBlockPos(), 1)) {
                                task = candidate;
                                break;
                            }
                        }
                        if (task != null) {
                            task.acceptReplicator(getBlockPos());
                            activeTask = task;
                            activeTaskUuid = task.getUuid().toString();
                            // .copy() でライブ参照によるアイテム不一致を防ぐ
                            activeCraftingStack = task.getReplicatingStack().copy();
                            if (inputSlot != null) {
                                inputSlot.setStackUnchecked(activeCraftingStack.copyWithCount(1));
                            }
                            network.onTaskValueChanged(task, (net.minecraft.server.level.ServerLevel) level);
                            sendUpdate = true;
                        }
                    }
                } else {
                    // Check if task was cancelled externally
                    if (!network.getTaskManager().getPendingTasks().containsKey(activeTaskUuid)) {
                        cancelActiveTask();
                        sendUpdate = true;
                    }
                }
            }

            // For task mode, get compound from the task's target item
            if (activeTask != null && !activeCraftingStack.isEmpty() && recipeCompound == null) {
                recipeCompound = ReplicationCalculation.getMatterCompound(activeCraftingStack);
            }

            // Check if tanks have enough matter (same logic for both modes)
            if (!sourceStack.isEmpty() && recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
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
                    ItemStack copyStack = sourceStack.copyWithCount(1);
                    boolean outputCompatible = outputStack.isEmpty() || (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize());
                    if (outputCompatible && energyContainer.getEnergy() >= energyUsage) {
                        canOperate = true;
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

                if (recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
                    // Drain matter from own tanks (same for both modes)
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

                    if (activeTask != null) {
                        // Task Mode Finalize: notify network, send item to terminal
                        activeTask.finalizeReplication(level, getBlockPos(), network);
                        network.onTaskValueChanged(activeTask, (net.minecraft.server.level.ServerLevel) level);

                        BlockPos source = activeTask.getSource();
                        ItemStack copyStack = activeCraftingStack.copyWithCount(1);

                        if (!getBlockPos().equals(source)) {
                            net.neoforged.neoforge.items.IItemHandler itemHandler = level.getCapability(
                                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                                source,
                                Direction.UP
                            );
                            if (itemHandler != null) {
                                ItemStack remaining = net.neoforged.neoforge.items.ItemHandlerHelper.insertItem(itemHandler, copyStack, false);
                                if (!remaining.isEmpty()) {
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
                            ItemStack outputStack = outputSlot.getStack();
                            if (outputStack.isEmpty()) {
                                outputSlot.setStack(copyStack);
                            } else if (ItemStack.isSameItemSameComponents(outputStack, copyStack) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize()) {
                                outputSlot.growStack(1, Action.EXECUTE);
                            } else {
                                net.minecraft.world.Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY() + 1, getBlockPos().getZ(), copyStack);
                            }
                        }

                        cancelActiveTask();
                    } else {
                        // Manual Mode Finalize: output to own slot
                        int outputCount = 1;
                        if (getComponent() != null && getComponent().isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
                            outputCount = 2;
                        }

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
        if (tag.contains("activeTaskUuid")) {
            this.activeTaskUuid = tag.getString("activeTaskUuid");
        } else {
            this.activeTaskUuid = null;
        }
        if (tag.contains("activeCraftingStack")) {
            this.activeCraftingStack = ItemStack.parse(registries, tag.getCompound("activeCraftingStack")).orElse(ItemStack.EMPTY);
        } else {
            this.activeCraftingStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("operatingTicks", this.operatingTicks);
        if (this.activeTaskUuid != null) {
            tag.putString("activeTaskUuid", this.activeTaskUuid);
        }
        if (!this.activeCraftingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            this.activeCraftingStack.save(registries, stackTag);
            tag.put("activeCraftingStack", stackTag);
        }
    }

    public InputInventorySlot getInputSlot() {
        return inputSlot;
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
