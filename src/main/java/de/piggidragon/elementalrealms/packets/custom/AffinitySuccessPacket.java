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
 * Network packet triggering client-side visual effects after affinity changes.
 * Sent from server to client when affinity stone is successfully consumed.
 *
 * @param itemStack The consumed affinity stone
 * @param affinity  The affinity granted/removed
 */
public record AffinitySuccessPacket(
        ItemStack itemStack,
        Affinity affinity
) implements CustomPacketPayload {

    /**
     * Unique packet type identifier for network routing.
     */
    public static final Type<AffinitySuccessPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "affinity_success")
            );

    /**
     * Codec for network serialization.
     * Converts ItemStack and Affinity enum to bytes for transmission.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, AffinitySuccessPacket> CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    AffinitySuccessPacket::itemStack,
                    ByteBufCodecs.INT.map(
                            i -> Affinity.values()[i],   // Deserialize ordinal to enum
                            Affinity::ordinal            // Serialize enum to ordinal
                    ),
                    AffinitySuccessPacket::affinity,
                    AffinitySuccessPacket::new
            );

    /**
     * Returns packet type for network routing.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
