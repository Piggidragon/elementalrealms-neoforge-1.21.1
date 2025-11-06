package de.piggidragon.elementalrealms.entities.custom;

import de.piggidragon.elementalrealms.attachments.ModAttachments;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.entities.variants.PortalVariant;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.particles.PortalParticles;
import de.piggidragon.elementalrealms.util.PortalUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private int idleAnimationTimer = -1;

    private final ResourceKey<Level> portalLevel; // Dimension where this portal exists
    private ResourceKey<Level> targetLevel; // Dimension to teleport to
    private UUID ownerUUID = null;
    private boolean initialized = false;
    private boolean discard = false; // Remove portal after single use
    private int despawnTimeout = 0; // Ticks until automatic removal
    private boolean primed = false; // Natural spawn flag - triggers explosion

    /**
     * Creates a portal entity with default settings.
     *
     * @param type the entity type
     * @param level the world level
     */
    public PortalEntity(EntityType<? extends PortalEntity> type, Level level) {
        super(type, level);
        this.portalLevel = level.dimension();

        if (!level.isClientSide()) {
            this.setVariant(PortalVariant.SCHOOL);
        }

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
     * @param type the entity type
     * @param level the world level
     * @param discard whether to remove portal after use
     * @param despawnTimeout ticks until automatic removal
     * @param targetLevel the dimension to teleport to
     * @param ownerUUID the UUID of the player who created this portal
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

    private MinecraftServer getServer() {
        return level().getServer();
    }

    /**
     * Gets the visual variant of this portal.
     *
     * @return the portal variant
     */
    public PortalVariant getVariant() {
        try {
            return PortalVariant.byId(this.entityData.get(DATA_VARIANT));
        } catch (Exception e) {
            return PortalVariant.SCHOOL;
        }
    }

    /**
     * Sets the visual variant of this portal.
     *
     * @param variant the portal variant to set
     */
    public void setVariant(PortalVariant variant) {
        if (variant == null) variant = PortalVariant.SCHOOL;
        this.entityData.set(DATA_VARIANT, variant.getId());
    }

    private ServerLevel getLevelfromKey(ResourceKey<Level> targetLevel) {
        return this.getServer().getLevel(targetLevel);
    }

    /**
     * Gets the portal's position as a vector.
     *
     * @return the position vector
     */
    public Vec3 getPositionVec() {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    /**
     * Sets a random variant from non-school types.
     */
    public void setRandomVariant() {
        PortalVariant[] variants = {PortalVariant.ELEMENTAL, PortalVariant.DEVIANT, PortalVariant.ETERNAL};
        int randomIndex = this.level().random.nextInt(variants.length);
        this.setVariant(variants[randomIndex]);
    }

    /**
     * Marks this portal as naturally spawned, enabling explosion on first tick.
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

    /**
     * Handles damage to the portal.
     */
    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

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

    /**
     * Determines if the portal can be collided with.
     */
    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return false;
    }

    /**
     * Manages animation states for the portal.
     */
    public void setupAnimationStates() {
        if (this.idleAnimationTimer <= 0) {
            this.idleAnimationTimer = 160; // Reset timer for next cycle
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimer;
        }
    }

    /**
     * Reads entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.despawnTimeout = valueInput.getIntOr("DespawnTimer", 0);
        this.discard = valueInput.getBooleanOr("Discard", false);
        this.primed = valueInput.getBooleanOr("IsNatural", false);
        this.initialized = valueInput.getBooleanOr("Initialized", false);

        // Parse target dimension from saved string
        String levelKey = valueInput.getStringOr("TargetLevel", "");
        if (!levelKey.isEmpty() && !this.level().isClientSide()) {
            ResourceLocation location = ResourceLocation.tryParse(levelKey);
            if (location != null) {
                this.targetLevel = ResourceKey.create(Registries.DIMENSION, location);
            } else {
                this.targetLevel = Level.OVERWORLD; // Fallback to overworld
            }
        } else if (levelKey.isEmpty() && !this.level().isClientSide() && this.getServer() != null) {
            this.targetLevel = Level.OVERWORLD;
        }

        // Parse owner UUID from string
        String uuidString = valueInput.getStringOr("OwnerUUID", "");
        if (!uuidString.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                this.ownerUUID = null;
            }
        }

        int variantId = valueInput.getIntOr("Variant", PortalVariant.SCHOOL.getId());
        this.setVariant(PortalVariant.byId(variantId));
    }

    /**
     * Saves entity data to NBT.
     */
    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("DespawnTimer", this.despawnTimeout);
        valueOutput.putBoolean("Discard", this.discard);
        valueOutput.putBoolean("IsNatural", this.primed);
        valueOutput.putBoolean("Initialized", this.initialized);
        valueOutput.putInt("Variant", this.getVariant().getId());

        if (this.targetLevel != null) {
            valueOutput.putString("TargetLevel", this.targetLevel.location().toString());
        }

        if (this.ownerUUID != null) {
            valueOutput.putString("OwnerUUID", this.ownerUUID.toString());
        }
    }

    /**
     * Defines synchronized data for client-server communication.
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VARIANT, PortalVariant.SCHOOL.getId());
    }

    /**
     * Updates the portal every tick.
     */
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

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

                // Create 3 particles in a rotating pattern
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
     * Teleports a player through the portal to the target dimension.
     * Creates a return portal at the destination if traveling from vanilla dimensions.
     *
     * @param level the current level
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

            Set<Relative> relatives = Collections.emptySet();
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            boolean setCamera = true;

            // Handle teleportation from vanilla dimensions to custom dimensions
            if (PortalUtils.isVanilla(portalLevel)) {
                // Store return position for later
                Map<ResourceKey<Level>, Vec3> returnLevelPos = Map.of(
                        player.level().dimension(), new Vec3(player.getX(), player.getY(), player.getZ())
                );

                ServerLevel destinationLevel = getLevelfromKey(targetLevel);

                ChunkPos spawnChunk = new ChunkPos(0, 0);
                destinationLevel.setChunkForced(spawnChunk.x, spawnChunk.z, true);

                // Determine spawn position based on target dimension
                Vec3 destinationPos;
                if (targetLevel == ModLevel.SCHOOL_DIMENSION) {
                    destinationPos = new Vec3(-1.5, 61, 0.5); // Fixed spawn
                } else {
                    int terrainHeight = destinationLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
                    destinationPos = new Vec3(0.5, terrainHeight, 0.5);
                }

                player.setData(ModAttachments.RETURN_LEVEL_POS.get(), returnLevelPos);
                player.teleportTo(destinationLevel, destinationPos.x, destinationPos.y, destinationPos.z, relatives, yaw, pitch, setCamera);
                player.setPortalCooldown();

                if (discard) {
                    this.discard();
                }

                MinecraftServer server = destinationLevel.getServer();
                ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();

                // Schedule portal check for next tick
                server.execute(() -> {
                    PortalEntity existingPortal = PortalUtils.findNearestPortal(destinationLevel, destinationPos, 5);

                    if (existingPortal == null) {
                        PortalEntity portal = new PortalEntity(
                                ModEntities.PORTAL_ENTITY.get(),
                                destinationLevel,
                                returnLevel
                        );
                        portal.setPos(destinationPos.x, destinationPos.y + 5, destinationPos.z);
                        destinationLevel.addFreshEntity(portal);
                    }
                });

            } else {
                // Handle return teleportation from custom dimension to vanilla
                Map<ResourceKey<Level>, Vec3> returnLevelPos = player.getData(ModAttachments.RETURN_LEVEL_POS.get());
                Vec3 returnPos = returnLevelPos.values().iterator().next();
                ResourceKey<Level> returnLevel = returnLevelPos.keySet().iterator().next();

                player.teleportTo(getLevelfromKey(returnLevel), returnPos.x, returnPos.y, returnPos.z, relatives, yaw, pitch, setCamera);
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
