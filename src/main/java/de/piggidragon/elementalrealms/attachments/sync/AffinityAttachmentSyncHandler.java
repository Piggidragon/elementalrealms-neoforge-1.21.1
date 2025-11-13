package de.piggidragon.elementalrealms.attachments.sync;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AffinityAttachmentSyncHandler implements AttachmentSyncHandler<Map<Affinity, Integer>> {

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, Map<Affinity, Integer> affinityIntegerMap, boolean b) {
        // Write map size
        registryFriendlyByteBuf.writeInt(affinityIntegerMap.size());

        // Write each entry: Affinity (as string) + completion (as int)
        for (Map.Entry<Affinity, Integer> entry : affinityIntegerMap.entrySet()) {
            registryFriendlyByteBuf.writeUtf(entry.getKey().name()); // Write affinity name
            registryFriendlyByteBuf.writeInt(entry.getValue());      // Write completion percentage
        }
    }

    @Override
    public @Nullable Map<Affinity, Integer> read(IAttachmentHolder iAttachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable Map<Affinity, Integer> affinityIntegerMap) {
        // Read map size
        int size = registryFriendlyByteBuf.readInt();

        // Create new map
        Map<Affinity, Integer> map = new HashMap<>();

        // Read each entry
        for (int i = 0; i < size; i++) {
            String affinityName = registryFriendlyByteBuf.readUtf();           // Read affinity name
            int completion = registryFriendlyByteBuf.readInt();                 // Read completion

            try {
                Affinity affinity = Affinity.valueOf(affinityName); // Convert string to enum
                map.put(affinity, completion);
            } catch (IllegalArgumentException e) {
                // Skip invalid affinity (shouldn't happen if data is valid)
                throw new RuntimeException("Unknown affinity: " + affinityName, e);
            }
        }

        return map;
    }
}
