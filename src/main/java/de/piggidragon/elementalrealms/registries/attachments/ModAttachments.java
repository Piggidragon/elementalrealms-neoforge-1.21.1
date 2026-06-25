package de.piggidragon.elementalrealms.registries.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.SimpleMapCodec;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.attachments.sync.AffinityAttachmentSyncHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Attachment types persisted on entities and levels.
 */
public final class ModAttachments {

    private static final Supplier<Stream<String>> DIMENSION_KEYS = () -> Stream.of(
            "minecraft:overworld",
            "minecraft:the_nether",
            "minecraft:the_end"
    );
    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
            ).apply(instance, Vec3::new)
    );
    private static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final SimpleMapCodec<ResourceKey<Level>, Vec3> VEC3_MAP_CODEC = Codec.simpleMap(
            RESOURCE_KEY_CODEC,
            VEC3_CODEC,
            Keyable.forStrings(DIMENSION_KEYS)
    );
    private static final Codec<Map<Affinity, Integer>> AFFINITY_MAP_CODEC = Codec.unboundedMap(
            Affinity.CODEC,
            Codec.INT
    );
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPE = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            ElementalRealms.MODID);
    /**
     * Player's affinity map. Persists through death.
     */
    public static final Supplier<AttachmentType<Map<Affinity, Integer>>> AFFINITIES = ATTACHMENT_TYPE.register(
            "affinities",
            () -> AttachmentType.<Map<Affinity, Integer>>builder(() -> new HashMap<>())
                    .serialize(AFFINITY_MAP_CODEC)
                    .sync(new AffinityAttachmentSyncHandler())
                    .copyOnDeath()
                    .build()
    );
    /**
     * Dimension a portal teleports the player to.
     */
    public static final Supplier<AttachmentType<ResourceKey<Level>>> PORTAL_TARGET_LEVEL = ATTACHMENT_TYPE.register(
            "portal_target_level",
            () -> AttachmentType.builder(() -> Level.OVERWORLD)
                    .serialize(RESOURCE_KEY_CODEC)
                    .build()
    );
    /**
     * Per-dimension return positions used after inter-dimensional travel.
     */
    public static final Supplier<AttachmentType<Map<ResourceKey<Level>, Vec3>>> RETURN_LEVEL_POS = ATTACHMENT_TYPE.register(
            "return_level_pos",
            () -> AttachmentType.builder(() -> Map.of(Level.OVERWORLD, Vec3.ZERO))
                    .serialize(VEC3_MAP_CODEC.codec())
                    .build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPE.register(modBus);
    }
}
