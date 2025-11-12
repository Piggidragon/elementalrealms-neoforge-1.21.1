package de.piggidragon.elementalrealms.packets;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from client to server to request opening the affinity GUI.
 * This is an empty packet (no data needed).
 */
public record OpenAffinityGuiPacket() implements CustomPacketPayload {

    // Unique identifier for this packet type
    public static final Type<OpenAffinityGuiPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_affinity_gui"));

    // Stream codec for serialization/deserialization
    // unit() means empty packet (no data to send)
    public static final StreamCodec<ByteBuf, OpenAffinityGuiPacket> CODEC =
            StreamCodec.unit(new OpenAffinityGuiPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
