package com.github.mochi7054.mixin;

import com.buuz135.replication.network.MatterNetwork;
import com.hrznstudio.titanium.block_network.element.NetworkElement;
import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = MatterNetwork.class, remap = false)
public class MatterNetworkMixin {

    @Shadow
    private List<NetworkElement> queueNetworkElements;

    @Shadow
    private List<NetworkElement> replicators;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    private void onUpdate(Level level, CallbackInfo ci) {
        for (NetworkElement element : this.queueNetworkElements) {
            if (element.getLevel() != null && element.getLevel().isLoaded(element.getPos())) {
                BlockEntity be = element.getLevel().getBlockEntity(element.getPos());
                if (be instanceof ImaginatorBlockEntity) {
                    if (!this.replicators.contains(element)) {
                        this.replicators.add(element);
                    }
                }
            }
        }
    }
}
