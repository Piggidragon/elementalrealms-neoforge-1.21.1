# vanilla-rework-source (snapshot)

Snapshot of the vanilla-dragon laser and enchantment-nerf code that was removed
from `elementalrealms-neoforge-1.21.1` in PR for issue #52.

**Purpose:** First-resource backup for `Piggidragon/dragonsrequiem-neoforge-1.21.1`,
the sibling repo where vanilla-rework code lives (ER = vanilla-balanced addons
only — see `elementalrealms-codebase` skill §Scope split, §9 Locked-in design
decisions).

**What is here:** All files that moved out of ER. Read-only, not part of the
ER build, no Gradle wiring, no `mod_id=elementalrealms` registration.

**What is NOT here:** Anything that stays in ER:
- `DragonDeathHandler.java` (gate trigger on `elementalrealms:root` advancement,
  vanilla-balance-aware — the mixin was never a dependency)
- `PortalConfig.java`, `SchoolConfig.java`, `ModAffinities`, etc.
- The `affinities.json` / `school.json` / `dimensions.json` configs
- Lodestone as VFX library (stays as a dependency, but per-file lodestone-using
  code like `LaserBeamTask` is here)

## Tree

```
mixin/                           EnderDragonMixin.java (server-side aiStep injection)
packets/custom/enderdragon/      EnderDragonLaserBeamPacket.java
                                 EnderDragonLaserBeamHitEntityPacket.java
                                 EnderDragonLaserBeamDestroyBlockPacket.java
client/rendering/tasks/tick/     LaserBeamTask.java (client-side particle rendering)
registries/configs/              DragonConfig.java (loads dragon.json)
                                 EnchantmentsConfig.java (loads enchantments.json)
registries/sounds/               ModSounds.java (LASER_BEAM sound event)
datagen/                         ModSoundsProvider.java (sounds.json generator)
sounds/                          laser_beam.ogg (binary copy, license audit open)
configs/                         dragon.json5 (default tunables, extracted)
                                 enchantments.json5 (default tunables, extracted)
```

## What needs changing when moving to `dragonsrequiem`

1. **Package rename** — every Java file's `package` line and every cross-file
   import references `de.piggidragon.elementalrealms`. Rename to
   `de.piggidragon.dragonsrequiem` (project root: dragonsrequiem-neoforge-1.21.1).

2. **Mod-ID swap** — every `ElementalRealms.MODID` reference (`"elementalrealms"`)
   becomes `DragonsRequiem.MODID` (`"dragonsrequiem"`). In particular the
   `ResourceLocation.fromNamespaceAndPath(...)` calls in the three packet files
   and the sound-event IDs.

3. **Config loader setup** — `Json5Reloadable`, `Json5ConfigLoader`,
   `Json5SectionReader`, `Json5ConfigLoader.resolve` are ER-internal helpers.
   DR either copies them or rewires to its own config loader. The two `.json5`
   files in `configs/` are the default write-default payloads — they will be
   written on first DR launch once the loader is in place.

4. **`ModDatapackProvider.LASER` DamageType** — the damage-type key needs to
   be re-registered in DR's `ModDatapackProvider`. The ER reference file
   `ModDatapackProvider.java` is not in this snapshot because the rest of its
   body is ER-specific (portal feature, biome modifier). The single relevant
   block is:
   ```java
   public static final ResourceKey<DamageType> LASER =
           ResourceKey.create(Registries.DAMAGE_TYPE,
                   ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "laser"));
   // ...
   .add(Registries.DAMAGE_TYPE, bootstrap -> bootstrap.register(LASER,
           new DamageType("laser", 0.0f)))
   ```
   For DR, the `ElementalRealms.MODID` becomes `DragonsRequiem.MODID`. The
   damage-type registry is global; if both mods register `minecraft:laser` the
   second mod will collide. Rename to e.g. `dragonsrequiem:dragon_laser` to
   avoid a future clash if ER ever re-adds a different laser.

5. **`mixins.json`** — DR's equivalent mixin config (if any) needs
   `"EnderDragonMixin"` listed under `mixins`. The ER `mixins.json` itself
   stays in ER but with the entry removed.

6. **Assets** — `laser_beam.ogg` needs to ship with DR's jar. License audit on
   this asset is still open as of the snapshot date (2026-06-27); verify the
   source/origin before publishing DR.

7. **Sound-subtitle lang key** — `sound.elementalrealms.laser_beam` (referenced
   in `ModSoundsProvider`) must become `sound.dragonsrequiem.laser_beam` in DR
   and be present in DR's `en_us.json`. ER's `lang/` directory is empty at
   snapshot time (lang keys are only generated through datagen); no cleanup
   needed in ER.

8. **`RenderManager` / `TickTask`** — `LaserBeamTask` uses
   `de.piggidragon.elementalrealms.client.rendering.tasks.RenderManager` and
   `TickTask`. These are ER render-loop primitives. DR needs to either reuse
   them via shared-library wiring (separate module, shared by both repos) or
   reimplement the tick-task queue. Decision for the DR port, not blocking the
   ER cleanup.

9. **`ModPacketHandler`** — the laser handlers (`handleLaserBeamHitEntity`,
   `handleDragonLaserBeam`, `handleLaserBeamDestroyBlock`) and the four
   `LASER_BEAM_*` constants in `ModPacketHandler.java` are not snapshot-ed
   here because the rest of `ModPacketHandler` is ER-specific (affinity-book
   packets, particle-show, etc). When porting, copy the four constants and the
   three handler methods into DR's `ModPacketHandler`, and register the three
   payload handlers on the DR registrar in DR's `onRegisterPayloadHandlers`.

## Why a snapshot directory and not a release branch

`docs/_references/` is the canonical "parking lot" location for code that
ER no longer uses but might still inform future work. It is not built, not
loaded, and not part of any mod packaging. When DR is ready to receive this
code, copy out of here and into the DR repo's `src/` tree — do not symlink,
do not wire up Gradle to read from this directory.

If the snapshot ever becomes stale (ER has been clean for many releases and
DR has its own implementation), delete this whole directory.