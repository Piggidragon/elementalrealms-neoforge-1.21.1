package de.piggidragon.elementalrealms.registries.attachments.sync;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Synchronizes the player affinity attachment between server and client.
 */
public final class AffinityAttachmentSyncHandler implements AttachmentSyncHandler<Map<Affinity, Integer>> {

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, Map<Affinity, Integer> map, boolean sync) {
        buf.writeInt(map.size());
        for (Map.Entry<Affinity, Integer> entry : map.entrySet()) {
            buf.writeUtf(entry.getKey().name());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public @Nullable Map<Affinity, Integer> read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable Map<Affinity, Integer> existing) {
        int size = buf.readInt();
        Map<Affinity, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf();
            int completion = buf.readInt();
            map.put(Affinity.valueOf(name), completion);
        }
        return map;
    }
}
