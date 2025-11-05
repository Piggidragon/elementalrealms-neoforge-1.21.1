package de.piggidragon.elementalrealms.worldgen.features.custom;

import com.mojang.serialization.Codec;
import de.piggidragon.elementalrealms.entities.ModEntities;
import de.piggidragon.elementalrealms.entities.custom.PortalEntity;
import de.piggidragon.elementalrealms.entities.variants.PortalVariant;
import de.piggidragon.elementalrealms.level.ModLevel;
import de.piggidragon.elementalrealms.util.PortalUtils;
import de.piggidragon.elementalrealms.worldgen.features.config.PortalConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Feature used to spawn portals in the world.
 */
public class PortalSpawnFeature extends Feature<PortalConfiguration> {

    public PortalSpawnFeature(Codec<PortalConfiguration> codec) {
        super(codec);
    }

    /**
     * Attempts to place a portal according to the configuration.
     */
    @Override
    public boolean place(FeaturePlaceContext<PortalConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        PortalConfiguration config = context.config();
        RandomSource randomSource = level.getRandom();

        // Server is required to create cross-dimension portal references
        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        if (!PortalUtils.isValidDimensionForSpawn(level.getLevel(), pos)) {
            return false;
        }

        // Validate portal base suitability
        if (!PortalUtils.isSuitableForPortalBase(level, pos.below(), level.getBlockState(pos.below()))) {
            return false;
        }

        // Create portal entity. The constructor parameters include target dimension lookup.
        PortalEntity portal = new PortalEntity(
                ModEntities.PORTAL_ENTITY.get(),
                level.getLevel(),
                false,
                -1,
                ModLevel.TEST_DIMENSION,
                null
        );

        // Position the portal precisely centered on the block
        portal.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        portal.setYRot(randomSource.nextFloat() * 360.0F);

        // Apply chosen variant from config, fallback to random variant if null
        if (config.portalVariant() != PortalVariant.RANDOM) {
            portal.setVariant(config.portalVariant());
        } else {
            portal.setRandomVariant();
        }

        if (pos.getY() < 41) portal.prime();

        level.addFreshEntity(portal);
        return true;
    }
}
