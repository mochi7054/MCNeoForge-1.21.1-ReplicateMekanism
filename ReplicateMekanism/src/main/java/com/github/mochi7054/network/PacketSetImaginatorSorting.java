package com.github.mochi7054.network;

import com.github.mochi7054.ReplicateMekanism;
import com.github.mochi7054.block.entity.ImaginatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * クライアントからサーバーへ Imaginator の自動分配（sorting）フラグを送信するパケット。
 */
public record PacketSetImaginatorSorting(BlockPos pos, boolean sorting) implements CustomPacketPayload {

    public static final ResourceLocation ID_RL = ResourceLocation.fromNamespaceAndPath(ReplicateMekanism.MODID, "set_imaginator_sorting");
    public static final Type<PacketSetImaginatorSorting> TYPE = new Type<>(ID_RL);

    public static final StreamCodec<FriendlyByteBuf, PacketSetImaginatorSorting> CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeBlockPos(pkt.pos);
                buf.writeBoolean(pkt.sorting);
            },
            buf -> new PacketSetImaginatorSorting(buf.readBlockPos(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * サーバー側の受信ハンドラ。
     */
    public static void handle(PacketSetImaginatorSorting pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) return;
            var level = player.level();
            if (level.isClientSide()) return;

            var be = level.getBlockEntity(pkt.pos());
            if (be instanceof ImaginatorBlockEntity imaginator) {
                imaginator.setSorting(pkt.sorting());
            }
        });
    }
}
