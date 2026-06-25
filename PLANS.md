# Elemental Realms — Master Plan

> **For Piggidragon:** Live document. Iterated as we go.
>
> **Status:** in development. Breaking changes expected. English only. No timeline pressure — we hack phase by phase at whatever pace feels right. Performance: lean and sensible, not potato-PC (MC is bad at that anyway; users can opt into Sodium/Embeddium).

**Goal:** Transform the current skeleton into a full progression mod — Overworld → Nether → End journey, combat, 12 affinities in 3 tiers, 12 pocket dimensions with bosses/spells/structures. Generic fantasy-academy world, TBATE = loose reference only.

**Architecture:** NeoForge 1.21.1, Parchment mappings, Infiniverse for dynamic dimensions, BoundedChunkGenerator as pocket base. Affinities as player attachment. Lodestone for VFX only. Pocket dimensions share an affinity ring layout via Jigsaw. **Every tunable exposed as data so modpack authors can rebalance without recompiling.**

**Tech Stack:** Java 21, NeoForge 21.1.214, Infiniverse 2.0.1.0, Lodestone 1.7.0 (VFX only), Curios 9.5.1, Jigsaw, DeferredRegister, payload networking, codec-based saved data, NeoForge ModConfigSpec + custom JSON5 loaders + datapacks.

**Versioning:** SemVer-ish. `0.X.0` = major (breaking for players or modpack authors — config schema breaking, removed content, save-invalidating mechanics, renames). `0.0.X` = minor (additive content, balance tweaks, internal refactors that don't break saves/configs). `main` <- `dev` only on release.

---

## 0. Glossary

- **Affinity** — elemental alignment a player holds. 12 total + Void.
- **Tier** — affinity category: NONE / ELEMENTAL / DEVIANT / ETERNAL.
- **Deviant** — advanced affinity, requires base at 100%.
- **Eternal** — ultimate, mutually exclusive (one per player).
- **Void** — "no affinity" state. Clears all.
- **Pocket** — small isolated 1000×1000 world per affinity entry, via portal.
- **Ring** — concentric zone inside a pocket (Ring 0 = center boss arena, Ring 4 = boundary wall).
- **Boss** — per-affinity pocket boss with phases, vanilla HP bar, drops.
- **Spell** — castable magical action tied to an affinity (e.g. Fire-Bolt). Costs mana + cooldown.
- **Stone / Shard** — items that grant (+100%) or increment (+5%) an affinity. Consumed.
- **Scroll of Awakening** — School item that reveals hidden affinities to the player.
- **Reveal** — the moment a player transitions from "unknown affinities" to "known affinities".
- **Mana Core** — Curios slot item holding player's mana pool.
- **Roll** — first-login random assignment of affinities.
- **NamingRegistry** — single source for boss / spell / dimension display names.
- **Configuration / Config** — external data files driving tunable values. Three layers: ModConfigSpec TOML (simple), custom JSON5 (complex tables), datapacks (content).
- **JSON5** — JSON superset with comments and trailing commas. Used for `config/*.json`.
- **Lodestone** — third-party VFX library. Particles, screen-shake, projectile rendering, screens. NOT HP bars.
- **Infiniverse** — library for per-portal dynamic dimensions.
- **Curios** — library for extra equipment slots (Mana Core).
- **SavedData** — persistent world-attached data (we use for `GenerationCenterData`).
- **BoundedChunkGenerator** — custom chunk gen producing noise terrain only inside configurable radius; outside = air-void.
- **Jigsaw** — Minecraft structure-piece system.
- **DeferredRegister** — NeoForge registration pattern.
- **Attachment** — NeoForge per-entity persistent data (player affinities, portal target level, return position).
- **Codec / StreamCodec / Packet** — serialization / networking primitives.
- **VFX** — visual effects. Anything particle/screen-shake-related.
- **Lean perf** — "as many particles as needed, as few as sensible". Don't over-optimize.

---

## 1. Vision & Setting

**TBATE is a rough reference only, NOT a faithful adaptation.** Borrow: fantasy-academy vibe, magic-to-elements, staged progression, "post-Dragon gateway to a larger world". Don't borrow: character names, plot points, creature names, story beats.

**Boss name placeholders** (final names decided during Phase 0):
- FIRE: Lord of Embers
- WATER: Tidal Sovereign
- EARTH: The Stoneheart
- WIND: Stormwing Roc
- LIGHTNING: Stormcaller
- ICE: The Frostbound King
- SOUND: The Resonant Wraith
- GRAVITY: The Singularity
- LIFE: World-Tree Avatar
- SPACE: The Void-Walker
- TIME: Chrono-Warden

Names live in `NamingRegistry`. Renaming later = one config edit.

---

## 2. Current Code State

### 2.1 Affinity system ✓
- `magic/affinities/{Affinity, AffinityType, ModAffinities, ModAffinitiesRoll}.java`
- `events/PlayerLoginHandler.java`
- `registries/attachments/ModAttachments.java` + `sync/AffinityAttachmentSyncHandler.java`
- `packets/custom/AffinitySuccessPacket.java`

### 2.2 Affinity items ✓
- `registries/items/magic/affinities/AffinityItems.java`
- `registries/items/magic/affinities/custom/{AffinityStone, AffinityShard}.java`
- `util/ModRarities.java` — LEGENDARY + MYTHIC enum extensions registered but currently unused.

### 2.3 School dimension ✓ (half)
- `data/elementalrealms/dimension/school.json`, `dimension_type/school.json`
- `data/elementalrealms/structure/{anchor,wood,nether,end,grass}.nbt`
- `registries/level/ModLevel.java`, `events/DragonDeathHandler.java`
- `registries/items/magic/misc/custom/SchoolStaff.java`

### 2.4 Ender Dragon buff (partial) ✓
- `mixin/EnderDragonMixin.java` (laser)
- `packets/custom/DragonLaserBeamPacket.java`
- `registries/sounds/ModSounds.java`
- `client/particles/vanilla/{PortalParticles, DimensionStaffParticles}.java`

### 2.5 Dynamic Realms (stub) ⚠
- `registries/level/DynamicDimensionHandler.java` — static counter bug, ring scan math not aligned with BoundedChunkGenerator radius.
- `saveddata/GenerationCenterData.java` — persists generation centers.
- `registries/worldgen/chunkgen/custom/BoundedChunkGenerator.java` — RADIUS=10 = 336×336 blocks; user wants 1000×1000.
- `registries/entities/custom/PortalEntity.java` — "I couldn't get the seed to change" — per-portal random seed via Infiniverse.

### 2.6 Particles / packets / commands / datagen ✓
- 5 packets, vanilla particles, RenderManager task queue.
- `/affinities list|set|clear|reroll` (OP 2).
- Datagen: advancements, loot, tags, sounds, models, recipes.

### 2.7 Saved-code skeletons ⚠
- `src/main/saved_code/` — Affinity GUI / hotbar + laser beam renderer prior work, not yet integrated.

### 2.8 Hardcoded values that must move to config (Phase 0 backlog)

`ModAffinitiesRoll.ROLL_CHANCES`, `DEVIANT_CHANCE_PERCENT`, `ModAffinities.MAX_COMPLETION`, `BoundedChunkGenerator.RADIUS`, `DynamicDimensionHandler.MAX_LAYERS`, `PortalEntity` constants, `EnderDragonMixin` constants, `AffinityItems` rarity assignments, `SchoolStaff` constants — all currently Java `static final`.

---

## 3. Outstanding Ideas / Stubs

From user + IDEAS.md:

- [ ] Dragon buff extension (HP + AoE)
- [ ] **Enchantment nerf** — Protection/Sharpness less game-changing *(Phase 0.5)*
- [ ] Ominous-Potion-Scaling like Trial Chambers
- [ ] School content (rooms, books, Scroll of Awakening)
- [ ] Pocket dimensions for ALL 11 boss affinities (4 OW / 4 Nether / 3 End)
- [ ] Boss per pocket
- [ ] Custom mobs in Overworld AND pockets
- [ ] Structure-rooms diversity
- [ ] Spells per affinity (3 each, 36 total)
- [ ] Spell API skeleton
- [ ] Affinity reveal mechanic
- [ ] Portal distribution worldwide (Nether + End)
- [ ] Affinity GUI integration (tweak visuals)
- [ ] Portal idle/teleport sound + rift shader
- [ ] Advancements as progression guide
- [ ] Force chunkload fix
- [ ] Timer difficulty / corrupted world (endgame)
- [ ] Spawn rarity per affinity stone
- [ ] ModRarities LEGENDARY/MYTHIC in use
- [ ] Modpack config exposure (every magic number reachable, hot-reloadable)

---

## 4. Consolidated Game Mechanics

### 4.1 Player lifecycle

| Step | Where | What happens |
|---|---|---|
| 0. Login | Overworld | Roll 1–4 ELEMENTAL affinities (100% each, falling) + 25% deviant bonus. Affinities **client-invisible**. Rates from `affinities.json.roll`. |
| 1. Early progression | Overworld | Shards build to 100%; stones instant. Order unknown because player doesn't know affinity. |
| 2. Dragon buff | End | 2× HP, 3 phases, laser + per-phase AoE + crystal-buff blocking. Numbers from `dragon.json`. |
| 3. Dragon kill | End | Permanent School-dimension portal at Overworld spawn. Broadcast. |
| 4. School entry | Staff or portal | Hogwarts-style hubs. Teacher books. Scroll of Awakening reveals affinities. |
| 5. First pocket | Overworld | Wind portal → pocket_dim_wind_<id>. Ring layout, boss in center. Win → drops stone + spell scroll. |
| 6. Deviant phase | Nether | Nether pockets (Sound/Gravity/Lightning/Ice). Requires deviant affinity. |
| 7. Eternal phase | End | End pockets (Life/Space/Time). Requires eternal affinity (extremely rare). |
| 8. Endgame | ? | Optional timer difficulty, corrupted world, generic endgame boss. |

### 4.2 Affinity acquisition

| Method | Probability | Where | Config |
|---|---|---|---|
| Login roll | 100/25/20/20% + 25% deviant bonus | automatic | `affinities.json.roll` |
| Affinity stone | rare | boss drops | `bosses.json` |
| Affinity shard | common | mob drops, structures | `mobs.json`, datapack loot |
| Void stone | manual reset | player-crafted or endgame | recipe in datapack |

### 4.3 Affinity tier visibility
- **Current:** client sees `Map<Affinity, Integer>` directly.
- **User requirement:** player does NOT know them at first.
- **Q (open):** see §11 — when exactly can spells be cast relative to reveal?

### 4.4 Pocket dimension spec

- **Size:** 1000×1000 blocks ≈ 63 chunk radius (currently 336×336). Radius from `dimensions.json.pocket.radiusChunks`.
- **Layout:**
  - Ring 0 (0–50 blocks, center): boss arena, custom NBT structure.
  - Ring 1 (50–250): hub — player spawn, NPC hint, safe zone.
  - Ring 2 (250–500): trap rooms + mini-bosses.
  - Ring 3 (500–750): resource farm (custom spawner, custom ores).
  - Ring 4 (750–1000): boundary wall (bedrock variant).
- **Mob spawn:** affinity-specific in rings 2+3. From `mobs.json`.
- **Seed:** per affinity, deterministic per portal-id. Same affinity = same style, different portal = different layout.

### 4.5 Spells

3 per affinity = 36 total. All values from `spells.json`.

| Affinity | Basic | Utility | Ultimate |
|---|---|---|---|
| FIRE | Fire Bolt | Flame Shield | Meteor |
| WATER | Water Bolt | Tidal Wave | Healing Rain |
| WIND | Wind Slash | Dash | Tornado |
| EARTH | Rock Throw | Earthen Wall | Quake |
| LIGHTNING | Lightning Bolt | Chain Lightning | Storm Call |
| ICE | Ice Spike | Frost Nova | Blizzard |
| SOUND | Sonic Blast | Silence Zone | Resonant Roar |
| GRAVITY | Gravity Crush | Float | Singularity |
| LIFE | Heal | Regen Aura | Resurrection |
| SPACE | Blink | Phase Shift | Pocket Dimension |
| TIME | Slow | Haste | Stasis |
| VOID | Purge | — | — |

---

## 5. System Architecture

### 5.1 Lodestone scope

VFX only. NOT HP bars, NOT general entity rendering.

| Use | Lodestone API |
|---|---|
| Spell VFX | `WorldParticleManager.spawnParticleBatch(...)` |
| Boss phase transition | `LodestoneScreenAPI.addWorldEvent(...)` |
| Portal idle | `WorldParticleManager` rotating glyph ring |
| Portal rift | `LodestoneShaderRegistry` |
| Meteor/Blizzard/Tornado | Lodestone `ProjectileRenderer` |
| Laser (Dragon + Lightning) | `WorldParticleRenderer` line-of-sight beam |
| Spell Book GUI | `LodestoneScreen` |
| Corrupted World | `LodestoneScreenAPI` vignette + tint |

NOT Lodestone: boss HP bars (vanilla `BossEvent`), Ender Dragon HP bar (already vanilla), general entity rendering, simple HUD elements (vanilla `GuiGraphics`).

### 5.2 Configuration-first

**Every magic number is data, never a Java constant.**

Three layers:
1. **NeoForge `ModConfigSpec` (TOML)** — `config/elementalrealms-{common,server,client}.toml`. Binary toggles, simple multipliers, audio sliders.
2. **Custom JSON5 files** in `config/elementalrealms/` — complex tables.
3. **Datapacks** in `data/elementalrealms/` — recipes, loot, structures, mob spawns.

```
config/elementalrealms/
├── common.toml, server.toml, client.toml
├── affinities.json, dimensions.json, spells.json, bosses.json
├── mobs.json, portal.json, dragon.json, school.json
├── enchantments.json, timer.json
```

**Reload model:** server configs hot-reload via `/reload` or `ModConfigEvent.Reloading`. Lazy-apply: new entities read new values, existing entities keep snapshot until despawn. Loader warns if a changed field only takes effect on next generation. Client configs hot-reload via ModConfigScreen. Datapacks via `/reload` (F3+T). New `/elementalrealms reload` command via `ConfigReloadListener`.

**Schema versioning:** every JSON carries `"schemaVersion": 1`. Strict by default with `common.toml.allowSchemaMismatch` toggle for dev. New fields default to safe fallbacks (forward-compat).

### 5.3 Module tree

```
registries/
├── items/magic/
│   ├── affinities/         [✓ — re-review in Phase 0]
│   ├── misc/               [✓ — SchoolStaff]
│   ├── spells/             [NEW] Spell items
│   └── affinitytools/      [NEW] Scroll of Awakening
├── worldgen/
│   ├── structures/pockets/ [NEW] 11 ring layouts
│   ├── structures/bosses/  [NEW] 11 boss-arena NBTs
│   └── features/pocketmobs/[NEW]
├── entities/custom/
│   ├── bosses/             [NEW] 11 boss entities
│   └── elementals/         [NEW] Custom mobs
├── level/PocketRegistry.java
├── configs/
│   ├── ModConfigs.java, ConfigReloadListener.java, NamingRegistry.java
│   └── {Affinity,Dimensions,Spells,Bosses,Mobs,Portal,Dragon,School,Enchantments,Timer}Config.java
└── commands/ModCommands.java [/pocket list|tp|heal + /elementalrealms reload]
magic/
├── affinities/             [✓]
├── spells/
│   ├── Spell.java, SpellRegistry.java, SpellCasting.java
│   ├── SpellBookScreen.java (Lodestone)
│   ├── SpellHotkeyHandler.java
│   └── impl/{FireSpells, WaterSpells, ...}.java
└── mana/{ManaCapability, ManaHudOverlay}.java
client/
├── lodestone/              [NEW]
│   ├── AffinityParticles.java (REWORK → Lodestone)
│   ├── PortalRiftRenderer.java
│   ├── LaserBeamRenderer.java (PROMOTE from saved_code)
│   └── ScreenShakeHook.java
events/
├── DragonDeathHandler.java [✓]
├── PlayerLoginHandler.java [✓]
├── ServerTickHandler.java  [✓ — extend]
└── PocketDimensionBuilder.java [NEW]
```

---

## 6. Phase 0 — Full Consolidation Pass

Runs **before any new feature**. Existing code reviewed, rewritten where needed, config-driven.

### Phase 0.1 — Code review & rewrite
- Re-read every active class. Document inconsistencies.
- Resolve `ModLevel.getRandomLevel()` — random pick contradicts affinity-specific requirement.
- Resolve why `PortalEntity.remove()` excludes SCHOOL from dimension cleanup.
- Resolve `BoundedChunkGenerator.getRadius()` + `DynamicDimensionHandler.scanRing` math consistency.
- Decide promotion / archive / discard for each `saved_code/` file.
- **Exit:** `./gradlew build` green, no functional regressions.

### Phase 0.2 — Config infrastructure
- Stand up `registries/configs/` package.
- Register TOML `ModConfigSpec` for `common.toml`, `server.toml`, `client.toml`.
- Ship default JSON for `affinities.json`, `dimensions.json`, `portal.json`, `dragon.json`, `school.json`.
- `ConfigReloadListener` + `/elementalrealms reload`.
- `NamingRegistry` for display names.
- **Exit:** edit `affinities.json.roll.slotChances` in-game, run reload, behavior changes.

### Phase 0.3 — Drain §2.8 hardcoded backlog
- One issue per file / file group. Each PR replaces Java constants with config calls.
- **Exit:** `grep -R "static final.*=.*[0-9]" src/main/java/de/piggidragon/elementalrealms/` returns no balance numbers in gameplay paths.

### Phase 0.4 — Naming pass (TBATE cleanup)
- Search all `lang/*.json` + code strings for TBATE references.
- Replace with generic fantasy-academy terms.
- **Exit:** `grep -R -i "turtleme\|arthur.*leywin\|tessia\|virion\|xerus\|dicate\|alacrya" src/` returns nothing relevant.

### Phase 0.5 — Enchantment nerf (HIGH PRIORITY)
- Mixin into `EnchantmentProtection` and `EnchantmentSharpness`.
- Per-level multiplier from `enchantments.json` (default: Protection N → × 0.7; Sharpness N → × 0.6).
- Optionally extend to Sweeping, Smite, Bane.
- Advancement "Old Enchantments Weakened".
- **Exit:** in-game, full Protection IV armor reduces damage visibly less than vanilla; Sharpness V sword bonus visibly less.

### Phase 0.6 — Affinity bugfix pass
- Assign LEGENDARY/MYTHIC per `affinities.json.rarities`.
- Improve tier-validation error messages.
- Make `PlayerLoginHandler` defensive on tier-validation errors (log + skip, don't block login).
- Decide on Eternal shards (see §11).
- **Exit:** fresh-game / mid-game / endgame save game-tests all green.

### Phase 0.7 — Consolidation summary + sign-off
- `docs/PHASE-0-DECISIONS.md`, `docs/PHASE-0-SUMMARY.md`, `docs/ASSET-MODELS.md`, `docs/ASSET-TEXTURES.md`, `docs/ASSET-SOUNDS.md`.
- `AGENTS.md` + `.github/agents/docs-agent.md` review — keep / disable / rewrite.
- Optional: `.github/PULL_REQUEST_TEMPLATE.md` + `.github/ISSUE_TEMPLATE/{bug,feat}.yml`.
- User game-tests in-game and signs off before Phase 1 begins.

---

## 7. Phased Rollout Plan

Each phase: code PR (Draft) → user game-test → feedback → fix on same branch → sign-off → "Ready for review" → merge to `dev`. Asset tasks for the phase can begin after code PR is approved.

### Phase 1 — Dragon buff (1 week)
1. Ender Dragon 2× HP, mid-phase AoE. `dragon.json`.
2. Dragon phase 3 destroys obsidian pillars. `dragon.json`.
3. Dragon-AoE scaling by player count. `common.toml`.
4. Advancements "Dragonslayer", "Survivor".

### Phase 2 — School dimension content (2 weeks)
1. Affinity reveal mechanic — `revealed: boolean` attachment field.
2. Scroll of Awakening item.
3. School layout: 8 teacher books (common room), 6 lecture halls (one per Elemental), library.
4. Lore books (written-book items, generic fantasy-academy lore).
5. "First Awakening" advancement.
6. Lodestone ambient particles in School halls. Density from `client.toml`.

### Phase 3 — Spell API skeleton (2 weeks)
1. `Spell` interface (cast, cooldown, manaCost, targetType, VFX hooks) — all values from `spells.json`.
2. `SpellRegistry` (Affinity → List<Spell>).
3. `ManaCapability` (Curios "Mana Core" slot).
4. Spell cooldown tick in `ServerTickHandler`.
5. `SpellBookScreen` on Lodestone.
6. Spell hotkey + cast burst via Lodestone.
7. 1 sample spell per ELEMENTAL affinity works end-to-end.

### Phase 4 — Pocket dimensions + layouts (3 weeks)
1. Pocket ring layout (reusable Jigsaw templates: boss arena, hub, trap rooms, boundary wall).
2. 11 pocket-dimension JSONs.
3. `BoundedChunkGenerator` extended to RADIUS=31. Radius from `dimensions.json`.
4. `PocketRegistry` (Affinity → ResourceKey<Level>).
5. Portal logic rework: portal with `affinity_target` tag resolves to correct pocket.
6. Custom mob spawns in pockets. `mobs.json`.
7. Lodestone per-pocket ambient particles.

### Phase 5 — 11 bosses (3 weeks)
1. Boss entity base (vanilla boss bar, NOT Lodestone; phases, AoE, resistances). All stats from `bosses.json`.
2. BossArenaStructure Jigsaw system.
3. 11 bosses (1–2 days each). Names from `NamingRegistry`.
4. Boss drops (affinity stone + spell scroll + lore). Chances from `bosses.json.<affinity>.drops`.
5. Lodestone phase-transition VFX + screen shake. Intensity from `bosses.json`.

### Phase 6 — Custom mobs Overworld + Nether + End (2 weeks)
1. Spawn rules for 8–10 custom mobs per biome group. `mobs.json`.
2. Loot tables (shards common, stones very rare). `mobs.json.<mob>.loot`.
3. Mob drops integrated.
4. Lodestone aura particles per mob type.

### Phase 7 — GUI + polish (1–2 weeks)
1. Affinity GUI integration (saved_code), vanilla + Lodestone `ScreenAPI`.
2. Portal idle/teleport sound + rift shader (Lodestone). `portal.json`.
3. Advancement tree (one per phase).
4. Force-chunkload fix. Timeout from `common.toml`.
5. Structures-rooms diversity. Pool from datapack + structure variant list from `dimensions.json`.
6. Lodestone VFX pass — remove vanilla particle leftovers.

### Phase 8 — Optional endgame
1. Timer difficulty / corrupted world. `timer.json`. Lodestone corruption VFX.
2. Generic-themed endgame boss.
3. Multiplayer tooling: /pocket share, /pocket list.
4. **Wiki content drafting** — player guide, config reference, modpack author guide. Goes live on GitHub Wiki at first release.

---

## 8. Asset Pipeline

Three categories, three workflows. No asset work begins before Phase 0.7 sign-off.

### 8.1 Models (you build)
**Owner:** Piggidragon. Blockbench.

Tracked in `docs/ASSET-MODELS.md`:
- Affinity Stones (12+1), Shards (8), Void Stone, Scroll of Awakening
- Spell Scrolls (36 items, 1 base mesh + 36 textures)
- Mana Core (Curio), School Staff (model check), Affinity Book (closed + open)
- Portal Entity frame
- 11 boss entity models
- 8–10 custom mob models
- Mana Core Curios slot icon

My deliverable per model: spec block in the issue (Blockbench dimensions, bone hierarchy, attachment points, animation set, 1–2 reference mood-board links).

### 8.2 Textures (external artist)
**Owner:** TBD external. Briefs per phase, not upfront.

Tracked in `docs/ASSET-TEXTURES.md`:
- Item textures (stones/shards/scrolls/staff/mana core)
- Block textures (per-pocket ores, decorative blocks)
- Entity textures (11 bosses, 8–10 mobs, portal, mana core)
- GUI textures (affinity book, spell book, mana HUD, scroll)
- Particle sprites (12 affinity-themed sprite sheets)
- Sky / dimension textures (3 pocket-sky gradients)

My deliverable: per-phase brief — list, palette hex codes, mood-board links, must-include / must-avoid.

### 8.3 Sounds (you source: online + DIY)
**Owner:** Piggidragon. Mix of freesound.org + DIY.

Tracked in `docs/ASSET-SOUNDS.md`:
- Affinity use (fire crackle, water splash, etc.)
- Spell cast (each spell 2–3s loop)
- Boss themes (30–90s loops) + hit/death/phase-change stingers
- Portal idle + teleport
- Dragon laser (extend existing)
- UI stings

My deliverable: per-sound spec — duration, mood, loop seamlessness, peak loudness reference.

### 8.4 Asset workflow rules
- No asset work begins before Phase 0.7 sign-off.
- Each phase's PR lists "asset deps".
- Asset missing → code uses placeholder (purple-black cube / missing-texture sound), ships anyway.
- Tracker files live in `docs/`, git-tracked.

---

## 9. Collaboration & Game-Testing Workflow

### 9.1 Per-issue flow
1. **Plan** — issue spec updated here + GitHub.
2. **Branch** — issue-first, off `dev`.
3. **Code** — implement, run `mcp_intellij_build_project` until green.
4. **Push + Draft PR** — push branch, open **Draft PR** on GitHub.
5. **Test** — user game-tests from `run/saves/<test-world>`.
6. **Feedback** — bugs reported in plain text or PR comments. Each bug = fix commit on same branch.
7. **Sign-off** — user says "fine"/"good"/"ready" → I mark Draft PR "Ready for review".
8. **Merge** — user squash-merges to `dev`.

### 9.2 Testing gate
- **Build before push:** yes, always. Cheap. `mcp_intellij_build_project`.
- **Game test before merge:** yes, the real gate. User runs `./gradlew runClient`, plays, reports.
- **Game test before push:** not required.

### 9.3 Test worlds
You maintain test worlds in `run/saves/` (gitignored):
- `Test-FreshAffinities` — fresh survival; login roll, shard/stone use, reveal flow.
- `Test-DragonFight` — creative with End portal + dragon summoner; dragon buff tests.
- `Test-Pockets` — creative with pre-given affinity stones; pocket enter/exit + boss spawn.
- `Test-ConfigTuning` — fresh world; verify `/elementalrealms reload` applies config edits.

### 9.4 PR review + merge
- PR review by user on GitHub.
- User squash-merges to `dev`. I do not merge for you.
- `dev` → `main` only on release; you decide when.

---

## 10. Issue Proposals (grouped by phase)

Will be created via GitHub MCP once user says "go". Branch names derived from issue titles. **Every feature issue with `area/config` MUST include a config write step in its PR.**

### Phase 0
- `chore/phase0-code-review-and-rewrite`
- `chore/config-stand-up-config-infrastructure`
- `chore/config-add-config-reload-listener-and-reload-command`
- `chore/naming-introduce-naming-registry`
- `chore/phase0-drain-hardcoded-backlog-into-config-loaders`
- `chore/phase0-naming-pass-remove-tbate-references`
- `chore/phase0-review-saved-code-for-promotion`
- `chore/docs-review-AGENTS-md-and-docs-agent`
- `feat/enchantment-nerf-protection-and-sharpness`
- `fix/rarity-assign-legendary-mythic-to-affinity-stones`
- `fix/dimensions-persist-dynamic-dimension-counter`
- `fix/chunkgen-align-bounded-radius-with-ring-scan`
- `refactor/centralize-affinity-stone-shard-id-derivation`
- `docs/phase0-write-decisions-and-summary`
- `docs/assets-initialize-asset-trackers`
- (optional) `chore/github-add-pr-and-issue-templates`

### Phase 1 (dragon buff)
- `feat/dragon-buff-hp-and-mid-phase-aoe`
- `feat/dragon-disable-crystal-regen-in-phase-3`
- `feat/advancements-add-dragonslayer-and-survivor`

### Phase 2 (school)
- `feat/school-add-scroll-of-awakening-and-reveal-field`
- `feat/school-build-common-room-with-8-lore-bookshelves`
- `feat/school-build-6-lecture-halls-one-per-elemental-affinity`
- `feat/school-build-library-with-fantasy-academy-lore-books`
- `feat/advancements-add-first-awakening`
- `feat/school-add-lodestone-ambient-particles-to-school-halls`

### Phase 3 (spell API)
- `feat/spells-add-spell-interface-and-spell-registry`
- `feat/mana-add-mana-capability-bound-to-curios-mana-core-slot`
- `feat/events-extend-server-tick-handler-with-spell-cooldown-tick`
- `feat/gui-build-spell-book-screen-on-lodestone-screenapi`
- `feat/client-add-spell-hotkey-and-cast-burst-via-lodestone`
- `feat/spells-add-one-sample-spell-per-elemental-affinity-end-to-end`

### Phase 4 (pockets)
- `feat/worldgen-add-reusable-pocket-ring-jigsaw-template-set`
- `feat/worldgen-add-4-overworld-pocket-dimensions`
- `feat/worldgen-add-4-nether-pocket-dimensions`
- `feat/worldgen-add-3-end-pocket-dimensions`
- `feat/chunkgen-expand-bounded-chunk-generator-to-1000x1000-radius`
- `feat/pockets-add-pocket-registry-and-affinity-aware-portal-routing`
- `feat/spawns-add-affinity-specific-mob-spawn-rules-in-pocket-dimensions`
- `feat/pockets-add-per-pocket-lodestone-ambient-particles`

### Phase 5 (bosses)
- `feat/boss-add-boss-entity-base-class-with-vanilla-boss-bar-phases-aoe`
- `feat/boss-add-boss-arena-structure-jigsaw-system`
- 11× `feat/boss-<affinity>-implement-<boss-name>` (boss names from `NamingRegistry`)
- `feat/loot-add-boss-drops-loot-table`
- `feat/boss-add-lodestone-phase-transition-vfx-and-screen-shake`

### Phase 6 (custom mobs)
- `feat/mobs-add-overworld-elemental-mobs-with-biome-spawn-rules`
- `feat/loot-add-affinity-shard-drops-to-custom-mob-loot-tables`
- `feat/mobs-add-lodestone-aura-particles-per-custom-mob-type`

### Phase 7 (GUI + polish)
- `feat/gui-integrate-affinity-book-screen-and-hud-overlay`
- `feat/portal-add-idle-and-teleport-sounds-and-rift-shader`
- `feat/advancements-add-progression-advancement-tree-covering-all-phases`
- `fix/chunkload-centralize-chunk-force-api-for-boss-arenas`
- `feat/worldgen-add-random-variant-pool-for-trap-rooms`
- `feat/client-full-lodestone-vfx-pass`

### Phase 8 (optional endgame)
- `feat/endgame-add-corrupted-world-timer-difficulty`
- `feat/endgame-add-generic-themed-endgame-boss`
- `feat/multiplayer-add-pocket-share-and-pocket-list-commands`
- `docs/wiki-draft-player-guide-config-reference-modpack-guide`

---

## 11. Open Design Questions

Status legend: `[ ]` unanswered, `[x]` answered, `[?]` superseded.

We add new questions as they come up. When you answer, I update the relevant sections of the plan and link your decision back here.

### Phase 0 critical (answer before Phase 0 starts)

- [ ] **Config format?** — TOML only / JSON only / hybrid TOML+JSON?
- [ ] **JSON5 or plain JSON for `config/*.json`?**
- [ ] **Hot-reload scope?** — full hot-reload / lazy / warn-and-restart-required?
- [ ] **Reveal mechanic timing?** — when can spells be cast relative to reveal? Before, after, both?
- [ ] **NamingRegistry scope?** — boss names + spell names + dimension names + advancement titles? Just bosses?
- [ ] **Boss names final?** — use §1 placeholders, swap in your own, or wait?
- [ ] **Saved_code triage?** — for each file, promote / archive / discard?
- [ ] **Enchantment nerf defaults?** — multipliers for Protection / Sharpness / Sweeping / Smite / Bane?
- [ ] **Eternal shards?** — should they exist, or are Eternal stones the only way?

### Phase 0 nice-to-have (during Phase 0)

- [ ] **AGENTS.md + docs-agent fate?** — keep / disable / rewrite?

### Phase 1 (dragon buff)

- [ ] **Dragon HP multiplier default?** — 2× / 2.5× / 3×?
- [ ] **Dragon-AoE by player count?** — linear / exponential / off?
- [ ] **Obsidian-pillar destruction in phase 3?** — destroy all / nearest / config-driven?

### Phase 2 (school)

- [ ] **Affinity-reveal UI?** — what does player see between login and reveal? Just count? Vague flavor?
- [ ] **Scroll of Awakening loot source?** — chest in School library / NPC drop / crafted?
- [ ] **Lecture hall behavior?** — decorative only / functional (right-click for lore book)?

### Phase 3 (spell API)

- [ ] **Spells as items or hotbar slots?** — items / Curios / Spell-Book GUI / combination?
- [ ] **Mana Curio?** — starter item / crafted-only / both?
- [ ] **Spell damage scaling?** — fixed / per-level / per-mana / fixed+affinity bonus?

### Phase 4 (pockets)

- [ ] **Pocket layout per affinity deterministic?** — same layout, different seed per portal-id?
- [ ] **Pocket size?** — 1000×1000 fixed / config-driven?
- [ ] **Pocket persistence?** — saved world / regenerate on portal re-enter / configurable?

### Phase 5 (bosses)

- [ ] **Boss HP scaling by player count?** — yes / no / config-driven?
- [ ] **Boss drop chances?** — fixed default / config-driven per-boss?
- [ ] **Boss theme / names?** — use §1 placeholders / user-supplied names?

### Phase 6 (mobs)

- [ ] **Overworld mob spawn rate?** — 5–10% / config-driven / per-mob?
- [ ] **Custom mob count?** — 8–10 / different number / per-affinity?

### Phase 7 (polish)

- [ ] **Advancement tree depth?** — one per phase / one big tree / minimal?
- [ ] **Force-chunkload timeout default?**

### Phase 8 (endgame)

- [ ] **Endgame boss required?** — yes / optional / only via config?
- [ ] **Corrupted-world mechanic scope?** — world modifier / dimension / arena?

### Cross-phase

- [ ] **Lodestone coverage?** — new spells/mobs/bosses use Lodestone (VFX only); existing particles stay vanilla. OK?
- [ ] **Boss-HP-bar visibility?** — always within 64 blocks + LoS / always / on-damage only?
- [ ] **Multiplayer edge cases** — 2+ players enter same pocket, boss kill sync, return-position race?
- [ ] **Modpack vs datapack priority?** — when both exist, who wins for shared fields?
- [ ] **Client config sync?** — strictly per-player (no server push)?
- [ ] **Schema versioning strictness?** — strict by default with dev-toggle / always strict / always lenient?

---

## 12. GitHub Workflow

### 12.1 Branch protection (do once in GitHub UI)
- `main`: protected, no direct push, requires PR + review.
- `dev`: open for direct push (mini fixes).

### 12.2 Labels (create in GitHub UI — Settings → Labels)

**area/\*** (color `#c5def5` light blue):
`area/config`, `area/worldgen`, `area/magic`, `area/spells`, `area/bosses`, `area/mobs`, `area/gui`, `area/portal`, `area/lodestone`, `area/enchantment`, `area/assets`, `area/dragon`, `area/school`, `area/affinity`, `area/multiplayer`

Standard GitHub defaults usually already exist: `bug`, `documentation`, `duplicate`, `enhancement`, `good first issue`, `help wanted`, `invalid`, `question`, `wontfix`.

### 12.3 Draft PRs

Every PR is created as `draft: true` via MCP. You see `[Draft]` tag in your PR list, can comment inline, click through to the diff, watch CI status. When you say "fine" → I mark it Ready for Review → you merge.

### 12.4 Templates
Skill `elementalrealms-workflow` is the canonical place for issue/PR layouts. Optional belt-and-suspenders: also add `.github/PULL_REQUEST_TEMPLATE.md` and `.github/ISSUE_TEMPLATE/{bug,feat}.yml` as files. Up to you.

### 12.5 Milestones
Not using GitHub milestones. Sequential work, blocking deps tracked in head.

---

## 13. Next Steps

1. **User reads this plan**, comments / corrects in reply.
2. **Go through §11 questions Phase 0 first** — answer the 9 Phase-0-critical ones. We figure out more as we go.
3. **Branch protection on `main`** (you do, GitHub UI, 30s).
4. **Create the labels** from §12.2 (you do, GitHub UI, 2 min).
5. **Phase 0 work begins** with the first agreed-on issue.
6. **PRs from now on are Draft PRs** until you sign off.

---

*Created 2026-06-25. Live document — we iterate as we go.*
