package de.piggidragon.elementalrealms.registries.worldgen.features.custom.entities;

import com.mojang.serialization.Codec;
import de.piggidragon.elementalrealms.registries.worldgen.features.config.PortalConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Places a portal entity during worldgen.
 *
 * <p><b>Currently disabled:</b> {@link #place} early-returns false because the
 * underlying portal-dimension wiring is incomplete. See the TODO in {@link #place}
 * for the re-enable steps.</p>
 */
public class PortalSpawnFeature extends Feature<PortalConfiguration> {

    public PortalSpawnFeature(Codec<PortalConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PortalConfiguration> context) {
        // TODO(re-enable in Phase 4 — pocket dimensions): portal worldgen is disabled.
        // PortalConfiguration.target_dimension is currently hardcoded to Level.OVERWORLD in
        // ModDatapackProvider, which means createDimensionForPortal() is invoked with
        // Level.OVERWORLD and NPEs inside getStemForLevel() (only test + school have stems).
        // Until the 11 affinity-specific pocket templates and their biome modifier exist,
        // portal spawning has no valid target dimension. Re-enable by:
        //   1. Setting PORTAL_CONFIGURED's target_dimension to one of the pocket templates,
        //   2. Adding the per-affinity template dimension keys to ModLevel.LEVEL_STEMS,
        //   3. Removing this early-return.
        return false;
    }
}
