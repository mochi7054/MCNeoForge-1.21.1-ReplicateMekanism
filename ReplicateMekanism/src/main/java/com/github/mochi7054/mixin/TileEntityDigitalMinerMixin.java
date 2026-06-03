package com.github.mochi7054.mixin;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.mochi7054.ReplicateMekanism;

@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class TileEntityDigitalMinerMixin {

    @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
    private void onGetDrops(ServerLevel level, BlockState state, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> drops = cir.getReturnValue();
        if (drops != null && !drops.isEmpty()) {
            TileEntityDigitalMiner miner = (TileEntityDigitalMiner) (Object) this;
            if (miner.getComponent() != null && miner.getComponent().isUpgradeInstalled(ReplicateMekanism.REPLICA_UPGRADE_TYPE)) {
                List<ItemStack> doubled = new ArrayList<>();
                for (ItemStack drop : drops) {
                    if (drop != null && !drop.isEmpty()) {
                        doubled.add(drop);
                        doubled.add(drop.copy());
                    } else {
                        doubled.add(drop);
                    }
                }
                cir.setReturnValue(doubled);
            }
        }
    }
}
