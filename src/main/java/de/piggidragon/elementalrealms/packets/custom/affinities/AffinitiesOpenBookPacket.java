package de.piggidragon.elementalrealms.packets.custom.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to open the affinity book menu.
 */
public record AffinitiesOpenBookPacket() implements CustomPacketPayload {

    public static final Type<AffinitiesOpenBookPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_affinity_book"));

    public static final StreamCodec<ByteBuf, AffinitiesOpenBookPacket> STREAM_CODEC =
            StreamCodec.unit(new AffinitiesOpenBookPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
