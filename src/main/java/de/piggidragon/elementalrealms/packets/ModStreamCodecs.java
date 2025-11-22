package de.piggidragon.elementalrealms.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

/**
 * Custom stream codecs for encoding/decoding complex data types in network packets.
 */
public class ModStreamCodecs {
    /**
     * Codec for serializing Vec3 positions to network buffers.
     * Writes/reads x, y, z as doubles for precise coordinate transmission.
     */
    public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(
            (buf, vec) -> {
                buf.writeDouble(vec.x);
                buf.writeDouble(vec.y);
                buf.writeDouble(vec.z);
            },
            buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );
}
