package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.ModStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Packet sent from Client to Server to request block destruction at a specific location.
 * This corresponds to the end position of the LaserBeamTask.
 *
 * @param center The center coordinates of the explosion/destruction bubble.
 * @param radius The radius within which blocks should be destroyed.
 */
public record LaserBeamDestroyBlockPacket(
        Vec3 center,
        float radius
) implements CustomPacketPayload {

    /**
     * Unique identifier for this packet type.
     */
    public static final Type<LaserBeamDestroyBlockPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "laser_beam_destroy_block"));

    /**
     * StreamCodec for serializing and deserializing the packet data.
     * Uses manual Vec3 reading/writing via double primitives.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, LaserBeamDestroyBlockPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ModStreamCodecs.VEC3_STREAM_CODEC,
                    LaserBeamDestroyBlockPacket::center,
                    ByteBufCodecs.FLOAT,
                    LaserBeamDestroyBlockPacket::radius,
                    LaserBeamDestroyBlockPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}