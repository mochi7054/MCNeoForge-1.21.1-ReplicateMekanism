package com.github.mochi7054.mixin;

import com.buuz135.replication.api.network.IMatterTanksConsumer;
import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.github.mochi7054.imaginator.ImaginatorBlockEntity;
import com.github.mochi7054.fluid.MekanismMatterTank;
import com.github.mochi7054.ReplicateMekanism;
import mekanism.common.capabilities.fluid.BasicFluidTank;
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

    @Shadow public BasicFluidTank earthTank;
    @Shadow public BasicFluidTank netherTank;
    @Shadow public BasicFluidTank organicTank;
    @Shadow public BasicFluidTank enderTank;
    @Shadow public BasicFluidTank metallicTank;
    @Shadow public BasicFluidTank preciousTank;
    @Shadow public BasicFluidTank livingTank;
    @Shadow public BasicFluidTank quantumTank;

    public List<? extends IMatterTank> matter$getTanks() {
        return List.of(
            new MekanismMatterTank(earthTank, ReplicateMekanism.EARTH_MATTER.source.get()),
            new MekanismMatterTank(netherTank, ReplicateMekanism.NETHER_MATTER.source.get()),
            new MekanismMatterTank(organicTank, ReplicateMekanism.ORGANIC_MATTER.source.get()),
            new MekanismMatterTank(enderTank, ReplicateMekanism.ENDER_MATTER.source.get()),
            new MekanismMatterTank(metallicTank, ReplicateMekanism.METALLIC_MATTER.source.get()),
            new MekanismMatterTank(preciousTank, ReplicateMekanism.PRECIOUS_MATTER.source.get()),
            new MekanismMatterTank(livingTank, ReplicateMekanism.LIVING_MATTER.source.get()),
            new MekanismMatterTank(quantumTank, ReplicateMekanism.QUANTUM_MATTER.source.get())
        );
    }

    public int matter$getPriority() {
        return 0;
    }
}
