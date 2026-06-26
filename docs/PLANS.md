# Elemental Realms — Master Plan

> **Plan version:** v15
>
> **For Piggidragon:** Master plan with the big picture. Detailed ideas and brainstorming live in `IDEAS.md` — that file is the workspace. This file is the high-level overview that feedback-givers read first.

**Credits:** Inspired by "The Beginning After the End" by TurtleMe (general fantasy-academy setting, elemental magic, staged progression). No characters, names, or plot points from the source material are used.

**Status:** in development. English only. No timeline pressure. Performance: lean and sensible.

**Goal:** Generic fantasy-academy progression mod. Player kills a hard Ender Dragon → unlocks a School dimension + 11 elemental pocket dimensions with bosses, spells, custom mobs.

**Architecture:** NeoForge 1.21.1, Infiniverse for dynamic dimensions, Lodestone for VFX only, all tunables as config data, all display names through one registry.

**Versioning:** SemVer-ish. `0.X.0` = major (breaking for players or modpack authors). `0.0.X` = minor (additive).

---

## 0. Glossary

- **Affinity** — elemental alignment a player holds. 12 + Void.
- **Tier** — affinity category: NONE / ELEMENTAL / DEVIANT / ETERNAL.
- **Deviant** — advanced affinity; requires its base at 100%.
- **Eternal** — ultimate; mutually exclusive, one per player.
- **Void** — "no affinity" state. Clears all.
- **Pocket** — small isolated world per affinity, via portal.
- **Ring** — concentric zone inside a pocket (outer = spawn, center = boss).
- **Boss** — per-affinity pocket boss.
- **Spell** — castable magical action. Costs mana + cooldown.
- **Stone / Shard** — items granting affinity completion. Small/Medium/Big shards by %. Stone = 100%.
- **Crystal Orb of Awakening** — School item revealing hidden affinities.
- **Reveal** — transition from "unknown affinities" to "known affinities".
- **Roll** — first-login random assignment of affinities.
- **Mage / Warrior / Bow** — spell archetypes (Mage first, others later).
- **NamingRegistry** — single source for boss / spell / dimension display names.
- **Affinity mob** — vanilla mob with an affinity tag + particles. Drops shards only.
- **Modded mob** — fully custom entity. Drops affinity-specific loot.
- **Barrier stage** — progression marker (Dragon killed / Elemental boss killed / Deviant boss killed) that unlocks new content tiers.

---

# PART A — FEATURES & GAMEPLAY

---

## 1. Vision & Setting

Generic fantasy-academy world. Player enters a School dimension after the Dragon fight, learns about affinities, explores 11 elemental pocket dimensions, and grows stronger through spellcasting, boss kills, and progressive mastery. Everything user-facing flows through `NamingRegistry` for re-themability.

---

## 2. Player Lifecycle

| Step | Where | What happens |
|---|---|---|
| **0. Login** | Overworld | One ELEMENTAL affinity auto-assigned at 100%. Others (incl. deviants) get small partial rolls. Affinities **client-invisible**. |
| **1. Early progression** | Overworld | Player finds **rare** affinity shards (Small/Medium/Big) from vanilla + custom structures across vanilla dimensions and very rarely from affinity mobs. Item names identify affinity + size. |
| **2. Dragon fight** | End | Hard dragon, maxed Netherite + skill required. |
| **3. Dragon kill** | End | A custom structure spawns at Overworld spawn with a permanent School portal. Pocket dimensions technically accessible but barriere-gated. |
| **4. School entry** | Staff or portal | Staff lets player reach the School from anywhere in vanilla dims. Fantasy-academy hub with the Crystal Orb of Awakening. |
| **5. First pocket** | Anywhere in vanilla | No designated "first pocket" — player chooses. No guarantee which pocket is which tier. Survival is the cost of entry. Boss stone drop is not guaranteed. |
| **6. Deviant stage** | After Elemental boss kill | Deviant mobs spawn in vanilla dims. Deviant pocket dims accessible. |
| **7. Eternal stage** | After Deviant boss kill | Eternal mobs spawn in End. Eternal pocket dims accessible (very hard). |
| **8. Endgame** | optional | Corrupted-world timer difficulty, generic endgame boss. |

---

## 3. Dragon Rework

The Ender Dragon is the **gate to the School dimension** — warden of the dimension barrier. Killing it cracks the barrier. Hard enough that maxed Netherite + skill is the baseline.

Ideas (full detail in IDEAS.md): HP multiplier, multiple escalating phases, dragon breath / meteor shower / exploding fireballs, aggressive AI sweep, perch knockback + damage waves, stand-still punishment attack, summon adds, End Crystals as enemies that self-regenerate and spawn mobs on destruction, climatic finish.

Reference: TrueEnd datapack (similar overhaul).

---

## 4. School Dimension

Fantasy-academy hub entered after the Dragon fight.

**Contents:** Crystal Orb of Awakening, lore library (written-book items), lecture halls (one per Elemental + Deviant halls), common room with cross-affinity bookshelves.

**Access:** permanent portal in a custom structure at Overworld spawn (spawns together after Dragon kill); Dimension Staff reaches the School from anywhere in vanilla dims (use count TBD).

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

First-login random assignment of affinities. Driven by `affinities.json.roll`
(see §19). Three stages (issue #23):

1. **Stage 1 — guaranteed Elemental at 100% (hardcoded).** One random ELEMENTAL
   affinity at **100%** — this is hardcoded by spec, not read from config.
   This is the player's anchor element, always full strength. The configurable
   `maxCompletionPercent` does NOT bound Stage 1.
2. **Stage 2 — rare Deviant partial.** Roll `deviantPartialChancePercent`
   (default 5%) for the matching DEVIANT of the guaranteed Elemental at a
   partial completion. The completion is rolled from a continuous left-skew
   distribution capped at `deviantMaxCompletionPercent` (default 80%).
3. **Stage 3 — decaying Elemental/Deviant loop.** Up to
   `elementalMaxIterations` (default 5) additional iterations. Each: roll
   "continue?" starting at `elementalContinueChanceStartPercent` (default
   50%), halving per iter via `elementalContinueChanceDecayPercent`
   (default 50%). On success: pick a candidate and roll a partial completion
   capped at `elementalMaxCompletionPercent` (default 80%).
   - Candidate rules:
     - ELEMENTAL candidates: any elemental the player does not yet hold.
     - DEVIANT candidates: any Deviant whose matching Elemental is held at
       100% (i.e. the guaranteed one, and only that one since Stage 1 is the
       only @100% source at login). Stage 2 may have already claimed it.
     - Deviant vs Elemental weight is `partialDeviantWeightPercent`
       (default 15). If the chosen pool is empty, fall back to the other
       pool before returning VOID.

**Partial-completion distribution (stages 2 + 3):**

`completion = (int)(maxCompletion * U^slope)`, where `U ~ Uniform[0,1)` and
`slope >= 1` controls the skew. This replaces the earlier bucket-skew
approach (which had a dominant top bucket that almost always landed at the
max).

- `slope = 1` → uniform across [0, max].
- `slope = 3` (default) → heavily left-skewed: ~79% of partials land ≤ 40%
  when `max = 80`; the @ max bucket itself is < 1%.
- `slope > 3` → more extreme (rare high rolls).

Result is `(int)`-truncated, so partials are integer percentages (matching
shard-item increments of +1/+3/+5%). Zero is mapped to 1% so the caller
never has to skip on 0%.

**Distributional intent (defaults, 100 fresh players):**
- ~95% get just 1× guaranteed Elemental @ 100%.
- ~5% get guaranteed + matching Deviant partial.
- Of those that pass the first stage-3 continue (~50%), ~25% pass the next,
  ~12% the next, etc. — additional partials become exponentially rare.
- Stage-3 partials land at low values (typically 5-25%) more often than at
  the max (rare exception, by design).

**Invariants:**
- ETERNAL affinities are never assigned at login (filtered by tier in
  candidate pools; stage 2 only emits the matching DEVIANT of the chosen
  ELEMENTAL which is by definition DEVIANT-typed).
- Player can never end up with unbounded affinities (loop capped at
  `elementalMaxIterations`).
- Login-side write goes through `ModAffinities.addAffinity` for the 100%
  Stage-1 element (hardcoded; `addAffinity` always sets 100% by spec) and
  `addIncrementAffinity` for partials (capped at 100% — shard items carry
  the player from partial to full).

**Configurable vs hardcoded at a glance:**
| Knob | Bound | Where |
|---|---|---|
| Stage 1 Elemental completion | 100% (hardcoded) | `AffinitiesRoll.rollAffinities` Stage 1 |
| Stage 2 Deviant completion | `deviantMaxCompletionPercent` (default 80%) | Config |
| Stage 3 partial completion | `elementalMaxCompletionPercent` (default 80%) | Config |
| Stage 2 + 3 distribution skew | `partialCompletionSlope` (default 3.0) | Config (continuous U^slope) |
| Shard-item completion | 100% (hardcoded) | `ModAffinities.addIncrementAffinity` |
| Stone-item / login Stage 1 | 100% (hardcoded) | `ModAffinities.addAffinity` |

**Tuning at runtime:** OP-only commands
`/elementalrealms affinities roll show` and
`/elementalrealms affinities roll set <field> <value>` write the config file
and reload — no server restart needed.

### 5.3 Learning more

- **Small shard** +1%, **Medium shard** +3%, **Big shard** +5%
- Item names identify affinity and size
- Shards are never "revealed" — player always knows what each helps
- Shard size lets player plan final % without grinding Big for the last 1%
- **Affinity stone** = 100%, ultimate reward, only boss drops it
- **Void stone** = clears all

### 5.4 Tier visibility

Player sees only "X unknown powers slumber within you" until Crystal Orb of Awakening reveals all.

### 5.5 Affinity scaling & vanilla effects

Even 5% gives small effects, 100% gives strong effects. Affinities grant vanilla MC effects at high completion (e.g. Fire 100% = permanent fire immunity). Useful even without spells. Full list in IDEAS.md.

### 5.6 Eternal affinities

No eternal shards, only boss stones. All-or-nothing. Only one per player. Lore: the three together are the foundation of existence.

---

## 6. Pocket Dimensions

### 6.1 Layout

3 concentric rings, player spawns at outer ring and fights inward.

| Ring | Contents |
|---|---|
| **Outer (spawn)** | Vanilla + affinity mobs, small structures, semi-safe start |
| **Intermediate (largest)** | Modded mobs only, larger structures, mini-bosses, traps |
| **Center** | Boss arena, themed by affinity |

Outside the outer ring = air void. Pocket sizes vary per affinity — balanced during Phase 4 build.

### 6.2 Per-affinity theme

Each pocket has a unique environment matching its affinity (Fire = volcano, Water = ocean trench, Wind = floating islands, Earth = underground cavern, Lightning = charged storm, Ice = frozen peak, Sound = echo cavern, Gravity = inverted/floating shards, Life = overgrown ruin, Space = void islands, Time = clockwork ruins). Theme seed-dependent — same Fire pocket looks different per portal-id, but always volcano-centered.

### 6.3 Dimensional effects

Each pocket has **negative passive effects**. Higher matching affinity = weaker effect. At 100% matching affinity, effects flip to buffs. Examples in IDEAS.md.

### 6.4 The 11 pockets

| Tier | Pockets | Where portals appear |
|---|---|---|
| Overworld | Wind, Fire, Water, Earth | Anywhere in vanilla Overworld |
| Nether | Sound, Gravity, Lightning, Ice | Anywhere in vanilla Nether |
| End | Life, Space, Time | Anywhere in vanilla End |

### 6.5 Boss drops

Boss drops: big shards of matching affinity (common), equipment, spells, lore items (always). **Affinity stone is rare** — not guaranteed even from boss. When boss dies, matching vanilla portal disappears (encourages variety).

---

## 7. Mobs

### 7.1 Affinity mobs

Vanilla mobs with an affinity tag + Lodestone particles. Spawn rarely across vanilla dimensions. Tier-gated (Elemental everywhere; Deviant unlocked after first Elemental boss; Eternal unlocked after first Deviant boss). **Drops shards only — never stones.**

### 7.2 Modded mobs

Fully custom entities. Spawn in pocket dimensions only. High-tier drops including equipment, scrolls, lore.

---

## 8. Spells

### 8.1 Three archetypes

| Archetype | Trigger | Scope |
|---|---|---|
| **Mage** | Hotbar / spell-book, projectiles & AoEs | Phase 3 first |
| **Warrior** | Off-hand held, melee/buffs | Later |
| **Bow** | Bow enchantment-mods | Later |

### 8.2 Mage samples (Phase 3)

12 sample spells, 3 per ELEMENTAL affinity. Per-archetype spell counts per affinity are not fixed.

### 8.3 Combo spells

Spells can combine affinities (Wind+Lightning=Storm, Water+Ice=Blizzard, etc.). Exact combos in IDEAS.md.

### 8.4 Mana

Mana is a separate resource (like hunger). Held in a central mana system — implementation TBD (menu, inventory slot, skills-tree UI). Upgradeable over time.

---

## 9. Bosses

11 bosses (one per affinity, Void has none). Each in a pocket's center arena, themed to affinity.

**Mechanics:** vanilla HP bar, phases with Lodestone VFX cues, resists own affinity, weak to deviant/base counter, AoE attacks per phase, Lodestone transition FX.

**Drops:** big shards (common), equipment, spells, lore items (always). Affinity stone is rare — not guaranteed.

Boss walking / behavior not yet decided.

---

## 10. Balance Changes

### 10.1 Enchantment nerf

Protection, Sharpness, Sweeping, Smite, Bane of Arthropods all scaled back. Reason: vanilla top-tier enchantments are too game-changing — general MC balance issue, not just for spell users. Multipliers per enchantment in `enchantments.json`.

### 10.2 Ominous-Potion-Scaling

Potion effects scale with difficulty / location, Trial-Chamber-style.

---

## 11. Progression & Advancements

One advancement tree per phase. Milestones include first-login, first shard, Dragon kill, Awakening, first pocket entry, first boss kill, tier completions, no-laser/no-death Dragon clears, combo spells learned, etc.

---

## 12. UI / GUI

Affinity Book (Lodestone ScreenAPI), Mana UI (skills-tree UI is one option), Portal rift shader (Lodestone).

---

## 13. Performance

Lean perf: as many particles as needed, as few as sensible. VFX LOD-aware. Boss transitions temporary. Pocket gen caps to bounded ring. No potato-PC target — users opt into Sodium/Embeddium.

---

## 14. Multiplayer

Multiplayer-safe for single-pocket scenarios. Edge cases handled per-phase. Single-player testing is the main gate.

---

## 15. Controversial Design Decisions

| # | Decision | Why |
|---|---|---|
| 15.1 | Hard Dragon gate | Dedication + world-building + natural power-tier transition |
| 15.2 | Enchantments nerfed globally | Vanilla enchantments too game-changing — general MC balance issue |
| 15.3 | Pockets gated by barrier-progression, not by affinity | All accessible after Dragon kill; new tiers unlock via boss kills |
| 15.4 | Pockets don't require affinity to enter | Soft gate via dimensional effects + gear |
| 15.5 | Affinity state hidden until Reveal | Mystery, drives shard collection, makes Reveal a moment |
| 15.6 | Boss stone drop not guaranteed | Affinity stone is the ultimate reward |
| 15.7 | Boss death removes matching vanilla portal | Encourages variety over farming |
| 15.8 | Pockets persistent | Boss killed = stays killed. Configurable to regenerate. |
| 15.9 | Eternal all-or-nothing | Lore: three eternals together are the foundation of existence |
| 15.10 | PvP not a design goal | Spells designed for PvE only |

---

# PART B — TECHNICAL BUILD PLAN

---

## 16. Phased Rollout

Each phase: code PR (Draft) → user game-test → feedback → fix → sign-off → merge to `dev`.

### Phase 0 — Full Consolidation Pass

See §17. Runs before any new feature.

### Phase 1 — Dragon Rework

TrueEnd-inspired overhaul. HP, phases, breath/meteor/fireballs, perch knockback, stand-still punishment, summon adds, crystal aggression + regen, climatic finish, aggressive AI.

### Phase 2 — School + Crystal Orb + Spawn Structure

- Crystal Orb of Awakening item
- School dimension content (lecture halls, library, common room)
- Custom structure at Overworld spawn with School portal (spawns together)
- Affinity reveal mechanic — `revealed: boolean` attachment field
- Dimension Staff (from-anywhere School teleport)
- Affinity Book GUI foundation

### Phase 3 — Spell API + 12 mage samples

- `Spell` interface, `SpellRegistry`
- Mana bar (vanilla GuiGraphics)
- Mana system TBD (menu / inventory slot / skills-tree UI)
- SpellBookScreen on Lodestone
- Spell hotkey + cast burst via Lodestone
- 12 sample spells end-to-end

### Phase 4 — Pocket Dimensions

- Pocket ring layout (reusable Jigsaw templates)
- 11 pocket-dimension JSONs with per-affinity themes
- Variable pocket size (balanced during build)
- `BoundedChunkGenerator` extension
- `PocketRegistry`
- Portal logic with `affinity_target` tag
- Negative dimensional effects per pocket; buffs at 100% matching affinity
- Boss death removes matching vanilla portal

### Phase 5 — 11 Bosses

- Boss entity base (vanilla boss bar, phases, AoE, resistances)
- BossArenaStructure Jigsaw system
- 11 bosses (names from `NamingRegistry`)
- Boss drops: big shards (common), equipment, spells, lore items (always); affinity stone (rare)
- Lodestone phase-transition VFX + screen shake

### Phase 6 — Custom Mobs

- Affinity mobs (vanilla mobs + tag + particles) spawning rarely in vanilla dims
- Tier-gating: Elemental everywhere; Deviant after first Elemental boss; Eternal after first Deviant boss
- Affinity mobs drop shards only
- Modded mobs in pockets only — fully custom, density tuned, high-tier drops

### Phase 7 — GUI + Polish

- Affinity Book full implementation
- Mana UI finalized
- Portal sound + rift shader
- Advancement tree per phase
- Force-chunkload fix
- Lodestone VFX pass

### Phase 8 — Endgame

- Corrupted-world timer difficulty
- Generic-themed endgame boss
- Multiplayer tooling
- Wiki content at first public release

### Phase 9+ — Expansion

1. Deviant mage spells
2. Eternal mage spells
3. Combo spells (Wind+Lightning, Water+Ice, etc.)
4. Warrior archetype
5. Bow archetype
6. Pre-Dragon content (lightweight affinity mobs, shard-glow hint, monster manual)
7. Idea-park: Overworld raids from pockets, corrupted-world scaling

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

### Phase 0.5 — Enchantment nerf
- Mixin into `EnchantmentProtection` and `EnchantmentSharpness`.
- Per-level multiplier from `enchantments.json`.
- Extend to Sweeping, Smite, Bane.
- Advancement "Old Enchantments Weakened".

### Phase 0.6 — Affinity bugfix pass
- Assign LEGENDARY/MYTHIC per `affinities.json.rarities`.
- Improve tier-validation error messages.
- Make `PlayerLoginHandler` defensive on tier-validation errors.
- Rewrite roll logic: one 100% + partial percentages for rest, rarity-skewed.

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
1. NeoForge `ModConfigSpec` (TOML) — `common.toml`, `server.toml`, `client.toml`
2. Custom JSON5 in `config/elementalrealms/` — complex tables
3. Datapacks in `data/elementalrealms/` — recipes, loot, structures, mob spawns

Hot-reload via `/reload` or `ModConfigEvent.Reloading`. Lazy-apply: new entities read new values, existing keep snapshot. New `/elementalrealms reload` command. `"schemaVersion": 1` per JSON. Strict by default with `common.toml.allowSchemaMismatch` toggle.

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
│   └── elementals/         [NEW]
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
└── mana/                    [NEW — implementation TBD]
client/
├── lodestone/              [NEW]
└── events/
    ├── DragonDeathHandler.java [✓ — overhaul in Phase 1]
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
| `affinities.json` | Roll mechanics (`roll` section, see §5.2 for field semantics), completion cap (`completion.maxCompletionPercent`), tier rules (`tiers`), drop rates (`drops`) |
| `dimensions.json` | Pocket size, ring layout, affinity → pocket map, dimensional effects, affinity-buff thresholds |
| `spells.json` | Spell definitions (damage, cooldown, mana, VFX hooks, combo rules) |
| `bosses.json` | Boss stats per affinity |
| `mobs.json` | Affinity mob spawn rules + tier-gating; modded mob spawn rules |
| `portal.json` | Portal config |
| `dragon.json` | HP multiplier, phase transitions, attack configs |
| `school.json` | School + Crystal Orb + Dimension Staff config |
| `enchantments.json` | Protection/sharpness nerf multipliers + potion scaling |
| `timer.json` | Corrupted-world timer difficulty |

---

## 20. Asset Pipeline

Three categories, three workflows. No asset work before Phase 0.7.

### 20.1 Models (you build)
Tracked in `docs/ASSET-MODELS.md`: Affinity Stones (12+1), Shards (12 affinities × 3 sizes = 36 variants), Void Stone, Crystal Orb of Awakening, Spell Scrolls, Mana element TBD, School Staff, Affinity Book, Portal frame, 11 boss models, modded mob models.

### 20.2 Textures (external artist)
Per-phase briefs, not upfront.

### 20.3 Sounds (you source: online + DIY)
Mix of freesound.org + DIY.

### 20.4 Asset workflow
- No asset work before Phase 0.7
- Each PR lists "asset deps"
- Asset missing → placeholder, ship anyway

---

## 21. GitHub Workflow

### 21.1 Branch protection
- `main`: protected, no direct push, requires PR + review
- `dev`: open for direct push (mini fixes)

### 21.2 Labels

**area/\*** (color `#c5def5`):
`area/config`, `area/worldgen`, `area/magic`, `area/spells`, `area/bosses`, `area/mobs`, `area/gui`, `area/portal`, `area/lodestone`, `area/enchantment`, `area/assets`, `area/dragon`, `area/school`, `area/affinity`, `area/multiplayer`

### 21.3 Draft PRs

Every PR created as `draft: true` via MCP. When you say "fine" → I mark Ready → you merge.

### 21.4 Templates

Skill `elementalrealms-workflow` is canonical. Optional: `.github/PULL_REQUEST_TEMPLATE.md` + `.github/ISSUE_TEMPLATE/{bug,feat}.yml`.

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

Branch names derived from issue titles. Full list lives in this section; not duplicated here. Run the GitHub MCP create-issues batch per phase.

---

# PART C — LIVE WORKING AREA

---

## 24. Open Design Questions

Status: `[ ]` unanswered · `[x]` answered · `[?]` superseded.

### Phase 0 critical
- [ ] Config format (TOML / JSON / hybrid)
- [ ] JSON5 or plain JSON
- [ ] Hot-reload scope
- [ ] Reveal mechanic timing
- [ ] NamingRegistry scope
- [ ] Boss names final
- [ ] Saved_code triage
- [ ] Enchantment nerf defaults
- [ ] Roll-logic specifics

### Phase 1 (Dragon)
- [ ] HP multiplier default
- [ ] Number of phases
- [ ] Phase-transition thresholds
- [ ] Add-spawn rate per phase
- [ ] Crystal aggression level

### Phase 2 (School)
- [ ] Crystal Orb visual design
- [ ] Affinity Book layout
- [ ] Dimension Staff use count

### Phase 3 (Spells)
- [ ] Mana system implementation
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
- [ ] Client config sync
- [ ] Schema versioning strictness

---

## 25. Next Steps

1. User reads this plan, comments / corrects.
2. Answer §24 Phase-0 questions.
3. Branch protection on `main` (you do, GitHub UI).
4. Create labels from §21.2 (you do, GitHub UI).
5. Phase 0 work begins.
6. PRs as Draft PRs until sign-off.

---

*For ideas, brainstorming, and the live scratchpad, see `IDEAS.md`.*
