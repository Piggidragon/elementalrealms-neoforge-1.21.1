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
 * Client -> server request to destroy blocks inside the laser impact bubble.
 */
public record LaserBeamDestroyBlockPacket(
        Vec3 center,
        float radius
) implements CustomPacketPayload {

    public static final Type<LaserBeamDestroyBlockPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "laser_beam_destroy_block"));

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
