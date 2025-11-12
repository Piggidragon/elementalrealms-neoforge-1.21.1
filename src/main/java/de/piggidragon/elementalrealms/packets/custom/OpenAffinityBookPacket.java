package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from client to server to request opening the affinity book menu.
 * This is a simple empty packet since no data needs to be sent.
 */
public record OpenAffinityBookPacket() implements CustomPacketPayload {

    /**
     * Unique identifier for this packet type.
     */
    public static final Type<OpenAffinityBookPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_affinity_book"));

    /**
     * Codec for serializing/deserializing this packet.
     * Since the packet has no data, we use StreamCodec.unit().
     */
    public static final StreamCodec<ByteBuf, OpenAffinityBookPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenAffinityBookPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}