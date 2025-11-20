package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ParticleHitEntityPacket(
        int hitEntityID,
        float damageAmount
) implements CustomPacketPayload {

    public static final Type<ParticleHitEntityPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "hit_entity")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleHitEntityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ParticleHitEntityPacket::hitEntityID,
                    ByteBufCodecs.FLOAT,
                    ParticleHitEntityPacket::damageAmount,
                    ParticleHitEntityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
