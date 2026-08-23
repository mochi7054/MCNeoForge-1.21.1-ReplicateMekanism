package com.github.mochi7054.mixin;

import com.buuz135.replication.api.network.IMatterTanksConsumer;
import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

import java.util.List;

@Mixin(ImaginatorBlockEntity.class)
@Implements({
    @Interface(iface = IMatterTanksConsumer.class, prefix = "matter$"),
    @Interface(iface = IMatterTanksSupplier.class, prefix = "matter$")
})
public abstract class ImaginatorBlockEntityMixin {

    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank earthTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank netherTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank organicTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank enderTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank metallicTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank preciousTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank livingTank;
    @Shadow public com.github.mochi7054.fluid.SimpleMatterTank quantumTank;

    public List<? extends IMatterTank> matter$getTanks() {
        return List.of(earthTank, netherTank, organicTank, enderTank, metallicTank, preciousTank, livingTank, quantumTank);
    }

    public int matter$getPriority() {
        return 0;
    }
}
