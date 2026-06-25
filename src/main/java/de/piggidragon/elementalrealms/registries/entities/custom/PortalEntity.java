package de.piggidragon.elementalrealms.registries.entities.custom;

import de.piggidragon.elementalrealms.client.particles.vanilla.PortalParticles;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.util.PortalUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Stationary portal entity. Teleports intersecting players to a target dimension,
 * despawns after a tick timeout, and removes its dynamically-created dimension on cleanup.
 */
public class PortalEntity extends Entity {

    public static final double PORTAL_HEIGHT_OFFSET = 5.0;
    public static final double PORTAL_Z_OFFSET = 2.0;
    public static final double PORTAL_SEARCH_RADIUS = 128.0;

    private static final int PARTICLE_SPAWN_INTERVAL = 5;
    private static final int PARTICLE_COUNT = 3;
    private static final double PARTICLE_RADIUS = 0.8;
    private static final double PARTICLE_Y_OFFSET = 0.5;
    private static final float PORTAL_EXPLOSION_POWER = 25.0f;
    private static final double PORTAL_EXPLOSION_Y_OFFSET = 1.0;
    private static final double RETURN_OFFSET = 2.0;

    private static final String TAG_DESPAWN_TIMER = "DespawnTimer";
    private static final String TAG_DISCARD = "Discard";
    private static final String TAG_IS_NATURAL = "IsNatural";
    private static final String TAG_INITIALIZED = "Initialized";
    private static final String TAG_TARGET_LEVEL = "TargetLevel";
    private static final String TAG_OWNER_UUID = "OwnerUUID";

    private final ResourceKey<Level> portalLevel;
    private ResourceKey<Level> targetLevel;
    private UUID ownerUUID = null;
    private boolean initialized = false;
    private boolean discard = false;
    private int despawnTimeout = 0;
    private boolean primed = false;

    public PortalEntity(EntityType<? extends PortalEntity> type, Level level) {
        super(type, level);
        this.portalLevel = level.dimension();
        if (!level.isClientSide() && level.getServer() != null) {
            this.targetLevel = Level.OVERWORLD;
        }
    }

    public PortalEntity(EntityType<? extends PortalEntity> type, Level level, ResourceKey<Level> targetLevel) {
        this(type, level);
        this.targetLevel = targetLevel;
    }

    public PortalEntity(
            EntityType<? extends PortalEntity> type,
            Level level,
            boolean discard,
            int despawnTimeout,
            ResourceKey<Level> targetLevel,
            @Nullable UUID ownerUUID
    ) {
        this(type, level);
        this.discard = discard;
        this.despawnTimeout = despawnTimeout;
        this.ownerUUID = ownerUUID;
        this.targetLevel = targetLevel;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setTargetLevel(ResourceKey<Level> targetLevel) {
        this.targetLevel = targetLevel;
    }

    /**
     * Marks this portal as naturally spawned; it will explode on its first server tick.
     */
    public void prime() {
        this.primed = true;
    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.despawnTimeout = compound.getInt(TAG_DESPAWN_TIMER);
        this.discard = compound.getBoolean(TAG_DISCARD);
        this.primed = compound.getBoolean(TAG_IS_NATURAL);
        this.initialized = compound.getBoolean(TAG_INITIALIZED);

        if (compound.contains(TAG_TARGET_LEVEL)) {
            String levelKey = compound.getString(TAG_TARGET_LEVEL);
            if (!levelKey.isEmpty() && !this.level().isClientSide()) {
                ResourceLocation location = ResourceLocation.tryParse(levelKey);
                this.targetLevel = location != null
                        ? ResourceKey.create(Registries.DIMENSION, location)
                        : Level.OVERWORLD;
            }
        } else if (!this.level().isClientSide() && this.getServer() != null) {
            this.targetLevel = Level.OVERWORLD;
        }

        if (compound.contains(TAG_OWNER_UUID)) {
            try {
                this.ownerUUID = UUID.fromString(compound.getString(TAG_OWNER_UUID));
            } catch (IllegalArgumentException e) {
                this.ownerUUID = null;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(TAG_DESPAWN_TIMER, this.despawnTimeout);
        compound.putBoolean(TAG_DISCARD, this.discard);
        compound.putBoolean(TAG_IS_NATURAL, this.primed);
        compound.putBoolean(TAG_INITIALIZED, this.initialized);

        if (this.targetLevel != null) {
            compound.putString(TAG_TARGET_LEVEL, this.targetLevel.location().toString());
        }
        if (this.ownerUUID != null) {
            compound.putString(TAG_OWNER_UUID, this.ownerUUID.toString());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && !initialized && this.tickCount == 1 && primed) {
            createExplosivePortalSpace();
            this.initialized = true;
        }

        if (this.level().isClientSide()) return;

        if (despawnTimeout > 0) {
            despawnTimeout--;
            if (despawnTimeout <= 0) {
                PortalParticles.createPortalDisappearEffect((ServerLevel) this.level(), this.position());
                this.discard();
                return;
            }
        }

        if (tickCount % PARTICLE_SPAWN_INTERVAL == 0) {
            spawnAmbientParticles();
        }

        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox());
        for (ServerPlayer player : players) {
            if (!player.isSpectator()) {
                teleportPlayer(player.level(), player);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        if (!reason.equals(RemovalReason.DISCARDED) && !reason.equals(RemovalReason.KILLED)) return;
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel serverLevel)) return;

        ResourceKey<Level> targetDimension = this.getData(ModAttachments.PORTAL_TARGET_LEVEL);
        if (!targetDimension.equals(Level.OVERWORLD) && !targetDimension.equals(ModLevel.SCHOOL_DIMENSION)) {
            DynamicDimensionHandler.removeDimensionForPortal(serverLevel.getServer(), this);
        }
    }

    private void spawnAmbientParticles() {
        ServerLevel serverLevel = (ServerLevel) level();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = tickCount * 0.1 + i * Math.PI * 2 / PARTICLE_COUNT;
            double x = getX() + Math.cos(angle) * PARTICLE_RADIUS;
            double y = getY() + PARTICLE_Y_OFFSET;
            double z = getZ() + Math.sin(angle) * PARTICLE_RADIUS;
            serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
        }
    }

    private void createExplosivePortalSpace() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 center = this.position();
        serverLevel.explode(
                this,
                center.x, center.y + PORTAL_EXPLOSION_Y_OFFSET, center.z,
                PORTAL_EXPLOSION_POWER,
                Level.ExplosionInteraction.BLOCK
        );
    }

    @Nullable
    private ServerLevel getLevelFromKey(ResourceKey<Level> targetLevel) {
        MinecraftServer server = this.getServer();
        return server == null ? null : server.getLevel(targetLevel);
    }

    private void teleportPlayer(Level level, ServerPlayer player) {
        if (level.isClientSide() || targetLevel == null) return;
        if (player.isOnPortalCooldown()) {
            player.displayClientMessage(Component.literal("Portal is on cooldown!"), true);
            return;
        }

        Set<RelativeMovement> relatives = Collections.emptySet();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (PortalUtils.isVanilla(portalLevel)) {
            teleportFromVanilla(player, relatives, yaw, pitch);
        } else {
            teleportFromCustom(player, relatives, yaw, pitch);
        }
    }

    private void teleportFromVanilla(ServerPlayer player, Set<RelativeMovement> relatives, float yaw, float pitch) {
        Map<ResourceKey<Level>, Vec3> returnLevelPos = Map.of(
                player.level().dimension(),
                new Vec3(player.getX(), player.getY(), player.getZ())
        );

        ServerLevel destinationLevel = getLevelFromKey(targetLevel);
        if (destinationLevel == null) return;

        Vec3 destinationPos = resolveDestinationPosition(destinationLevel);

        player.setData(ModAttachments.RETURN_LEVEL_POS.get(), returnLevelPos);
        de.piggidragon.elementalrealms.ElementalRealms.LOGGER.info(
                "Teleporting player {} to dimension {} at position {}",
                player.getName().getString(), targetLevel.location(), destinationPos);
        player.teleportTo(destinationLevel, destinationPos.x, destinationPos.y + 1, destinationPos.z, relatives, yaw, pitch);
        player.setPortalCooldown();

        if (discard) {
            this.discard();
        }

        ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();
        PortalEntity existingPortal = PortalUtils.findNearestPortal(destinationLevel, player.position(), PORTAL_SEARCH_RADIUS);
        if (existingPortal == null) {
            PortalEntity returnPortal = new PortalEntity(
                    ModEntities.PORTAL_ENTITY.get(),
                    destinationLevel,
                    returnLevel
            );
            returnPortal.setPos(player.position().x, player.position().y + PORTAL_HEIGHT_OFFSET, player.position().z);
            destinationLevel.addFreshEntity(returnPortal);
        }
    }

    private Vec3 resolveDestinationPosition(ServerLevel destinationLevel) {
        if (targetLevel == ModLevel.SCHOOL_DIMENSION) {
            return new Vec3(-1.5, 61, 0.5);
        }
        ChunkPos spawnChunk = DynamicDimensionHandler.getGenerationCenterData()
                .getGenerationCenters()
                .get(targetLevel);
        destinationLevel.setChunkForced(spawnChunk.x, spawnChunk.z, true);
        try {
            ChunkAccess chunk = destinationLevel.getChunk(spawnChunk.x, spawnChunk.z);
            int terrainHeight = chunk.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    spawnChunk.getMiddleBlockX(),
                    spawnChunk.getMiddleBlockZ()
            );
            return new Vec3(0.5 + spawnChunk.getMiddleBlockX(), terrainHeight, 0.5 + spawnChunk.getMiddleBlockZ());
        } finally {
            destinationLevel.setChunkForced(spawnChunk.x, spawnChunk.z, false);
        }
    }

    private void teleportFromCustom(ServerPlayer player, Set<RelativeMovement> relatives, float yaw, float pitch) {
        Map<ResourceKey<Level>, Vec3> returnLevelPos = player.getData(ModAttachments.RETURN_LEVEL_POS.get());
        if (returnLevelPos == null || returnLevelPos.isEmpty()) {
            player.displayClientMessage(Component.literal("No return position found!"), true);
            return;
        }
        Vec3 returnPos = returnLevelPos.values().iterator().next();
        ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();

        player.teleportTo(getLevelFromKey(returnLevel), returnPos.x + RETURN_OFFSET, returnPos.y, returnPos.z + RETURN_OFFSET, relatives, yaw, pitch);
        player.removeData(ModAttachments.RETURN_LEVEL_POS.get());
        player.setPortalCooldown();

        if (discard) {
            this.discard();
        }
    }
}
