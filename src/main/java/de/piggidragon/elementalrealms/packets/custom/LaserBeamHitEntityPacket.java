package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet sent from client to server when laser beam hits an entity.
 * Requests damage application on server side for authoritative hit detection.
 *
 * @param hitEntityID   The ID of the entity that was hit
 * @param damageAmount  Amount of damage to apply
 */
public record LaserBeamHitEntityPacket(
        int hitEntityID,
        float damageAmount
) implements CustomPacketPayload {

    /**
     * Unique packet type identifier for network routing.
     */
    public static final Type<LaserBeamHitEntityPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "hit_entity")
            );

    /**
     * Codec for network serialization.
     * Encodes entity ID and damage amount for transmission.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, LaserBeamHitEntityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    LaserBeamHitEntityPacket::hitEntityID,
                    ByteBufCodecs.FLOAT,
                    LaserBeamHitEntityPacket::damageAmount,
                    LaserBeamHitEntityPacket::new
            );

    // Returns packet type for network routing
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
