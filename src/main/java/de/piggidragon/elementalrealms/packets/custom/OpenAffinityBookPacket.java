package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to open the affinity book menu.
 */
public record OpenAffinityBookPacket() implements CustomPacketPayload {

    public static final Type<OpenAffinityBookPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_affinity_book"));

    public static final StreamCodec<ByteBuf, OpenAffinityBookPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenAffinityBookPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
