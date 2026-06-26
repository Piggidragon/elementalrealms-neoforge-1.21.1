package de.piggidragon.elementalrealms.registries.worldgen.features.custom.entities;

import com.mojang.serialization.Codec;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import de.piggidragon.elementalrealms.registries.entities.custom.misc.PortalEntity;
import de.piggidragon.elementalrealms.registries.level.DynamicDimensionHandler;
import de.piggidragon.elementalrealms.registries.level.ModLevel;
import de.piggidragon.elementalrealms.registries.worldgen.features.config.PortalConfiguration;
import de.piggidragon.elementalrealms.util.entities.portal.PortalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Places a portal entity during worldgen. Underground portals are primed to
 * explode on their first server tick to clear space around them.
 */
public class PortalSpawnFeature extends Feature<PortalConfiguration> {

    private static final double PORTAL_CENTER_OFFSET = 0.5;
    private static final int UNDERGROUND_THRESHOLD = 41;

    public PortalSpawnFeature(Codec<PortalConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PortalConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = level.getRandom();
        MinecraftServer server = level.getServer();
        if (server == null) return false;
        if (!PortalUtils.isValidDimensionForSpawn(level, pos)) return false;
        if (!PortalUtils.isSuitableForPortalBase(level.getBlockState(pos.below()))) return false;

        PortalEntity portal = new PortalEntity(ModEntities.PORTAL_ENTITY.get(), level.getLevel());
        portal.setTargetLevel(DynamicDimensionHandler.createDimensionForPortal(server, portal, ModLevel.getRandomLevel()));

        portal.setPos(
                pos.getX() + PORTAL_CENTER_OFFSET,
                pos.getY() + PORTAL_CENTER_OFFSET,
                pos.getZ() + PORTAL_CENTER_OFFSET
        );
        portal.setYRot(random.nextFloat() * 360.0F);

        if (pos.getY() < UNDERGROUND_THRESHOLD) {
            portal.prime();
        }

        level.addFreshEntity(portal);
        return true;
    }
}
