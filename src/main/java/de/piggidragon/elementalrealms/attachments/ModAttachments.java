package de.piggidragon.elementalrealms.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.SimpleMapCodec;
import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Defines persistent data attachments for entities and levels.
 */
public class ModAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPE = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            ElementalRealms.MODID);

    /**
     * Player's magical affinities. Persists through death.
     */
    public static final Supplier<AttachmentType<List<Affinity>>> AFFINITIES = ATTACHMENT_TYPE.register(
            "affinities",
            () -> AttachmentType.<List<Affinity>>builder(() -> new ArrayList<>())
                    .serialize(
                            Codec.list(Affinity.CODEC)
                                    .fieldOf("affinities")
                                    .xmap(ArrayList::new, list -> list)
                    )
                    .copyOnDeath() // Preserve affinities when player dies
                    .build()
    );

    /**
     * Codec for serializing Vec3 positions to NBT.
     */
    public static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
            ).apply(instance, Vec3::new)
    );

    /**
     * Codec for Level ResourceKeys.
     */
    static Codec<ResourceKey<Level>> resourceKeyCodec = ResourceKey.codec(Registries.DIMENSION);

    /**
     * Provides dimension keys for map codec.
     */
    static Supplier<Stream<String>> keys = () -> Stream.of(
            "minecraft:overworld",
            "minecraft:the_nether",
            "minecraft:the_end"
    );

    /**
     * SimpleMapCodec for storing positions per dimension.
     */
    public static final SimpleMapCodec<ResourceKey<Level>, Vec3> VEC3_MAP_CODEC = Codec.simpleMap(
            resourceKeyCodec,
            VEC3_CODEC,
            Keyable.forStrings(keys)
    );

    /**
     * Stores the return position and dimension for inter-dimensional travel.
     */
    public static final Supplier<AttachmentType<Map<ResourceKey<Level>, Vec3>>> RETURN_LEVEL_POS = ATTACHMENT_TYPE.register(
            "return_level_pos",
            () -> AttachmentType.builder(() -> Map.of(Level.OVERWORLD, Vec3.ZERO))
                    .serialize(VEC3_MAP_CODEC.fieldOf("return_level_pos"))
                    .build()
    );

    public static final Supplier<AttachmentType<ResourceKey<Level>>> PORTAL_TARGET_LEVEL = ATTACHMENT_TYPE.register(
            "portal_target_level",
            () -> AttachmentType.builder(() -> Level.OVERWORLD)
                    .serialize(resourceKeyCodec.fieldOf("portal_target_level"))
                    .build()
    );

    /**
     * Registers all attachment types with the mod event bus.
     *
     * @param modBus The mod's event bus for registration
     */
    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPE.register(modBus);
    }
}
