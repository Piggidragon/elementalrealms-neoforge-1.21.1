package de.piggidragon.elementalrealms.packets.custom.affinities;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Server -> client trigger for the totem pop animation when an affinity stone is consumed.
 */
public record AffinitiesSuccessPacket(
        ItemStack itemStack,
        Affinity affinity
) implements CustomPacketPayload {

    public static final Type<AffinitiesSuccessPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_success"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AffinitiesSuccessPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    AffinitiesSuccessPacket::itemStack,
                    ByteBufCodecs.INT.map(i -> Affinity.values()[i], Affinity::ordinal),
                    AffinitiesSuccessPacket::affinity,
                    AffinitiesSuccessPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
