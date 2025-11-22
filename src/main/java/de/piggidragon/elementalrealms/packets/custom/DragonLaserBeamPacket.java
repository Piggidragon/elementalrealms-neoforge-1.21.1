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
 * Network packet for rendering dragon laser beam on client.
 * Sent from server to client to visualize laser attack.
 *
 * @param dragonId Dragon entity ID for tracking
 * @param startPos Beam start position (dragon head)
 * @param endPos   Beam end position (target)
 */
public record DragonLaserBeamPacket(
        int dragonId,
        Vec3 startPos,
        Vec3 endPos
) implements CustomPacketPayload {

    /**
     * Unique packet type identifier for network routing.
     */
    public static final Type<DragonLaserBeamPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "dragon_laser_beam")
            );

    /**
     * Codec for network serialization.
     * Encodes dragon ID and beam coordinates for transmission.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonLaserBeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    DragonLaserBeamPacket::dragonId,
                    ModStreamCodecs.VEC3_STREAM_CODEC,
                    DragonLaserBeamPacket::startPos,
                    ModStreamCodecs.VEC3_STREAM_CODEC,
                    DragonLaserBeamPacket::endPos,
                    DragonLaserBeamPacket::new
            );

    // Returns packet type for network routing
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
