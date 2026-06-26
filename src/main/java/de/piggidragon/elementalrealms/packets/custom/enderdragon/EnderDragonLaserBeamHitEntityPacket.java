package de.piggidragon.elementalrealms.packets.custom.enderdragon;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to apply laser-beam damage to an entity.
 */
public record EnderDragonLaserBeamHitEntityPacket(
        int hitEntityID,
        float damageAmount
) implements CustomPacketPayload {

    public static final Type<EnderDragonLaserBeamHitEntityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "hit_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnderDragonLaserBeamHitEntityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    EnderDragonLaserBeamHitEntityPacket::hitEntityID,
                    ByteBufCodecs.FLOAT,
                    EnderDragonLaserBeamHitEntityPacket::damageAmount,
                    EnderDragonLaserBeamHitEntityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
