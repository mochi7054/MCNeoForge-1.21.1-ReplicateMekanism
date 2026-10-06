package com.github.mochi7054.mixin;

import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.github.mochi7054.collapser.CollapserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

import java.util.List;

@Mixin(CollapserBlockEntity.class)
@Implements({
    @Interface(iface = IMatterTanksSupplier.class, prefix = "matter$")
})
public abstract class CollapserBlockEntityMixin {

    public List<? extends IMatterTank> matter$getTanks() {
        return ((CollapserBlockEntity) (Object) this).getMatterTanks();
    }

    public int matter$getPriority() {
        return 0;
    }
}
