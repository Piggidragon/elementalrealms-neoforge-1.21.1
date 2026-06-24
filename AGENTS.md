# Elemental Realms — Agent Guide

## Build & Run

```bash
./gradlew build                          # CI builds; no separate lint/typecheck step
./gradlew runClient                      # Minecraft client with mod
./gradlew runServer                      # Dedicated server (--nogui)
./gradlew runGameTests                   # NeoForge game tests (GameTestServer, auto-exits)
./gradlew runData                        # Data generators → src/generated/resources/ (checked in)
./gradlew generateModMetadata            # Expand templates in src/main/templates/ into build/
```

Gradle 8.14.3, Java 21, configuration cache enabled (`gradle.properties`).

## Architecture

**`@Mod("elementalrealms")`** at `de.piggidragon.elementalrealms.ElementalRealms`. Constructor registers all `DeferredRegister`s on the **mod event bus** (`IEventBus`), one `register(IEventBus)` call per registry class.

**Registration pattern** — every content category lives in `registries/`:
```
registries/blocks/ModBlocks.java          # DeferredRegister.Blocks
registries/items/magic/affinities/        # AffinityItems (stones, shards)
registries/items/magic/misc/MiscItems.java # DimensionStaff
registries/entities/ModEntities.java       # PortalEntity
registries/attachments/ModAttachments.java # NeoForge AttachmentType
registries/worldgen/...                    # chunkgen, features, structures
registries/sounds/ModSounds.java
registries/guis/menus/ModMenus.java
registries/commands/ModCommands.java
registries/creativetabs/ModCreativeTabs.java # 3 tabs
```

**Event handlers** use `@EventBusSubscriber(modid = MODID)` (mod bus by default):
- Server events in `events/` (DragonDeathHandler, PlayerLoginHandler, ServerTickHandler)
- Client events in `client/events/ClientModEvents.java`
- Packet handler: `packets/ModPacketHandler.java`

**Networking** — custom `CustomPacketPayload` records with `StreamCodec`, registered in `RegisterPayloadHandlersEvent`. 5 packets total (2 S→C, 3 C→S). Use `context.enqueueWork()` in all handlers.

**Mixin** — 1 mixin (`mixin/EnderDragonMixin.java`), config at `elementalrealms.mixins.json`. Private mixin members prefixed `elementalrealms_neoforge_1_21_1$`.

**Enum extensions** — `META-INF/enumextensions.json` adds 2 custom `Rarity` values.

**Data generation** — `DataGenerators` class subscribes to `GatherDataEvent`. Output lands in `src/generated/resources/`. **Generated files are checked into git** — run `runData` and commit if generators change.

**No access transformers** in use (commented out in build.gradle and toml).

**No unit test framework** — only NeoForge game tests (run via `runGameTests`).

**Keep `run/` in `.gitignore`** — local run data never committed.

## Dependencies

| Library | Role |
|---------|------|
| InfiniVerse 2.0.1.0 | Dynamic dimension creation per portal |
| Lodestone 1.7.0 | Particle & screen effects |
| Curios API 9.5.1 | Required by Lodestone |

## CI

GitHub Actions runs `./gradlew build` on push/PR (Java 21, temurin).

There is also a `docs-agent` at `.github/agents/docs-agent.md` that adds JavaDoc/inline comments only — used via a `auto-docs.yml` workflow that creates issues for PRs touching Java files.
