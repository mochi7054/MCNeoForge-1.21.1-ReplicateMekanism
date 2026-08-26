package com.github.mochi7054.forensic;

import com.buuz135.replication.api.pattern.IMatterPatternModifier;
import com.buuz135.replication.api.pattern.IMatterPatternModifier.ModifierAction;
import com.buuz135.replication.api.pattern.IMatterPatternModifier.ModifierType;
import com.github.mochi7054.ReplicateMekanism;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ForensicChamberBlockEntity extends TileEntityConfigurableMachine {

    public static final long SCAN_ENERGY_COST = 10_000L; // 10,000 J (4,000 FE)
    public static final long MAX_ENERGY = 40_000L;

    public MachineEnergyContainer<ForensicChamberBlockEntity> energyContainer;
    public BasicInventorySlot inputSlot;
    public BasicInventorySlot chipInputSlot;
    public OutputInventorySlot chipOutputSlot;
    public EnergyInventorySlot energySlot;

    public ForensicChamberBlockEntity(BlockPos pos, BlockState state) {
        super(state.getBlockHolder(), pos, state);

        configComponent.setupItemIOExtraConfig(inputSlot, chipOutputSlot, chipInputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
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
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);

        inputSlot = BasicInventorySlot.at(stack -> true, listener, 44, 35);
        chipInputSlot = BasicInventorySlot.at(stack -> stack.getItem() instanceof IMatterPatternModifier, listener, 80, 20);
        chipOutputSlot = OutputInventorySlot.at(listener, 116, 35);
        energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35);

        builder.addSlot(inputSlot);
        builder.addSlot(chipInputSlot);
        builder.addSlot(chipOutputSlot);
        builder.addSlot(energySlot);

        return builder.build();
    }

    public void tryScan(ServerPlayer player) {
        if (level == null || level.isClientSide) return;

        ItemStack inputStack = inputSlot.getStack();
        ItemStack chipStack = chipInputSlot.getStack();

        if (inputStack.isEmpty() || chipStack.isEmpty()) {
            return;
        }

        if (energyContainer.getEnergy() < SCAN_ENERGY_COST) {
            return;
        }

        if (!(chipStack.getItem() instanceof IMatterPatternModifier modifier)) {
            return;
        }

        ItemStack chipCopy = chipStack.copy();
        chipCopy.setCount(1);

        // Execute 100% pattern identification
        ModifierAction action = (ModifierAction) modifier.addPattern(level, chipCopy, inputStack, 1.0f);
        if (action != null && action.getType() == ModifierType.FULL) {
            ItemStack outputStack = chipOutputSlot.getStack();
            if (outputStack.isEmpty()) {
                chipOutputSlot.setStack(chipCopy);
            } else if (ItemStack.isSameItemSameComponents(outputStack, chipCopy) && outputStack.getCount() + 1 <= outputStack.getMaxStackSize()) {
                outputStack.grow(1);
            } else {
                // Output slot is full or mismatch
                return;
            }

            // Consume 1 chip, 1 target item, and energy
            chipInputSlot.shrinkStack(1, Action.EXECUTE);
            inputSlot.shrinkStack(1, Action.EXECUTE);
            energyContainer.extract(SCAN_ENERGY_COST, Action.EXECUTE, AutomationType.INTERNAL);

            // Play success sound
            level.playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.5f);
            markForSave();
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean superRet = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        return superRet;
    }
}