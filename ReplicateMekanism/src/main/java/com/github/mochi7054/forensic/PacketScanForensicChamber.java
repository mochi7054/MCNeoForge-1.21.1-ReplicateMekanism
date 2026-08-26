package com.github.mochi7054.forensic;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketScanForensicChamber(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketScanForensicChamber> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("replicatemekanism", "scan_forensic_chamber"));
    
    public static final StreamCodec<ByteBuf, PacketScanForensicChamber> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PacketScanForensicChamber::pos,
            PacketScanForensicChamber::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketScanForensicChamber payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                var level = player.level();
                if (level.getBlockEntity(payload.pos()) instanceof ForensicChamberBlockEntity forensicChamber) {
                    forensicChamber.tryScan(player);
                }
            }
        });
    }
}
