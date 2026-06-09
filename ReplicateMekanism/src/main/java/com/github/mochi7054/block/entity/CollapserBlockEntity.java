package com.github.mochi7054.block.entity;

import com.github.mochi7054.ReplicateMekanism;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.MatterValue;
import com.buuz135.replication.calculation.ReplicationCalculation;
import com.github.mochi7054.fluid.MatterFluidWrapper;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IExtendedFluidTank;
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

/**
 * 崩壊機 (Collapser) - アイテムをマターに変換するブロックエンティティ。
 * 左スロットにアイテムを挿入し、電力を消費してマター変換を行う。
 * 変換されたマターは右側のタンクに出力される。
 */
public class CollapserBlockEntity extends TileEntityConfigurableMachine implements MenuProvider {

    public static final int BASE_TICKS_REQUIRED = 100;
    public static final long BASE_ENERGY_USAGE = 50L;

    public int operatingTicks = 0;
    public int ticksRequired = BASE_TICKS_REQUIRED;

    private MachineEnergyContainer<CollapserBlockEntity> energyContainer;

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

    private InputInventorySlot inputSlot;
    private EnergyInventorySlot energySlot;

    public CollapserBlockEntity(BlockPos pos, BlockState state) {
        super(ReplicateMekanism.COLLAPSER, pos, state);
        // ITEM config: input only, no output (null は内部NPEの可能性ありのでList版を使用)
        configComponent.setupItemIOConfig(
            java.util.Collections.singletonList(inputSlot),  // item input
            java.util.Collections.emptyList(),                // no item output
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
        // 各タンクはそれぞれのマター流体のみ受け入れ (出力として機能)
        earthTank    = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.EARTH_MATTER.source.get(), listener);
        netherTank   = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.NETHER_MATTER.source.get(), listener);
        organicTank  = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.ORGANIC_MATTER.source.get(), listener);
        enderTank    = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.ENDER_MATTER.source.get(), listener);
        metallicTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.METALLIC_MATTER.source.get(), listener);
        preciousTank = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.PRECIOUS_MATTER.source.get(), listener);
        livingTank   = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.LIVING_MATTER.source.get(), listener);
        quantumTank  = BasicFluidTank.create(10000, stack -> stack.getFluid() == ReplicateMekanism.QUANTUM_MATTER.source.get(), listener);

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
        // 変換できるアイテムのみ入力スロットに入れられる
        inputSlot = InputInventorySlot.at(stack -> {
            MatterCompound compound = ReplicationCalculation.getMatterCompound(stack);
            return compound != null && !compound.getValues().isEmpty();
        }, listener, 16, 35);
        energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 141, 35);

        builder.addSlot(inputSlot);
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

        if (canFunction() && !inputStack.isEmpty()) {
            MatterCompound recipeCompound = ReplicationCalculation.getMatterCompound(inputStack);

            if (recipeCompound != null && !recipeCompound.getValues().isEmpty()) {
                // 全マタータンクにスペースがあるか確認
                boolean allTanksHaveSpace = true;
                for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
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
                // アイテムをマターに変換して各タンクへ出力
                MatterCompound recipeCompound = ReplicationCalculation.getMatterCompound(inputStack);
                if (recipeCompound != null) {
                    for (Map.Entry<IMatterType, MatterValue> entry : recipeCompound.getValues().entrySet()) {
                        IMatterType matterType = entry.getKey();
                        int amount = (int) Math.ceil(entry.getValue().getAmount());
                        BasicFluidTank targetTank = getTankForMatterType(matterType);
                        if (targetTank != null) {
                            Fluid fluid = MatterFluidWrapper.getFluidFromMatterType(matterType);
                            if (fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                                targetTank.fill(new FluidStack(fluid, amount),
                                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                    // 入力スロットから1個消費
                    inputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
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

    /**
     * MatterTypeに対応するタンクを返す。
     */
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

    public InputInventorySlot getInputSlot() {
        return inputSlot;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.replicatemekanism.collapser");
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

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
}
