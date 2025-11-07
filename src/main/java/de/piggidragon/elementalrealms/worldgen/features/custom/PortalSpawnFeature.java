package de.piggidragon.elementalrealms.worldgen.features.custom;

import com.mojang.serialization.Codec;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.entities.variants.PortalVariant;
import de.piggidragon.elementalrealms.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.util.PortalUtils;
import de.piggidragon.elementalrealms.worldgen.features.config.PortalConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Worldgen feature for spawning portal entities during world generation.
 * Used to create naturally occurring portals in vanilla dimensions.
 */
public class PortalSpawnFeature extends Feature<PortalConfiguration> {

    /**
     * Creates portal spawn feature with configuration codec.
     *
     * @param codec Configuration codec for serialization
     */
    public PortalSpawnFeature(Codec<PortalConfiguration> codec) {
        super(codec);
    }

    /**
     * Attempts to place a portal entity during worldgen.
     * Validates spawn conditions and creates portal with configured properties.
     */
    @Override
    public boolean place(FeaturePlaceContext<PortalConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        PortalConfiguration config = context.config();
        RandomSource randomSource = level.getRandom();

        // Server required for cross-dimension portal references
        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        // Validate dimension-specific spawn conditions
        if (!PortalUtils.isValidDimensionForSpawn(level, pos)) {
            return false;
        }

        // Validate solid non-fluid base block
        if (!PortalUtils.isSuitableForPortalBase(level, pos.below(), level.getBlockState(pos.below()))) {
            return false;
        }

        // Create portal entity with target dimension
        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                level.getLevel()
        );

        portal.setTargetLevel(DynamicDimensionHandler.createDimensionForPortal(server, portal));


        // Position portal centered on block with random rotation
        portal.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        portal.setYRot(randomSource.nextFloat() * 360.0F);

        // Apply configured variant or randomize
        if (config.portalVariant() != PortalVariant.RANDOM) {
            portal.setVariant(config.portalVariant());
        } else {
            portal.setRandomVariant();
        }

        // Prime underground portals (Y < 41) for activation
        if (pos.getY() < 41) portal.prime();

        level.addFreshEntity(portal);
        return true;
    }
}
