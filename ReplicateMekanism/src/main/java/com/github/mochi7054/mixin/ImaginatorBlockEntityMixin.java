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

    public List<? extends IMatterTank> matter$getTanks() {
        return ((ImaginatorBlockEntity) (Object) this).getMatterTanks();
    }

    public int matter$getPriority() {
        return 0;
    }
}
