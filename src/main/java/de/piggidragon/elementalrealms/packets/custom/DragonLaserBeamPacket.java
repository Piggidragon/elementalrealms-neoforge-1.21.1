package de.piggidragon.elementalrealms.packets.custom;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.ModStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record DragonLaserBeamPacket(
        int dragonId,
        Vec3 startPos,
        Vec3 endPos
) implements CustomPacketPayload {

    public static final Type<DragonLaserBeamPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "dragon_laser_beam")
            );

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
