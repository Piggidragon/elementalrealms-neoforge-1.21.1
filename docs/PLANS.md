# Elemental Realms — Master Plan

> **Plan version:** v18
>
> **For Piggidragon:** Master plan with the big picture. Detailed ideas and brainstorming live in `IDEAS.md` — that file is the workspace. This file is the high-level overview that feedback-givers read first.

**Credits:** Inspired by "The Beginning After the End" by TurtleMe (general fantasy-academy setting, elemental magic, staged progression). No characters, names, or plot points from the source material are used.

**Status:** in development. English only. No timeline pressure. Performance: lean and sensible.

**Goal:** Generic fantasy-academy progression mod. Player kills the Dragon → mod starts. Unlocks a School dimension + 11 elemental pocket dimensions with bosses, spells, custom mobs.

**Architecture:** NeoForge 1.21.1, Infiniverse for dynamic dimensions, Lodestone for VFX only, all tunables as config data, all display names through one registry.

**Scope split:**
- **ER (this repo)** = vanilla-balanced addons only. Vanilla mobs, vanilla structures, vanilla recipes stay untouched. Anything that modifies vanilla behavior lives in the sibling repo.
- **`Piggidragon/dragonsrequiem-neoforge-1.21.1`** (sibling repo) = vanilla reworks.
- **Modpack** combining both gets a separate balance pass.

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
- **Affinity mob** — vanilla mob with an affinity tag + particles. Drops shards only.
- **Modded mob** — fully custom entity. Drops affinity-specific loot.
- **Barrier stage** — progression marker (Dragon killed / Elemental boss killed / Deviant boss killed) that unlocks new content tiers.

---

# PART A — FEATURES & GAMEPLAY

---

## 1. Vision & Setting

Generic fantasy-academy world. Player enters a School dimension after killing the Dragon, learns about affinities, explores 11 elemental pocket dimensions, and grows stronger through spellcasting, boss kills, and progressive mastery. All user-facing display names live in `lang/en_us.json` and flow through `Component.translatable()`; re-themability uses the standard Minecraft Resource Pack workflow.

---

## 2. Player Lifecycle

| Step | Where | What happens |
|---|---|---|
| **0. Login** | Overworld | One ELEMENTAL affinity auto-assigned at 100%. Others (incl. deviants) get small partial rolls. Affinities **client-invisible**. |
| **1. Early progression** | Overworld | Player finds **rare** affinity shards (Small/Medium/Big) from vanilla + custom structures across vanilla dimensions and very rarely from affinity mobs. Item names identify affinity + size. |
| **2. Dragon fight** | End | Standard End dimension. |
| **3. Dragon kill** | End | Mod starts. A custom structure spawns at Overworld spawn with a permanent School portal. Pocket dimensions technically accessible but barrier-gated. |
| **4. School entry** | Staff or portal | Staff lets player reach the School from anywhere in vanilla dims. Fantasy-academy hub with the Crystal Orb of Awakening. |
| **5. First pocket** | Anywhere in vanilla | No designated "first pocket" — player chooses. No guarantee which pocket is which tier. Survival is the cost of entry. Boss stone drop is not guaranteed. |
| **6. Deviant stage** | After Elemental boss kill | Deviant mobs spawn in vanilla dims. Deviant pocket dims accessible. |
| **7. Eternal stage** | After Deviant boss kill | Eternal mobs spawn in End. Eternal pocket dims accessible (very hard). |
| **8. Endgame** | optional | Corrupted-world timer difficulty, generic endgame boss. |

---

## 3. Dragon Gate

The Dragon kill is the gate that starts the mod. Killing the Dragon spawns the School-portal structure at Overworld spawn. After that, School, pockets, bosses, affinities, spells — everything is unlocked.

ER does not modify anything about the Dragon, the End dimension, the crystals, or any dragon-related vanilla behavior. The Dragon is the standard Minecraft experience; ER only listens for the kill advancement to trigger School-portal spawn.

---

## 4. School Dimension

Fantasy-academy hub entered after the Dragon kill.

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
   50%), halving per iter via `elementalContinueChanceDecayPercent` (default
   50%). On success: pick a candidate and roll a partial completion
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
| **Bow** | Bow-mod interactions | Later |

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

### 10.1 Ominous-Potion-Scaling

Potion effects scale with difficulty / location, Trial-Chamber-style. Applied to vanilla potions in harder contexts (pockets, boss arenas). Tunables live in ER config.

---

## 11. Progression & Advancements

One advancement tree per phase. Milestones include first-login, first shard, Dragon kill, Awakening, first pocket entry, first boss kill, tier completions, combo spells learned, etc.

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
| 15.1 | Hard Dragon gate | Dedication + world-building + power-tier transition |
| 15.2 | Pockets gated by barrier-progression, not by affinity | All accessible after Dragon kill; new tiers unlock via boss kills |
| 15.3 | Pockets don't require affinity to enter | Soft gate via dimensional effects + gear |
| 15.4 | Affinity state hidden until Reveal | Mystery, drives shard collection, makes Reveal a moment |
| 15.5 | Boss stone drop not guaranteed | Affinity stone is the ultimate reward |
| 15.6 | Boss death removes matching vanilla portal | Encourages variety over farming |
| 15.7 | Pockets persistent | Boss killed = stays killed. Configurable to regenerate. |
| 15.8 | Eternal all-or-nothing | Lore: three eternals together are the foundation of existence |
| 15.9 | PvP not a design goal | Spells designed for PvE only |

---

# PART B — TECHNICAL BUILD PLAN

---

## 16. Phased Rollout

Each phase: code PR (Draft) → user game-test → feedback → fix → sign-off → merge to `dev`.

### Phase 1 — School + Crystal Orb + Spawn Structure

The Dragon is the gate that starts the mod. After the kill, the player reaches the School content.

- Crystal Orb of Awakening item
- School dimension content (lecture halls, library, common room)
- Custom structure at Overworld spawn with School portal (spawns together)
- Affinity reveal mechanic — `revealed: boolean` attachment field
- Dimension Staff (from-anywhere School teleport)
- Affinity Book GUI foundation
- Dragon-kill advancement listener → trigger School-portal spawn

### Phase 2 — Spell API + 12 mage samples

- `Spell` interface, `SpellRegistry`
- Mana bar (vanilla GuiGraphics)
- Mana system TBD (menu / inventory slot / skills-tree UI)
- SpellBookScreen on Lodestone
- Spell hotkey + cast burst via Lodestone
- 12 sample spells end-to-end

### Phase 3 — Pocket Dimensions

- Pocket ring layout (reusable Jigsaw templates)
- 11 pocket-dimension JSONs with per-affinity themes
- Variable pocket size (balanced during build)
- `BoundedChunkGenerator` extension
- `PocketRegistry`
- Portal logic with `affinity_target` tag
- Negative dimensional effects per pocket; buffs at 100% matching affinity
- Boss death removes matching vanilla portal

### Phase 4 — 11 Bosses

- Boss entity base (vanilla boss bar, phases, AoE, resistances)
- BossArenaStructure Jigsaw system
- 11 bosses (display names via `lang/en_us.json` + `Component.translatable()`)
- Boss drops: big shards (common), equipment, spells, lore items (always); affinity stone (rare)
- Lodestone phase-transition VFX + screen shake

### Phase 5 — Custom Mobs

- Affinity mobs (vanilla mobs + tag + particles) spawning rarely in vanilla dims
- Tier-gating: Elemental everywhere; Deviant after first Elemental boss; Eternal after first Deviant boss
- Affinity mobs drop shards only
- Modded mobs in pockets only — fully custom, density tuned, high-tier drops

### Phase 6 — GUI + Polish

- Affinity Book full implementation
- Mana UI finalized
- Portal sound + rift shader
- Advancement tree per phase
- Force-chunkload fix
- Lodestone VFX pass

### Phase 7 — Endgame

- Corrupted-world timer difficulty
- Generic-themed endgame boss
- Multiplayer tooling
- Wiki content at first public release

### Phase 8+ — Expansion

1. Deviant mage spells
2. Eternal mage spells
3. Combo spells (Wind+Lightning, Water+Ice, etc.)
4. Warrior archetype
5. Bow archetype
6. Pre-Dragon content (lightweight affinity mobs, shard-glow hint, monster manual)
7. Idea-park: Overworld raids from pockets, corrupted-world scaling

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
| Laser (Lightning) | `WorldParticleRenderer` line-of-sight beam |
| Spell Book GUI | `LodestoneScreen` |
| Corrupted World | `LodestoneScreenAPI` vignette + tint |

NOT Lodestone: boss HP bars (vanilla `BossEvent`), general entity rendering, mana bar / simple HUD (vanilla `GuiGraphics`).

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
│   └── affinities/         [✓]
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
│   ├── ModConfigs.java, ConfigReloadListener.java
│   └── {Affinity,Dimensions,Spells,Bosses,Mobs,Portal,School,Timer}Config.java
└── commands/ModCommands.java
magic/
├── affinities/             [✓ — 3-stage roll per §5.2]
├── spells/
│   ├── Spell.java, SpellRegistry.java, SpellCasting.java
│   ├── SpellBookScreen.java (Lodestone)
│   └── impl/{FireSpells, WaterSpells, ...}.java
└── mana/                    [NEW — implementation TBD]
client/
├── lodestone/              [NEW]
└── events/
    ├── DragonDeathHandler.java [✓ — dragon-kill listener, Phase 1]
    ├── PlayerLoginHandler.java [✓ — 3-stage roll per §5.2]
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
| `school.json` | School + Crystal Orb + Dimension Staff config |
| `timer.json` | Corrupted-world timer difficulty |

---

## 20. Asset Pipeline

Three categories, three workflows. No asset work blocks code PRs.

### 20.1 Models (you build)
Tracked in `docs/ASSET-MODELS.md`: Affinity Stones (12+1), Shards (12 affinities × 3 sizes = 36 variants), Void Stone, Crystal Orb of Awakening, Spell Scrolls, Mana element TBD, School Staff, Affinity Book, Portal frame, 11 boss models, modded mob models.

### 20.2 Textures (external artist)
Per-phase briefs, not upfront.

### 20.3 Sounds (you source: online + DIY)
Mix of freesound.org + DIY.

### 20.4 Asset workflow
- Asset deps are tracked per PR but do not block code PRs
- Asset missing → placeholder, ship anyway

---

## 21. GitHub Workflow

### 21.1 Branch protection
- `main`: protected, no direct push, requires PR + review
- `dev`: open for direct push (mini fixes)

### 21.2 Labels

**area/\\*** (color `#c5def5`):
`area/config`, `area/worldgen`, `area/magic`, `area/spells`, `area/bosses`, `area/mobs`, `area/gui`, `area/portal`, `area/lodestone`, `area/assets`, `area/school`, `area/affinity`, `area/multiplayer`

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
| `Test-DragonGate` | Creative + End portal + dragon summoner; Dragon kill triggers School-portal spawn at Overworld spawn |
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

### Resolved (Phase 0)
- [x] Config format (TOML + JSON5 hybrid)
- [x] JSON5 selected
- [x] Hot-reload scope (TOML via ModConfigEvent, JSON5 via /elementalrealms reload)
- [x] Reveal mechanic timing (post-Dragon kill, Crystal Orb of Awakening)
- [x] Saved_code triage (#26 — promoted / archived / discarded)
- [x] Roll-logic specifics (3-stage per §5.2)

### Phase 1 (School)
- [ ] Crystal Orb visual design
- [ ] Affinity Book layout
- [ ] Dimension Staff use count

### Phase 2 (Spells)
- [ ] Mana system implementation
- [ ] Combo spell list exact
- [ ] Spell-Book GUI vs hotbar

### Phase 3 (Pockets)
- [ ] Pocket size exact per affinity
- [ ] Dimensional effects list exact
- [ ] Affinity-buff specifics when inside matching dim

### Phase 4 (Bosses)
- [ ] Boss HP scaling by player count
- [ ] Boss walking / behavior
- [ ] Boss theme / names final

### Phase 5 (Mobs)
- [ ] Affinity mob spawn rate per tier
- [ ] Modded mob count

### Phase 6 (Polish)
- [ ] Advancement tree depth
- [ ] Force-chunkload timeout

### Phase 7 (Endgame)
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
2. Branch protection on `main` (you do, GitHub UI).
3. Create labels from §21.2 (you do, GitHub UI).
4. Phase 1 work begins.
5. PRs as Draft PRs until sign-off.

---

*For ideas, brainstorming, and the live scratchpad, see `IDEAS.md`.*
