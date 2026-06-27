package de.piggidragon.elementalrealms.packets.custom.enderdragon;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.ModStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Server -> client instruction to render the dragon laser beam.
 */
public record EnderDragonLaserBeamPacket(
        int dragonId,
        Vec3 startPos,
        Vec3 endPos
) implements CustomPacketPayload {

    public static final Type<EnderDragonLaserBeamPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "dragon_laser_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnderDragonLaserBeamPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    EnderDragonLaserBeamPacket::dragonId,
                    ModStreamCodecs.VEC3_STREAM_CODEC,
                    EnderDragonLaserBeamPacket::startPos,
                    ModStreamCodecs.VEC3_STREAM_CODEC,
                    EnderDragonLaserBeamPacket::endPos,
                    EnderDragonLaserBeamPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
