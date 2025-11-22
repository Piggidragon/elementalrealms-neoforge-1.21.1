package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LaserBeamHitEntityPacket(
        int hitEntityID,
        float damageAmount
) implements CustomPacketPayload {

    public static final Type<LaserBeamHitEntityPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "hit_entity")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, LaserBeamHitEntityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    LaserBeamHitEntityPacket::hitEntityID,
                    ByteBufCodecs.FLOAT,
                    LaserBeamHitEntityPacket::damageAmount,
                    LaserBeamHitEntityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
