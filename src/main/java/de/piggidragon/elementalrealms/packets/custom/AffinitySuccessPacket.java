package de.piggidragon.elementalrealms.packets.custom;

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
public record AffinitySuccessPacket(
        ItemStack itemStack,
        Affinity affinity
) implements CustomPacketPayload {

    public static final Type<AffinitySuccessPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_success"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AffinitySuccessPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    AffinitySuccessPacket::itemStack,
                    ByteBufCodecs.INT.map(i -> Affinity.values()[i], Affinity::ordinal),
                    AffinitySuccessPacket::affinity,
                    AffinitySuccessPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
