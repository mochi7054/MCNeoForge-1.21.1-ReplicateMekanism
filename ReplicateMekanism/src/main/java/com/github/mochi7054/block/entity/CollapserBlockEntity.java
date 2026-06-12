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
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    public int[] operatingTicks;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    /** 自動分配（ソート）が有効かどうか。BASIC以上のティアのみ機能する。 */
    public boolean sorting = false;

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
        this.operatingTicks = new int[slotCount];
        
        int[][] inputCoords = new int[slotCount][2];
        int energyX;
        int energyY;
        if (tier == ReplicaTier.STANDARD) {
            inputCoords[0][0] = 16;
            inputCoords[0][1] = 40;
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
            }
            energyX = 10;
            energyY = 17;
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

        boolean[] canOperate = new boolean[inputSlots.size()];
        MatterCompound[] slotCompounds = new MatterCompound[inputSlots.size()];

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

                        if (allTanksHaveSpace) {
                            canOperate[i] = true;
                            slotCompounds[i] = compound;
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
                        MatterCompound compound = slotCompounds[i];
                        if (compound != null) {
                            for (Map.Entry<IMatterType, MatterValue> entry : compound.getValues().entrySet()) {
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
                            inputSlots.get(i).extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
                        }
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

        // 自動分配: BASIC以上のティアで sorting が有効なら毎 tick 実行
        if (sorting && inputSlots.size() > 1) {
            sortInventory();
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

    // ---- 自動分配 ----

    public void setSorting(boolean value) {
        this.sorting = value;
        setChanged();
    }

    /**
     * 全入力スロットのアイテムを均等に分配する（Mekanism Factory の sortInventory に相当）。
     * 同じアイテムをできるだけ均等に各スロットへ振り分け、余りは先頭スロットから順に入れる。
     */
    private void sortInventory() {
        if (inputSlots == null || inputSlots.size() <= 1) return;

        // 全スロットのアイテムを集める
        java.util.List<ItemStack> collected = new java.util.ArrayList<>();
        for (mekanism.common.inventory.slot.InputInventorySlot slot : inputSlots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                collected.add(stack.copy());
                slot.setStack(ItemStack.EMPTY);
            }
        }
        if (collected.isEmpty()) return;

        // アイテム種別ごとに合計数を集計
        java.util.Map<net.minecraft.core.component.DataComponentPatch, java.util.AbstractMap.SimpleEntry<ItemStack, Integer>> countMap =
                new java.util.LinkedHashMap<>();
        for (ItemStack stack : collected) {
            var key = stack.getComponentsPatch();
            var entry = countMap.get(key);
            if (entry == null) {
                countMap.put(key, new java.util.AbstractMap.SimpleEntry<>(stack.copyWithCount(1), stack.getCount()));
            } else {
                entry.setValue(entry.getValue() + stack.getCount());
            }
        }

        int slotCount = inputSlots.size();

        // 各アイテム種別の情報を格納する一時クラス
        class SortingGroup {
            final ItemStack template;
            final int total;
            final int maxPerStack;
            int allocatedSlots;

            SortingGroup(ItemStack template, int total) {
                this.template = template;
                this.total = total;
                this.maxPerStack = template.getMaxStackSize();
                this.allocatedSlots = (total + maxPerStack - 1) / maxPerStack;
            }

            int getAverage() {
                return (total + allocatedSlots - 1) / allocatedSlots;
            }
        }

        java.util.List<SortingGroup> groups = new java.util.ArrayList<>();
        int totalAllocated = 0;
        for (var entry : countMap.values()) {
            SortingGroup group = new SortingGroup(entry.getKey(), entry.getValue());
            groups.add(group);
            totalAllocated += group.allocatedSlots;
        }

        // 防御的コード：もし必要なスロット数の合計が総スロット数を超えている場合
        if (totalAllocated > slotCount) {
            totalAllocated = 0;
            for (SortingGroup group : groups) {
                group.allocatedSlots = 1;
                totalAllocated += 1;
            }
            if (totalAllocated > slotCount) {
                while (groups.size() > slotCount) {
                    groups.remove(groups.size() - 1);
                }
                totalAllocated = groups.size();
            }
        }

        // 残りのスロットを、一番スロットあたりの個数が多いアイテムに優先して均等に分配する
        while (totalAllocated < slotCount) {
            SortingGroup bestGroup = null;
            int maxAverage = -1;
            for (SortingGroup group : groups) {
                int avg = group.getAverage();
                if (avg > maxAverage) {
                    maxAverage = avg;
                    bestGroup = group;
                }
            }
            if (bestGroup != null) {
                bestGroup.allocatedSlots++;
                totalAllocated++;
            } else {
                break;
            }
        }

        // 分配したスロット数に従って、アイテムをスロットにセットする
        int slotIdx = 0;
        for (SortingGroup group : groups) {
            int total = group.total;
            int slotsToUse = group.allocatedSlots;
            int perSlot = total / slotsToUse;
            int remainder = total % slotsToUse;

            for (int i = 0; i < slotsToUse && slotIdx < slotCount; i++, slotIdx++) {
                int count = perSlot + (i < remainder ? 1 : 0);
                if (count <= 0) continue;
                ItemStack toSet = group.template.copyWithCount(Math.min(count, group.maxPerStack));
                inputSlots.get(slotIdx).setStack(toSet);
            }
        }
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
        if (this.operatingTicks != null) {
            for (int i = 0; i < this.operatingTicks.length; i++) {
                final int idx = i;
                container.track(SyncableInt.create(() -> this.operatingTicks[idx], value -> this.operatingTicks[idx] = value));
            }
        }
        container.track(SyncableInt.create(() -> ticksRequired, value -> ticksRequired = value));
        container.track(SyncableBoolean.create(() -> this.sorting, value -> this.sorting = value));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
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
        if (tag.contains("sorting")) {
            this.sorting = tag.getBoolean("sorting");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.operatingTicks != null) {
            tag.putIntArray("operatingTicksArray", this.operatingTicks);
            if (this.operatingTicks.length > 0) {
                tag.putInt("operatingTicks", this.operatingTicks[0]);
            }
        }
        tag.putBoolean("sorting", this.sorting);
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
            this.operatingTicks != null ? this.operatingTicks.clone() : new int[0]
        );
    }

    public static class CollapserUpgradeData implements mekanism.common.upgrade.IUpgradeData {
        public final long energy;
        public final List<ItemStack> inputStacks;
        public final ItemStack energySlotStack;
        public final List<FluidStack> fluidStacks;
        public final CompoundTag componentNbt;
        public final int[] operatingTicks;
        
        public CollapserUpgradeData(long energy, List<ItemStack> inputStacks, ItemStack energySlotStack, List<FluidStack> fluidStacks, CompoundTag componentNbt, int[] operatingTicks) {
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
