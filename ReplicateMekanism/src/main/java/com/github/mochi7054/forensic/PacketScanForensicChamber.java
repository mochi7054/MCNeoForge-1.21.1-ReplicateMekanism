package com.github.mochi7054.forensic;

import com.github.mochi7054.ReplicateMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketScanForensicChamber(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketScanForensicChamber> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReplicateMekanism.MODID, "scan_forensic_chamber"));

    public static final StreamCodec<FriendlyByteBuf, PacketScanForensicChamber> CODEC = CustomPacketPayload.codec(
            PacketScanForensicChamber::write,
            PacketScanForensicChamber::new
    );

    public PacketScanForensicChamber(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(pos) instanceof ForensicChamberBlockEntity forensicChamber) {
                    forensicChamber.tryAutoScan();
                }
            }
        });
    }
}