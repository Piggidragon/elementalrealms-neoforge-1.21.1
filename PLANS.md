# Elemental Realms — Master Plan

> **Plan version:** v12
>
> **For Piggidragon:** Live document. Part A is the feature overview, Part B is the technical build plan, Part C is the live working area + idea scratchpad.

**Credits:** Inspired by "The Beginning After the End" by TurtleMe (general fantasy-academy setting, elemental magic, staged progression). No characters, names, or plot points from the source material are used.

**Status:** in development. No timeline pressure. Performance: lean and sensible.

**Goal:** Generic fantasy-academy progression mod. Player kills a hard Ender Dragon → unlocks a School dimension + 11 elemental pocket dimensions with bosses, spells, custom mobs.

**Architecture:** NeoForge 1.21.1, Infiniverse for dynamic dimensions, Lodestone for VFX only, all tunables as config data, all display names through one registry.

**Versioning:** SemVer-ish. `0.X.0` = major (breaking for players or modpack authors). `0.0.X` = minor (additive).

---

## 0. Glossary

- **Affinity** — elemental alignment a player holds. 12 + Void.
- **Tier** — affinity category: NONE / ELEMENTAL / DEVIANT / ETERNAL.
- **Deviant** — advanced affinity; requires its base at 100% (FIRE → LIGHTNING).
- **Eternal** — ultimate; mutually exclusive, one per player.
- **Void** — "no affinity" state. Clears all.
- **Pocket** — small isolated world per affinity, via portal.
- **Ring** — concentric zone inside a pocket (outer ring = spawn, center = boss arena).
- **Boss** — per-affinity pocket boss.
- **Spell** — castable magical action. Costs mana + cooldown.
- **Stone / Shard** — items granting +100% / +5% of an affinity. Consumed.
- **Crystal Orb of Awakening** — School item revealing hidden affinities.
- **Reveal** — transition from "unknown affinities" to "known affinities".
- **Mana Core** — central place for mana pool (TBD: menu / inventory / etc.).
- **Roll** — first-login random assignment of affinities.
- **Mage / Warrior / Bow** — spell archetypes (Mage first, Warrior/Bow later).
- **NamingRegistry** — single source for boss / spell / dimension display names.
- **Affinity mob** — vanilla mob with an affinity tag + particles. Drops shards only.
- **Modded mob** — fully custom entity. Drops affinity-specific loot.
- **Barrier stage** — progression marker (Dragon killed / Elemental boss killed / Deviant boss killed) that unlocks new content tiers.

# PART A — FEATURES & GAMEPLAY

---

## 1. Vision & Setting

Generic fantasy-academy world. The player enters a School dimension after the Dragon fight, learns about affinities, explores 11 elemental pocket dimensions, and grows stronger through spellcasting, boss kills, and progressive mastery.

Everything user-facing flows through `NamingRegistry` so anyone can re-theme freely.

---

## 2. Player Lifecycle

| Step | Where | What happens |
|---|---|---|
| **0. Login** | Overworld | One ELEMENTAL affinity auto-assigned at 100%. Other affinities (incl. deviants) get small partial rolls. Affinities **client-invisible**. |
| **1. Early progression** | Overworld | Player finds **rare** affinity shards from vanilla structures (and custom structures distributed across vanilla dimensions) and very rarely from affinity mobs. Item name tells the player which affinity each shard helps ("Affinity Shard of Fire"). Builds toward stronger affinity completion. |
| **2. Dragon fight** | End | Hard dragon, maxed Netherite + skill required (see §3). |
| **3. Dragon kill** | End | A custom structure spawns at Overworld spawn with a permanent portal to the School. Broadcast message. Pocket dimensions become technically accessible but barriere-gated (see §6). |
| **4. School entry** | Staff or portal | Staff lets the player reach the School from anywhere in vanilla dimensions (no need to walk back to spawn). Fantasy-academy hub with the Crystal Orb of Awakening, which reveals affinities. |
| **5. First pocket** | Anywhere in vanilla | Player chooses which pocket to enter; no designated "first pocket." Once inside, exiting is hard — survival is the cost of entry. No guarantee which pocket it is, only which tier is active. Boss kill may or may not drop an affinity stone (stone is the ultimate reward, not guaranteed). |
| **6. Deviant stage** | After Elemental boss kill | Deviant mobs start spawning in vanilla dimensions. Deviant pocket dimensions accessible. |
| **7. Eternal stage** | After Deviant boss kill | Eternal mobs start spawning in End. Eternal pocket dimensions accessible (still very hard). |
| **8. Endgame** | optional | Corrupted-world timer difficulty, generic endgame boss. |

When a boss of a specific affinity dies, the matching portal disappears from vanilla dimensions (forces the player to seek other content instead of farming the same portal).

---

## 3. Dragon Rework (TrueEnd-inspired)

The Ender Dragon is the **gate to the School dimension** — the warden of the dimension barrier. Killing it cracks the barrier. It must be hard enough that maxed Netherite + skill is the baseline, not overkill.

**Core mechanics** (full list in IDEAS.md):

- Significant HP multiplier
- Multiple phases escalating aggression and attacks
- Dragon breath / meteor shower / exploding fireballs
- Aggressive AI that actively targets players (not just orbit)
- Perch knockback + damage waves in every direction
- Stand-still punishment attack (1-hit kill for staying put)
- Summon adds in later phases (custom mobs)
- End Crystals as enemies — attack players, self-regenerate, destroying them spawns mobs
- Climatic finish with strong visual moment

**Reference:** inspired by TrueEnd datapack (similar overhaul).

---

## 4. School Dimension

A fantasy-academy hub the player enters after killing the Dragon.

**Contents:**
- **Crystal Orb of Awakening** — reveals all affinities. Without it, the player only sees "X unknown powers slumber within you."
- **Lore library** — written-book items about affinities, the 12 worlds, the boss lineage
- **Lecture halls** — one per Elemental affinity + Deviant halls
- **Common room** — cross-affinity lore bookshelves

**Access:**
- Permanent portal at Overworld spawn (in a custom structure that spawns with the portal after Dragon kill)
- **Dimension Staff** — reaches the School from anywhere in vanilla dimensions (uses count TBD)

---

## 5. Affinity System

### 5.1 The 12 affinities

| Tier | Affinities |
|---|---|
| ELEMENTAL | Fire, Water, Wind, Earth |
| DEVIANT | Lightning (←Fire), Ice (←Water), Sound (←Wind), Gravity (←Earth) |
| ETERNAL | Life, Space, Time |
| NONE | Void |

### 5.2 Login roll

One ELEMENTAL affinity at 100%, plus partial percentages on others (incl. deviants). Distribution skewed: higher percentages rarer, more affinities held at once rarer, deviants rarer than elementals, eternals not given at login.

### 5.3 Learning more affinities

- **Affinity shard** — +5% of one specific affinity. Item name identifies the affinity.
- **Affinity stone** — instant 100% of one affinity. The ultimate reward; only the boss can drop it (or specific endgame content).
- **Void stone** — clears all affinities.

Affinity items are **never revealed** — players always know which shard helps what.

### 5.4 Tier visibility

After login: player sees only "X unknown powers slumber within you." The Crystal Orb of Awakening reveals everything.

### 5.5 Affinity scaling & vanilla effects

Affinities scale at any percentage — even 5% gives small benefits, 100% gives strong effects. At high completion, affinities grant **vanilla MC effects** (e.g. Fire 100% = permanent fire immunity, Water 100% = water breathing + dolphin grace, etc.). Full list TBD in IDEAS.md. Building affinity is useful even without using spells.

### 5.6 Boss-name placeholders

Names live in `NamingRegistry`. Boss walkstyle, behavior, and mechanics not yet decided.

| Affinity | Boss placeholder |
|---|---|
| FIRE | Lord of Embers |
| WATER | Tidal Sovereign |
| EARTH | The Stoneheart |
| WIND | Stormwing Roc |
| LIGHTNING | Stormcaller |
| ICE | The Frostbound King |
| SOUND | The Resonant Wraith |
| GRAVITY | The Singularity |
| LIFE | World-Tree Avatar |
| SPACE | The Void-Walker |
| TIME | Chrono-Warden |

### 5.7 Eternal affinities

Eternal affinities are absolute mysteries. There are **no eternal shards** — only the boss stone. All-or-nothing. Only one per player, because holding more than one would tear them apart. The three eternals together are the foundation of existence.

---

## 6. Pocket Dimensions

### 6.1 Layout

3 concentric rings, player spawns at the **outer ring** and fights inward.

| Ring | Contents |
|---|---|
| **Outer (spawn)** | Vanilla + affinity mobs, small structures, semi-safe start |
| **Intermediate (largest)** | Modded mobs only, larger structures, mini-bosses, traps |
| **Center** | Boss arena, themed by affinity |

Outside the outer ring = air void. Pocket sizes vary per affinity — balance decided during Phase 4 build.

### 6.2 Per-affinity theme

Each pocket has a unique environmental theme matching its affinity (Fire = volcano, Water = ocean trench, Wind = floating islands, Earth = underground cavern, Lightning = charged storm, Ice = frozen peak, Sound = echo cavern, Gravity = inverted/floating shards, Life = overgrown ruin, Space = void islands, Time = clockwork ruins). Theme is seed-dependent — same Fire pocket looks different per portal-id, but always volcano-centered.

### 6.3 Dimensional effects

Each pocket has **negative passive environmental effects**. The higher the matching affinity the player has, the weaker the effect. At 100% matching affinity, the effects flip to buffs. Players without the matching affinity are punished; players with it are rewarded.

Examples (full list TBD): Fire dim burns you faster; Water dim drowns you faster; Wind dim pushes you around; Earth dim slows movement; etc.

### 6.4 The 11 pockets

| Tier | Pockets | Where portals appear |
|---|---|---|
| Overworld | Wind, Fire, Water, Earth | Anywhere in vanilla Overworld |
| Nether | Sound, Gravity, Lightning, Ice | Anywhere in vanilla Nether |
| End | Life, Space, Time | Anywhere in vanilla End |

Portals can spawn **anywhere** in the corresponding vanilla dimension.

### 6.5 Boss drops

Boss kill is not guaranteed to drop an affinity stone — stones are the ultimate reward. Other drops: equipment, spells, lore items, etc.

When the boss of an affinity dies, the matching portal **disappears from vanilla dimensions** (encourages variety over farming).

---

## 7. Mobs

### 7.1 Affinity mobs

Affinity mobs are **vanilla mobs with an affinity tag attached**. They spawn rarely across all vanilla dimensions (frequency tier-gated). They emit Lodestone particles so their affinity is visible. They drop affinity shards **only** — never stones.

| Tier | Where they spawn |
|---|---|
| Elemental | Overworld + Nether + End |
| Deviant | Nether + End (unlocked after first Elemental boss kill) |
| Eternal | End only (unlocked after first Deviant boss kill) |

### 7.2 Modded mobs

Fully custom entities. Spawn in pocket dimensions only (densely, affinity-specific). Drop affinity-specific loot including high-tier equipment, scrolls, and lore. Stronger than affinity mobs; tuned for pocket content.

---

## 8. Spells

### 8.1 Three spell archetypes

| Archetype | Trigger | Scope |
|---|---|---|
| **Mage** | Hotbar / spell-book, projectiles & AoEs | Phase 3 first |
| **Warrior** | Off-hand held, melee/buffs | Later |
| **Bow** | Bow enchantment-mods | Later |

### 8.2 Mage spells (Phase 3)

12 sample mage spells (3 per ELEMENTAL affinity):

| Affinity | Basic | Utility | Ultimate |
|---|---|---|---|
| FIRE | Fire Bolt | Flame Shield | Meteor |
| WATER | Water Bolt | Tidal Wave | Healing Rain |
| WIND | Wind Slash | Dash | Tornado |
| EARTH | Rock Throw | Earthen Wall | Quake |

Deviant + Eternal mage spells come later. Per-archetype counts per affinity are not fixed.

### 8.3 Combo spells

Spells can combine affinities (Wind + Lightning = Storm, Water + Ice = Blizzard, etc.). Exact combinations and effects TBD in IDEAS.md.

### 8.4 Mana

Mana is a **separate resource** (like hunger). Held in a central **mana system** — implementation TBD (menu, inventory slot, or skills-tree UI). The core upgrades over time (more mana, faster regen). Exact upgrade path decided during Phase 3.

### 8.5 Spell costs & scaling

All values configurable per spell in `config/elementalrealms/spells.json`. Damage scaling: fixed + affinity-completion bonus.

---

## 9. Bosses

11 bosses (one per affinity, Void has none). Each in a pocket's center arena, themed to the affinity.

**Mechanics:**
- Vanilla HP bar above head
- Phases with new attack patterns + Lodestone VFX cues
- Resists own affinity, weak to deviant/base counter
- AoE attacks per phase
- Lodestone transition FX (full-screen shake + radial particle burst + screen flash on phase change)

**Boss walking / behavior specifics are not yet decided.**

**Drops (always):** spell scrolls, equipment, lore items. **Affinity stone is NOT guaranteed** — it's the ultimate reward, possibly the rarest drop in the game.

---

## 10. Balance Changes

### 10.1 Enchantment nerf

Protection, Sharpness, Sweeping, Smite, Bane of Arthropods all scaled back. Reason: vanilla top-tier enchantments are too game-changing — this is a general MC balance issue, not just for spell users. Multipliers per enchantment in `enchantments.json` (configurable).

### 10.2 Ominous-Potion-Scaling

Potion effects scale with difficulty / location, Trial-Chamber-style. Configuration in `enchantments.json` or its own file.

---

## 11. Progression & Advancements

One advancement tree per phase. Milestones include first-login, first shard, Dragon kill, Awakening, first pocket entry, first boss kill, tier completions, no-laser / no-death Dragon clears, combo spells learned, etc. Full tree built during each phase.

---

## 12. UI / GUI

- **Affinity Book** (Lodestone ScreenAPI) — shows affinities with completion %, picks active spells, toggles VFX density.
- **Mana UI** — exact placement TBD (skills-tree UI is one option).
- **Portal rift shader** — Lodestone distortion around active portal frames.

---

## 13. Performance Philosophy

Lean perf: as many particles as needed, as few as sensible. Spell VFX LOD-aware (full density close, half mid, minimal far). Boss transitions temporary. Pocket gen caps to bounded ring. We do not target potato-PC — users opt into Sodium/Embeddium separately.

---

## 14. Multiplayer Behavior

Multiplayer-safe for single-pocket scenarios (boss kill sync, return position per-player). Multi-player edge cases (simultaneous pocket entry, boss kill sync) handled per-phase. Single-player testing is the main gate.

---

## 15. Controversial Design Decisions

### 15.1 The Ender Dragon is the hard gate
Hard dragon, maxed Netherite + skill required. Lore: warden of dimension barrier, killing it cracks the barrier.

### 15.2 Enchantments nerfed globally
Reduced for all players (not just spell users). Reason: vanilla enchantments are too game-changing — general MC balance issue.

### 15.3 Pockets are gated by barrier-progression, not by affinity
After Dragon kill, all pockets technically accessible. But new tiers (Deviant, Eternal) unlock only after the previous tier's boss kill. Tier-gated mob spawns follow the same pattern.

### 15.4 Pockets don't require affinity to enter
No hard requirement. Players without the matching affinity are punished by dimensional effects; with the affinity, they're rewarded. Soft gate.

### 15.5 Affinity state hidden until Reveal
Mystery, drives shard collection, makes Reveal a moment. Item names still tell you which affinity a shard helps.

### 15.6 Boss stone drop is not guaranteed
Affinity stones are the ultimate reward. Other drops (equipment, spells, lore) are reliable. The stone is rare even from the boss.

### 15.7 Boss death removes the matching portal
Once a boss is killed, the matching affinity's vanilla portal disappears. Encourages exploration over farming.

### 15.8 Pocket dimensions are persistent
Boss killed = stays killed. Saved world. Modpack-configurable to regenerate.

### 15.9 Eternal affinities are mystery + all-or-nothing
No eternal shards — only boss stones. Mutually exclusive. The three together are the foundation of existence.

### 15.10 PvP balance is not a design goal
Spells designed for PvE only. PvP mods exist separately.

---

# PART B — TECHNICAL BUILD PLAN

---

## 16. Phased Rollout Plan

Each phase: code PR (Draft) → user game-test → feedback → fix on same branch → sign-off → "Ready for review" → merge to `dev`. Asset tasks can begin after code PR approval.

### Phase 0 — Full Consolidation Pass

See §17.

### Phase 1 — Dragon Rework
TrueEnd-inspired overhaul. HP multiplier, phases, breath/meteor/fireball attacks, perch knockback waves, stand-still punishment, summon adds, crystal aggression + regen, climatic finish, aggressive AI sweep.

### Phase 2 — School + Crystal Orb of Awakening
- Crystal Orb item
- School dimension content (lecture halls, library, common room)
- Custom structure at Overworld spawn with portal (spawns together with portal after Dragon kill)
- Affinity reveal mechanic — `revealed: boolean` attachment field
- Dimension Staff (use count TBD)
- Affinity Book GUI foundation

### Phase 3 — Spell API + 12 mage sample spells
- `Spell` interface — values from `spells.json`
- `SpellRegistry` (Affinity → List<Spell>)
- Mana bar (vanilla GuiGraphics)
- Mana system TBD (menu / inventory slot / skills-tree UI)
- SpellBookScreen on Lodestone
- Spell hotkey + cast burst via Lodestone
- 12 sample spells end-to-end

### Phase 4 — Pocket Dimensions + Layouts
- Pocket ring layout (reusable Jigsaw templates: outer spawn, intermediate, center boss arena)
- 11 pocket-dimension JSONs with per-affinity themes
- Variable pocket size (balanced during build)
- `BoundedChunkGenerator` extension
- `PocketRegistry` (Affinity → ResourceKey<Level>)
- Portal logic: portal with `affinity_target` tag resolves to correct pocket
- Negative dimensional effects per pocket; flips to buffs at 100% matching affinity
- Boss death removes matching vanilla portal

### Phase 5 — 11 Bosses
- Boss entity base (vanilla boss bar, phases, AoE, resistances)
- BossArenaStructure Jigsaw system
- 11 bosses (boss names from `NamingRegistry`)
- Boss drops: equipment, spells, lore items (always); affinity stone (rare)
- Lodestone phase-transition VFX + screen shake

### Phase 6 — Custom Mobs (Affinity + Modded)
- Affinity mobs (vanilla mobs + affinity tag + particles) spawning rarely across vanilla dimensions
- Tier-gating: Elemental everywhere; Deviant unlocked after first Elemental boss kill; Eternal unlocked after first Deviant boss kill
- Affinity mobs drop shards only (no stones)
- Modded mobs in pockets only — fully custom, density tuned, high-tier drops

### Phase 7 — GUI + Polish
- Affinity Book full implementation on Lodestone
- Mana UI finalized
- Portal idle/teleport sound + rift shader (Lodestone)
- Advancement tree per phase
- Force-chunkload fix
- Lodestone VFX pass

### Phase 8 — Endgame
- Corrupted-world timer difficulty
- Generic-themed endgame boss
- Multiplayer tooling
- Wiki content at first public release (player guide, config reference, modpack guide)

### Phase 9+ — Spell archetype expansion
1. Deviant mage spells
2. Eternal mage spells
3. Combo spells (Wind+Lightning, Water+Ice, etc.)
4. Warrior archetype
5. Bow archetype

### Phase 9+ — Pre-Dragon content additions
1. Lightweight affinity mobs in vanilla dimensions (always available, small-scale)
2. Shard-glow visual hint on player's hand
3. "Monster manual" hint item

### Phase 9+ — Idea-park (interesting but not scheduled)
- Overworld raids from pocket dimensions (mobs + mini-bosses spill into vanilla worlds)
- Corrupted-world scaling (depends on Warrior/Bow existing)

---

## 17. Phase 0 — Full Consolidation Pass

Runs before any new feature. Existing code reviewed, rewritten where needed, config-driven.

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

### Phase 0.3 — Drain hardcoded backlog
- Replace Java constants with config calls.
- **Exit:** `grep -R "static final.*=.*[0-9]" src/main/java/de/piggidragon/elementalrealms/` returns no balance numbers in gameplay paths.

### Phase 0.4 — Naming pass
- Search `lang/*.json` + code strings for any external references.
- Replace with generic fantasy-academy terms.
- **Exit:** `grep -R -i "turtleme\|arthur.*leywin\|tessia\|virion\|xerus\|dicate\|alacrya" src/` returns nothing relevant.

### Phase 0.5 — Enchantment nerf
- Mixin into `EnchantmentProtection` and `EnchantmentSharpness`.
- Per-level multiplier from `enchantments.json`.
- Optionally extend to Sweeping, Smite, Bane.
- Advancement "Old Enchantments Weakened".

### Phase 0.6 — Affinity bugfix pass
- Assign LEGENDARY/MYTHIC per `affinities.json.rarities`.
- Improve tier-validation error messages.
- Make `PlayerLoginHandler` defensive on tier-validation errors.
- Rewrite roll logic: one 100% + partial percentages for rest, rarity-skewed (see §5.2).

### Phase 0.7 — Consolidation summary + sign-off
- `docs/PHASE-0-DECISIONS.md`, `docs/PHASE-0-SUMMARY.md`, `docs/ASSET-MODELS.md`, `docs/ASSET-TEXTURES.md`, `docs/ASSET-SOUNDS.md`.
- `AGENTS.md` + `.github/agents/docs-agent.md` review.
- User game-tests and signs off before Phase 1.

---

## 18. System Architecture

### 18.1 Lodestone scope

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

NOT Lodestone: boss HP bars (vanilla `BossEvent`), Ender Dragon HP bar (vanilla), general entity rendering, mana bar / simple HUD (vanilla `GuiGraphics`).

### 18.2 Configuration-first

Every magic number is data, never a Java constant. Three layers:

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

Hot-reload via `/reload` or `ModConfigEvent.Reloading`. Lazy-apply: new entities read new values, existing entities keep snapshot. New `/elementalrealms reload` command via `ConfigReloadListener`. `"schemaVersion": 1` per JSON. Strict by default with `common.toml.allowSchemaMismatch` toggle.

### 18.3 Module tree

```
registries/
├── items/magic/
│   ├── affinities/         [✓ — re-review in Phase 0]
│   ├── misc/               [✓ — SchoolStaff]
│   ├── spells/             [NEW]
│   └── affinitytools/      [NEW — Crystal Orb]
├── worldgen/
│   ├── structures/pockets/ [NEW]
│   ├── structures/bosses/  [NEW]
│   ├── structures/spawn/   [NEW — Overworld spawn structure for School portal]
│   └── features/pocketmobs/[NEW]
├── entities/custom/
│   ├── bosses/             [NEW]
│   └── elementals/         [NEW — modded mobs]
├── level/PocketRegistry.java
├── configs/
│   ├── ModConfigs.java, ConfigReloadListener.java, NamingRegistry.java
│   └── {Affinity,Dimensions,Spells,Bosses,Mobs,Portal,Dragon,School,Enchantments,Timer}Config.java
└── commands/ModCommands.java
magic/
├── affinities/             [✓ — roll logic rewrite in 0.6]
├── spells/
│   ├── Spell.java, SpellRegistry.java, SpellCasting.java
│   ├── SpellBookScreen.java (Lodestone)
│   └── impl/{FireSpells, WaterSpells, ...}.java
└── mana/                    [NEW — implementation TBD: menu / inventory slot / skills-tree UI]
client/
├── lodestone/              [NEW]
│   ├── AffinityParticles.java, PortalRiftRenderer.java
│   ├── LaserBeamRenderer.java (PROMOTE from saved_code)
│   └── ScreenShakeHook.java
events/
├── DragonDeathHandler.java [✓ — overhaul in Phase 1; spawns School structure + portal]
├── PlayerLoginHandler.java [✓ — rewrite roll in 0.6]
├── ServerTickHandler.java
└── PocketDimensionBuilder.java [NEW]
```

---

## 19. Configuration Subsystem

| File | What's in it |
|---|---|
| `common.toml` | Toggles, multipliers, audio sliders |
| `server.toml` | Server-only tunables |
| `client.toml` | Client-only (HUD, particles, audio) |
| `affinities.json` | Roll chances, deviant mapping, rarities, vanilla-effect thresholds |
| `dimensions.json` | Pocket size, ring layout, affinity → pocket map, dimensional effects, affinity-buff thresholds |
| `spells.json` | Spell definitions (damage, cooldown, mana, VFX hooks, combo rules) |
| `bosses.json` | Boss stats per affinity |
| `mobs.json` | Affinity mob spawn rules + tier-gating; modded mob spawn rules for pockets |
| `portal.json` | Portal config |
| `dragon.json` | HP multiplier, phase transitions, attack configs |
| `school.json` | School + Crystal Orb + Dimension Staff config |
| `enchantments.json` | Protection/sharpness nerf multipliers + potion scaling |
| `timer.json` | Corrupted-world timer difficulty |

---

## 20. Asset Pipeline

Three categories, three workflows. No asset work begins before Phase 0.7 sign-off.

### 20.1 Models (you build)
**Owner:** Piggidragon. Blockbench.

Tracked in `docs/ASSET-MODELS.md`:
- Affinity Stones (12+1), Shards (8), Void Stone, Crystal Orb of Awakening
- Spell Scrolls
- Mana Core / Skills-tree UI element (TBD)
- School Staff, Affinity Book, Portal Entity frame
- 11 boss entity models
- Modded mob models (pocket content)

### 20.2 Textures (external artist)
**Owner:** TBD external. Briefs per phase, not upfront.

### 20.3 Sounds (you source: online + DIY)
**Owner:** Piggidragon. Mix of freesound.org + DIY.

### 20.4 Asset workflow rules
- No asset work before Phase 0.7 sign-off.
- Each PR lists "asset deps".
- Asset missing → placeholder, ship anyway.

---

## 21. GitHub Workflow

### 21.1 Branch protection (do once in GitHub UI)
- `main`: protected, no direct push, requires PR + review.
- `dev`: open for direct push (mini fixes).

### 21.2 Labels (create in GitHub UI — Settings → Labels)

**area/\*** (color `#c5def5`):
`area/config`, `area/worldgen`, `area/magic`, `area/spells`, `area/bosses`, `area/mobs`, `area/gui`, `area/portal`, `area/lodestone`, `area/enchantment`, `area/assets`, `area/dragon`, `area/school`, `area/affinity`, `area/multiplayer`

### 21.3 Draft PRs

Every PR created as `draft: true` via MCP. When you say "fine" → I mark it Ready for Review → you merge.

### 21.4 Templates
Skill `elementalrealms-workflow` is canonical. Optional belt-and-suspenders: `.github/PULL_REQUEST_TEMPLATE.md` + `.github/ISSUE_TEMPLATE/{bug,feat}.yml`.

### 21.5 Milestones
Not used. Sequential work, blocking deps tracked in head.

---

## 22. Test Worlds

You maintain in `run/saves/` (gitignored):

| World | Purpose |
|---|---|
| `Test-FreshAffinities` | Fresh survival; login roll, shard use, reveal flow |
| `Test-DragonFight` | Creative + End portal + dragon summoner; dragon buff tests |
| `Test-Pockets` | Creative + pre-given affinity stones; pocket enter/exit + boss spawn |
| `Test-ConfigTuning` | Fresh world; verify `/elementalrealms reload` applies config edits |

---

## 23. Issue Proposals

Branch names derived from issue titles.

### Phase 0
- `chore/phase0-code-review-and-rewrite`
- `chore/config-stand-up-config-infrastructure`
- `chore/config-add-config-reload-listener-and-reload-command`
- `chore/naming-introduce-naming-registry`
- `chore/phase0-drain-hardcoded-backlog-into-config-loaders`
- `chore/phase0-naming-pass-remove-external-references`
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

### Phase 1 (Dragon Rework)
- `feat/dragon-buff-hp-multiple-phases`
- `feat/dragon-add-breath-and-meteor-attacks`
- `feat/dragon-add-perch-knockback-waves`
- `feat/dragon-add-stand-still-punishment`
- `feat/dragon-summon-adds-in-phases`
- `feat/dragon-crystals-attack-and-regenerate`
- `feat/dragon-climatic-finish`
- `feat/dragon-aggressive-ai-sweep`

### Phase 2 (School)
- `feat/school-add-crystal-orb-of-awakening-and-reveal-field`
- `feat/school-build-common-room-with-8-lore-bookshelves`
- `feat/school-build-6-lecture-halls-one-per-elemental-affinity`
- `feat/school-build-library-with-fantasy-academy-lore-books`
- `feat/school-spawn-structure-with-portal-at-overworld-spawn`
- `feat/items-add-dimension-staff-with-from-anywhere-teleport`
- `feat/advancements-add-first-awakening`

### Phase 3 (Spell API + mage samples)
- `feat/spells-add-spell-interface-and-spell-registry`
- `feat/mana-add-mana-bar-and-mana-core`
- `feat/events-extend-server-tick-handler-with-spell-cooldown-tick`
- `feat/gui-build-spell-book-screen-on-lodestone-screenapi`
- `feat/client-add-spell-hotkey-and-cast-burst-via-lodestone`
- `feat/spells-add-one-sample-spell-per-elemental-affinity-end-to-end`

### Phase 4 (Pockets)
- `feat/worldgen-add-reusable-pocket-ring-jigsaw-template-set`
- `feat/worldgen-add-4-overworld-pocket-dimensions`
- `feat/worldgen-add-4-nether-pocket-dimensions`
- `feat/worldgen-add-3-end-pocket-dimensions`
- `feat/pockets-add-pocket-registry-and-affinity-aware-portal-routing`
- `feat/pockets-add-negative-dimensional-effects-per-affinity`
- `feat/pockets-add-affinity-buff-when-in-matching-dimension`
- `feat/pockets-remove-portal-on-boss-death`
- `feat/spawns-add-affinity-specific-modded-mobs-in-pocket-dimensions`

### Phase 5 (Bosses)
- `feat/boss-add-boss-entity-base-class-with-vanilla-boss-bar-phases-aoe`
- `feat/boss-add-boss-arena-structure-jigsaw-system`
- 11× `feat/boss-<affinity>-implement-<boss-name>` (names from `NamingRegistry`)
- `feat/loot-add-boss-drops-loot-table-with-stone-as-rare`
- `feat/boss-add-lodestone-phase-transition-vfx-and-screen-shake`

### Phase 6 (Mobs)
- `feat/mobs-add-affinity-mobs-tier-gated-across-vanilla-dimensions`
- `feat/mobs-add-affinity-mob-particle-aura`
- `feat/mobs-affinity-mobs-drop-shards-only`
- `feat/loot-add-affinity-shard-drops-from-affinity-mobs`

### Phase 7 (GUI + polish)
- `feat/gui-integrate-affinity-book-screen-and-hud-overlay`
- `feat/portal-add-idle-and-teleport-sounds-and-rift-shader`
- `feat/advancements-add-progression-advancement-tree-covering-all-phases`
- `fix/chunkload-centralize-chunk-force-api-for-boss-arenas`
- `feat/client-full-lodestone-vfx-pass`

### Phase 8 (endgame)
- `feat/endgame-add-corrupted-world-timer-difficulty`
- `feat/endgame-add-generic-themed-endgame-boss`
- `feat/multiplayer-add-pocket-share-and-pocket-list-commands`
- `docs/wiki-draft-player-guide-config-reference-modpack-guide`

### Phase 9+ (expansion)
- `feat/spells-add-devious-mage-spells`
- `feat/spells-add-eternal-mage-spells`
- `feat/spells-add-combo-spells`
- `feat/spells-add-warrior-archetype`
- `feat/spells-add-bow-archetype`
- `feat/pre-dragon-add-lightweight-affinity-mobs`
- `feat/pre-dragon-add-shard-glow-hint-on-player-hand`
- `feat/pre-dragon-add-monster-manual-hint-item`
- `feat/idea-park-overworld-raids-from-dimensions`
- `feat/idea-park-corrupted-world-scaling`

---

# PART C — LIVE WORKING AREA + IDEAS

---

## 24. Open Design Questions

Status: `[ ]` unanswered · `[x]` answered · `[?]` superseded.

### Phase 0 critical
- [ ] Config format (TOML / JSON / hybrid)
- [ ] JSON5 or plain JSON
- [ ] Hot-reload scope
- [ ] Reveal mechanic timing (when can spells be cast relative to reveal)
- [ ] NamingRegistry scope
- [ ] Boss names final
- [ ] Saved_code triage (per file: promote / archive / discard)
- [ ] Enchantment nerf defaults (multipliers per enchantment)
- [ ] Roll-logic specifics (exact partial-percentage distribution)

### Phase 1 (Dragon)
- [ ] HP multiplier exact default
- [ ] Number of phases (3? 4?)
- [ ] Phase-transition thresholds
- [ ] Add-spawn rate per phase
- [ ] Crystal aggression level

### Phase 2 (School)
- [ ] Crystal Orb visual design
- [ ] Affinity Book layout
- [ ] Dimension Staff use count

### Phase 3 (Spells)
- [ ] Mana system implementation (menu / inventory slot / skills-tree UI)
- [ ] Combo spell list exact
- [ ] Spell-Book GUI vs hotbar

### Phase 4 (Pockets)
- [ ] Pocket size exact per affinity
- [ ] Dimensional effects list exact
- [ ] Affinity-buff specifics when inside matching dim

### Phase 5 (Bosses)
- [ ] Boss HP scaling by player count
- [ ] Boss walking / behavior
- [ ] Boss theme / names final

### Phase 6 (Mobs)
- [ ] Affinity mob spawn rate per tier
- [ ] Modded mob count

### Phase 7 (Polish)
- [ ] Advancement tree depth
- [ ] Force-chunkload timeout

### Phase 8 (Endgame)
- [ ] Endgame boss required
- [ ] Corrupted-world mechanic scope

### Cross-phase
- [ ] Lodestone coverage
- [ ] Boss-HP-bar visibility
- [ ] Modpack vs datapack priority
- [ ] Client config sync (per-player)
- [ ] Schema versioning strictness

---

## 25. IDEAS.md (scratchpad)

> New ideas land here. They graduate to Part A/B when they mature.

### Dragon Rework
- HP multiplier TBD, start with 2× vanilla and tune
- Phases escalating aggression, new attacks each phase
- Dragon breath / meteor shower / exploding fireballs
- Aggressive AI sweep (not just orbit)
- Perch knockback + damage waves in all directions
- Stand-still punishment attack
- Summon adds in later phases
- Crystals as enemies: attack players, self-regenerate, destroying spawns mobs
- Climatic finish with strong visual moment
- Lore: Dragon = warden of dimension barrier

### Affinity ideas
- Crystal Orb of Awakening (not a scroll) — sits somewhere in School
- Affinity-Items show their affinity in name (Shard of Fire etc.)
- Tier-scaling: 5% gives small benefits, 100% gives strong effects
- Vanilla-MC effects at high affinity completion (full list TBD)
- Login roll: one 100% + partial % for rest, rarity-skewed
- Eternal affinities: no shards, only stones, all-or-nothing, mutually exclusive
- Lore: "the three eternals together are the foundation of existence"

### Pocket dimension ideas
- Variable size per affinity (final size decided during Phase 4)
- Per-affinity themed environments (see §6.2)
- Negative dimensional effects; flip to buffs at 100% matching affinity
- Affinity buff when inside matching dim
- Boss death removes matching vanilla portal

### Mob ideas
- Affinity mobs = vanilla mobs with affinity tag + particles, shards-only drops
- Modded mobs = fully custom, in pockets only
- Tier-gated spawn locations

### Spell ideas
- Three archetypes: Mage (Phase 3), Warrior (later), Bow (later)
- Combo spells (Wind+Lightning, Water+Ice, etc.)
- 12 sample mage spells first (3 per Elemental affinity)
- Mana as separate bar; mana system TBD
- Per-archetype spell counts per affinity: not fixed

### Lore ideas
- Dragon = warden of dimension barrier, kills cracks it
- Pocket dimensions always existed; barrier made them inaccessible
- Affinities are the building blocks of magic
- Three eternal affinities are the foundation of existence
- School built by an ancient magical society
- Bosses are rulers of their respective pocket worlds
- Modded mobs are elemental creatures born of affinity

### Feature-park ideas
- Overworld raids from pocket dimensions (mobs + mini-bosses spill into vanilla worlds)
- Corrupted-world scaling (depends on Warrior/Bow existing)
- More combat archetypes beyond Mage/Warrior/Bow
- Pocket leaderboards / time-trial scoring
- Affinity-themed music per pocket

---

## 26. Next Steps

1. User reads this plan, comments / corrects.
2. Answer §24 Phase-0 questions.
3. Branch protection on `main` (you do, GitHub UI).
4. Create labels from §21.2 (you do, GitHub UI).
5. Phase 0 work begins.
6. PRs as Draft PRs until sign-off.

---

*Created 2026-06-25. Live document — we iterate as we go.*
