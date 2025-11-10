package de.piggidragon.elementalrealms.entities.custom;

import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.particles.PortalParticles;
import de.piggidragon.elementalrealms.util.PortalUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
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
 * Dimensional portal entity that teleports players between worlds.
 * Supports multiple variants and can be configured for automatic despawn.
 */
public class PortalEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState spawnAnimationState = new AnimationState();
    private final ResourceKey<Level> portalLevel; // Dimension where this portal exists
    private int idleAnimationTimer = -1;
    private ResourceKey<Level> targetLevel; // Dimension to teleport to
    private UUID ownerUUID = null;
    private boolean initialized = false;
    private boolean discard = false; // Remove portal after single use
    private int despawnTimeout = 0; // Ticks until automatic removal
    private boolean primed = false; // Natural spawn flag - triggers explosion

    /**
     * Creates a portal entity with default settings.
     *
     * @param type  the entity type
     * @param level the world level
     */
    public PortalEntity(EntityType<? extends PortalEntity> type, Level level) {
        super(type, level);
        this.portalLevel = level.dimension();

        if (!level.isClientSide() && level.getServer() != null) {
            this.targetLevel = Level.OVERWORLD;
        }

        if (level.isClientSide()) {
            this.spawnAnimationState.start(0);
        }
    }

    /**
     * Creates a portal entity with a specific target level.
     *
     * @param type        the entity type
     * @param level       the world level
     * @param targetLevel the dimension to teleport to
     */
    public PortalEntity(EntityType<? extends PortalEntity> type, Level level, ResourceKey<Level> targetLevel) {
        this(type, level);
        this.targetLevel = targetLevel;
    }

    /**
     * Creates a fully configured portal entity.
     *
     * @param type           the entity type
     * @param level          the world level
     * @param discard        whether to remove portal after use
     * @param despawnTimeout ticks until automatic removal (-1 for never)
     * @param targetLevel    the dimension to teleport to
     * @param ownerUUID      the UUID of the player who created this portal
     */
    public PortalEntity(EntityType<? extends PortalEntity> type, Level level, boolean discard, int despawnTimeout, ResourceKey<Level> targetLevel, @Nullable UUID ownerUUID) {
        this(type, level);
        this.discard = discard;
        this.despawnTimeout = despawnTimeout;
        this.ownerUUID = ownerUUID;
        this.targetLevel = targetLevel;
    }

    /**
     * Gets the UUID of the player who created this portal.
     *
     * @return the owner's UUID, or null if none
     */
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Nullable
    private ServerLevel getLevelfromKey(ResourceKey<Level> targetLevel) {
        MinecraftServer server = this.getServer();
        if (server == null) {
            return null;
        }
        return server.getLevel(targetLevel);
    }

    /**
     * Sets the target dimension for portal teleportation.
     *
     * @param targetLevel the dimension key to teleport to
     */
    public void setTargetLevel(ResourceKey<Level> targetLevel) {
        this.targetLevel = targetLevel;
    }

    /**
     * Marks this portal as naturally spawned, enabling explosion on first tick.
     */
    public void prime() {
        this.primed = true;
    }

    /**
     * Determines if the portal is invulnerable to damage.
     */
    @Override
    public boolean isInvulnerable() {
        return false;
    }

    /**
     * Determines if the portal can be pushed by entities.
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * Handles entity collision pushing.
     */
    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    /**
     * Determines if the portal is affected by gravity.
     */
    @Override
    public boolean isNoGravity() {
        return true;
    }

    /**
     * Handles player interaction with the portal.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Manages animation states for the portal.
     */
    public void setupAnimationStates() {
        if (this.idleAnimationTimer <= 0) {
            this.idleAnimationTimer = 160;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimer;
        }
    }

    /**
     * Reads entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.despawnTimeout = compound.getInt("DespawnTimer");
        this.discard = compound.getBoolean("Discard");
        this.primed = compound.getBoolean("IsNatural");
        this.initialized = compound.getBoolean("Initialized");

        // Parse target dimension from saved string
        if (compound.contains("TargetLevel")) {
            String levelKey = compound.getString("TargetLevel");
            if (!levelKey.isEmpty() && !this.level().isClientSide()) {
                ResourceLocation location = ResourceLocation.tryParse(levelKey);
                if (location != null) {
                    this.targetLevel = ResourceKey.create(Registries.DIMENSION, location);
                } else {
                    this.targetLevel = Level.OVERWORLD; // Fallback to overworld
                }
            }
        } else if (!this.level().isClientSide() && this.getServer() != null) {
            this.targetLevel = Level.OVERWORLD;
        }

        // Parse owner UUID from string
        if (compound.contains("OwnerUUID")) {
            String uuidString = compound.getString("OwnerUUID");
            try {
                this.ownerUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                this.ownerUUID = null;
            }
        }
    }

    /**
     * Saves entity data to NBT.
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("DespawnTimer", this.despawnTimeout);
        compound.putBoolean("Discard", this.discard);
        compound.putBoolean("IsNatural", this.primed);
        compound.putBoolean("Initialized", this.initialized);

        if (this.targetLevel != null) {
            compound.putString("TargetLevel", this.targetLevel.location().toString());
        }

        if (this.ownerUUID != null) {
            compound.putString("OwnerUUID", this.ownerUUID.toString());
        }
    }

    /**
     * Defines synchronized data for client-server communication.
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /**
     * Updates the portal every tick.
     */
    @Override
    public void tick() {
        super.tick();

        // Trigger explosion on first tick for naturally spawned portals
        if (!this.level().isClientSide() && !initialized && this.tickCount == 1 && primed) {
            createExplosivePortalSpace();
            this.initialized = true;
        }

        if (!this.level().isClientSide()) {
            // Handle automatic despawn countdown
            if (despawnTimeout > 0) {
                despawnTimeout--;
                if (despawnTimeout <= 0) {
                    PortalParticles.createPortalDisappearEffect((ServerLevel) this.level(), this.position());
                    this.discard();
                }
            }

            // Spawn swirling portal particles every 5 ticks
            if (tickCount % 5 == 0) {
                ServerLevel serverLevel = (ServerLevel) level();

                for (int i = 0; i < 3; i++) {
                    double angle = (tickCount * 0.1 + i * Math.PI * 2 / 3);
                    double radius = 0.8;

                    double x = getX() + Math.cos(angle) * radius;
                    double y = getY() + 0.5;
                    double z = getZ() + Math.sin(angle) * radius;

                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
                }
            }

            // Check for players entering the portal
            List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox());

            for (ServerPlayer player : players) {
                if (player != null && !player.isSpectator()) {
                    teleportPlayer(player.level(), player);
                }
            }
        }
    }

    /**
     * Handles portal removal and dimension cleanup.
     */
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        if (!reason.equals(RemovalReason.DISCARDED) && !reason.equals(RemovalReason.KILLED)) return;

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            ResourceKey<Level> targetDimension = this.getData(ModAttachments.PORTAL_TARGET_LEVEL);

            // Remove dimension if it's not school dimension
            if (!targetDimension.equals(Level.OVERWORLD) && !targetDimension.equals(ModLevel.SCHOOL_DIMENSION)) {
                DynamicDimensionHandler.removeDimensionForPortal(
                        serverLevel.getServer(),
                        this
                );
            }
        }
    }

    /**
     * Teleports a player through the portal to the target dimension.
     * Creates a return portal at the destination if traveling from vanilla dimensions.
     *
     * @param level  the current level
     * @param player the player to teleport
     */
    private void teleportPlayer(Level level, ServerPlayer player) {
        if (!level.isClientSide()) {
            if (player.isOnPortalCooldown()) {
                player.displayClientMessage(Component.literal("Portal is on cooldown!"), true);
                return;
            }

            if (targetLevel == null) {
                return;
            }

            Set<RelativeMovement> relatives = Collections.emptySet();
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            // Handle teleportation from vanilla dimensions to custom dimensions
            if (PortalUtils.isVanilla(portalLevel)) {
                // Store return position for later
                Map<ResourceKey<Level>, Vec3> returnLevelPos = Map.of(
                        player.level().dimension(), new Vec3(player.getX(), player.getY(), player.getZ())
                );

                ServerLevel destinationLevel = getLevelfromKey(targetLevel);

                // Determine spawn position based on target dimension
                Vec3 destinationPos;
                if (targetLevel == ModLevel.SCHOOL_DIMENSION) {
                    destinationPos = new Vec3(-1.5, 61, 0.5); // Fixed spawn point
                } else {
                    ChunkPos spawnChunk = DynamicDimensionHandler.getGenerationCenterData()
                            .getGenerationCenters()
                            .get(targetLevel);

                    assert destinationLevel != null;

                    destinationLevel.setChunkForced(spawnChunk.x, spawnChunk.z, true);
                    ChunkAccess chunk = destinationLevel.getChunk(spawnChunk.x, spawnChunk.z);
                    int terrainHeight = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnChunk.getMiddleBlockX(), spawnChunk.getMiddleBlockZ());
                    destinationLevel.setChunkForced(spawnChunk.x, spawnChunk.z, false);

                    destinationPos = new Vec3(0.5 + spawnChunk.getMiddleBlockX(), terrainHeight, 0.5 + spawnChunk.getMiddleBlockZ());
                }

                player.setData(ModAttachments.RETURN_LEVEL_POS.get(), returnLevelPos);
                assert destinationLevel != null;
                player.teleportTo(destinationLevel, destinationPos.x, destinationPos.y + 1, destinationPos.z, relatives, yaw, pitch);
                player.setPortalCooldown();

                if (discard) {
                    this.discard();
                }

                ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();
                PortalEntity existingPortal = PortalUtils.findNearestPortal(destinationLevel, player.position(), 128);

                // Create return portal if none exists nearby
                if (existingPortal == null) {
                    PortalEntity portal = new PortalEntity(
                            ModEntities.PORTAL_ENTITY.get(),
                            destinationLevel,
                            returnLevel
                    );
                    portal.setPos(player.position().x, player.position().y + 5, player.position().z);
                    destinationLevel.addFreshEntity(portal);
                }

            } else {
                // Handle return teleportation from custom dimension to vanilla
                Map<ResourceKey<Level>, Vec3> returnLevelPos = player.getData(ModAttachments.RETURN_LEVEL_POS.get());
                if (returnLevelPos == null || returnLevelPos.isEmpty()) {
                    player.displayClientMessage(Component.literal("No return position found!"), true);
                    return;
                }
                Vec3 returnPos = returnLevelPos.values().iterator().next();
                ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();

                player.teleportTo(getLevelfromKey(returnLevel), returnPos.x + 2, returnPos.y, returnPos.z + 2, relatives, yaw, pitch);
                player.removeData(ModAttachments.RETURN_LEVEL_POS.get());
                player.setPortalCooldown();

                if (discard) {
                    this.discard();
                }
            }
        }
    }

    /**
     * Creates an explosion to clear space around a naturally spawned portal.
     */
    private void createExplosivePortalSpace() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 centerPos = this.position();

        serverLevel.explode(
                this,
                centerPos.x,
                centerPos.y + 1,
                centerPos.z,
                25.0f,
                Level.ExplosionInteraction.BLOCK
        );
    }
}
