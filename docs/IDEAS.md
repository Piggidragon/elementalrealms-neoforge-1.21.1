# Elemental Realms — IDEAS

> **Scratchpad / workspace for brainstorming.** All loose ideas, mechanics sketches, and design experiments land here. When something matures and gets locked in, it graduates to `PLANS.md`.
>
> **Not a stable document** — expect messy, conflicting, half-baked. That's the point. Anything in here can change.

---

## 0. How to use this file

- **PLANS.md** = the high-level overview. What the mod does, why, in what order.
- **IDEAS.md** (this file) = the scratchpad. Brainstorming, alternatives, things we tried then dropped, mechanics sketches with numbers we might tune, etc.

When an idea graduates to PLANS.md, move it (don't copy — actually move it and delete from here, so this file stays small).

Sections below group ideas by topic. They're loosely ordered — newer ideas go on top of their section.

---

## 1. Affinity System

### Shard sizes
- Small +1%, Medium +3%, Big +5%
- Different loot tables feel different (common sources = mostly small, rare = mix, boss = mostly big)
- Player one tick away from completion can use Small shards, isn't forced to grind Big
- 12 affinities × 3 sizes = 36 shard variants

### Affinity item names
- Always include affinity + size: "Small Affinity Shard of Fire", "Big Affinity Shard of Water", "Affinity Stone of Lightning"
- Player always knows which shard helps what (shards are never "revealed")
- Item name lets player plan: "I have 96% Fire, I need Small shards to finish"

### Login roll
- One ELEMENTAL at 100% guaranteed
- Partial % on others (incl. deviants), rarity-skewed
- Distribution shape TBD — rough draft:
  - 50% chance for "≥1 other at >20%"
  - 25% for "≥1 other at >50%"
  - 10% for "any other at 100%"
  - Deviant rarer than Elemental
  - Eternal not given at login
  - More-affinities-held rarer

### Tier scaling & vanilla effects
- Even 5% gives small effects, 100% gives strong effects
- High completion grants vanilla MC effects (full list TBD)
- Examples drafted:
  - Fire 100% = permanent fire immunity
  - Water 100% = water breathing + dolphin grace
  - Wind 100% = slow fall + speed
  - Earth 100% = knockback resistance + mining haste
  - Lightning 100% = ? (chain on hit? speed?)
  - Ice 100% = ? (freeze attackers? cold immunity?)
  - Sound 100% = ? (echo location? reveal hidden?)
  - Gravity 100% = ? (float? slow falling negates damage?)
  - Life 100% = regen / extra health
  - Space 100% = ? (chorus fruit teleport? ender pearl speed?)
  - Time 100% = ? (haste 2? slower status effects?)

### Affinity buffs inside matching dimension
- Player with matching affinity gets a buff while inside their matching pocket
- Buff stacks with the 100% → flip mechanic (negative → positive effects)

### Eternal lore
- No eternal shards, only boss stones
- All-or-nothing — one eternal per player
- The three together = foundation of existence
- Lore: holding more than one would tear you apart

### Reveal mechanic
- After login, player sees only "X unknown powers slumber within you"
- Crystal Orb of Awakening reveals all
- Until revealed, even spell-casting UI feedback is limited TBD

---

## 2. Pocket Dimensions

### Per-affinity themes (11 unique environments)
- FIRE = volcano in center
- WATER = ocean trench / flooded cave
- WIND = floating islands / canyon with updrafts
- EARTH = underground cavern / mesa
- LIGHTNING = charged storm / obsidian spires
- ICE = frozen peak / glacier
- SOUND = echo cavern / acoustic resonance chamber
- GRAVITY = inverted floating shards / anti-gravity ruins
- LIFE = overgrown ruin / jungle temple
- SPACE = void islands / black-hole pockets
- TIME = clockwork ruins / eroded landscape

### Layout (3 rings)
- Outer (spawn) = vanilla + affinity mobs, small structures, semi-safe
- Intermediate (largest) = modded mobs only, larger structures, mini-bosses, traps
- Center = boss arena, themed

### Size
- Variable per affinity, balanced during Phase 3 build
- No concrete numbers yet — start at rough placeholder, tune during testing

### Dimensional effects
- **Negative** by default (no matching affinity)
- **Weaker** as matching affinity goes up
- **Flip to buffs** at 100% matching affinity

Examples (TBD tuning):
- Fire dim = you burn longer / more damage from fire / lava pools spawn frequently
- Water dim = you drown faster / water flow is stronger
- Wind dim = constant updrafts push you around / jump boost lost
- Earth dim = lower jump height / unstable ground (tremors?)
- Lightning dim = random lightning strikes / charged creepers?
- Ice dim = freezing damage / slippery ground
- Sound dim = constant loud noise (slowness? nausea?) or echo-based puzzles
- Gravity dim = jumps weakened / fall damage amplified / float when 100%
- Life dim = hostile vines? / regen when 100%
- Space dim = random teleports / ender pearl effects / void damage
- Time dim = day/night cycle sped up / mob spawns faster / haste when 100%

### Seed behavior
- Same affinity = same layout structure (always a volcano for Fire)
- Different portal-id = different generation (mob positions, resource placement, trap locations)
- No two portals identical, but affinity "feel" consistent

### Boss death removes portal
- When boss of affinity X dies, matching vanilla portal disappears
- Forces player to seek other content instead of farming same portal
- Open question: does the portal reappear later? After a timer? When player respawns? Never?

---

## 3. Mobs

### Affinity mobs
- Vanilla mobs with affinity tag attached
- Spawn rarely across vanilla dimensions (tier-gated)
- Lodestone particles so affinity is visible
- Drops: shards only, never stones

### Modded mobs
- Fully custom entities
- Pocket dimensions only
- Density tuned per pocket ring
- High-tier drops: equipment, scrolls, lore

### Tier gating
- Overworld + Nether + End: Elemental mobs
- Nether + End (after first Elemental boss): + Deviant mobs
- End only (after first Deviant boss): + Eternal mobs

### Custom mob count TBD
- 8–10 per tier?
- Per-affinity or shared variants?

---

## 4. Spells

### Three archetypes
- **Mage** = hotbar / spell-book, projectiles & AoEs (Phase 2 first)
- **Warrior** = off-hand held, melee/buffs (later, ~year+ of work)
- **Bow** = bow-mod interactions (later, ~year+ of work)

### Combo spells (TBD exact list)
- Wind + Lightning = Storm
- Water + Ice = Blizzard
- Earth + Lightning = ?
- Fire + Wind = Wildfire / Firestorm?
- Sound + Lightning = Thunderclap?
- (More to design — design happens during Phase 2)

### Mana
- Separate bar (like hunger)
- Central mana system — implementation TBD (menu, inventory slot, skills-tree UI)
- Upgradeable over time
- Exact upgrade path TBD (crafted / boss drop / level-gated — decide later)

### Per-archetype spell counts
- Not fixed per affinity
- Phase 2: 12 sample mage spells (3 per Elemental) as starting point
- Deviant + Eternal mage spells later
- Warrior + Bow archetypes later

### Spell costs & scaling (all in spells.json)
- Mana cost, cooldown, damage all configurable
- Damage scaling: fixed + affinity-completion bonus (e.g. Fire 100% = +30% Fire spell damage)

### Phase 2 sample list (locked in)
| Affinity | Basic | Utility | Ultimate |
|---|---|---|---|
| FIRE | Fire Bolt | Flame Shield | Meteor |
| WATER | Water Bolt | Tidal Wave | Healing Rain |
| WIND | Wind Slash | Dash | Tornado |
| EARTH | Rock Throw | Earthen Wall | Quake |

---

## 5. Bosses

### Boss walking / behavior
- **Not yet decided** — design during Phase 4
- Per-affinity style (e.g. Fire boss = aggressive, Ice boss = defensive with adds, Sound boss = moves around environment)
- Boss HP / phase / drop mechanics TBD per boss during build

### Boss drops
- Big shards of matching affinity (common)
- Equipment (boss-tier gear themed by affinity)
- Spells (scrolls)
- Lore items (written books with boss backstory)
- **Affinity stone is the ultimate reward, rare, not guaranteed**

---

## 6. Lore

### Established
- Dragon kill is the progression gate; School + pockets unlock after the kill
- Pocket dimensions always existed; barrier kept them inaccessible
- Affinities are the building blocks of magic
- Three eternal affinities together are the foundation of existence
- School built by an ancient magical society
- Bosses are rulers of their pocket worlds
- Modded mobs are elemental creatures born of affinity

### Open questions / ideas
- Who were the ancient magical society? (generic — no specific names)
- Why do affinities roll at random at login? Is it reincarnation-flavored?
- What happens when a player holds an eternal affinity? Why is it tearing?
- What destroyed the ancient magical society?
- Do the bosses have personalities? Speak? Drop lore that reveals their backstories?

---

## 7. School Dimension

### Layout
- Crystal Orb of Awakening (in a specific room — exact location TBD)
- Lecture halls (one per Elemental + Deviant halls)
- Common room with cross-affinity lore bookshelves
- Library with fantasy-academy lore books

### Custom structure at Overworld spawn
- Spawns together with the School portal after Dragon kill
- Acts as a "gateway building" — lore-friendly way for a permanent portal to exist in vanilla Overworld
- Exact structure design TBD

### Dimension Staff
- Use count TBD
- Lets player reach School from anywhere in vanilla dims
- Alternative to walking back to spawn portal

---

## 8. Balance Changes

### Ominous-Potion-Scaling
- Like Trial Chambers — potion effects scale with difficulty
- Applied to vanilla potions in harder contexts (pockets, boss arenas)
- Tunables live in ER config — pocket-difficulty lever, not a vanilla-rework
- File layout TBD (where in JSON tree: `dimensions.json` pocket-effects section vs new top-level file)

### Pocket difficulty
- Even easiest pocket one-shots maxed-vanilla players without matching affinity + correct gear
- Forces mod's progression flow even if players know about portals
- Pockets are end-game content disguised as accessible

---

## 9. UI / GUI

### Affinity Book (Lodestone ScreenAPI)
- Shows all affinities with completion % (after reveal)
- Pick active spells (3 slots, one per archetype?)
- Toggle particles / VFX density
- Linked to Lodestone particle batch + screen-shake for spell preview

### Mana UI
- Skills-tree UI is one option
- Other options: menu, inventory slot
- Exact implementation TBD (Phase 2)

### Portal rift shader
- Lodestone distortion around active portal frames
- Looks like swirling rift

### Other UI ideas
- HUD overlay for current active spell + cooldown
- Death screen / overlay with affinity death cause ("burned by Fire dimension" etc.)
- Boss kill celebration screen

---

## 10. Progression & Advancements

### Milestone ideas
- First Login (affinity rolled)
- First Shard
- The Journey Begins (Dragon killed)
- Awakening (Crystal Orb used)
- First Pocket (entered any pocket)
- First Blood (first boss kill)
- Elemental Master (all 4 elementals at 100%)
- Deviant (first deviant learned)
- Eternal (eternal affinity learned — rare)
- Combo spells learned (per combo)
- All Bosses Defeated
- Pocket Variety (entered all 11 pockets)
- Strongest of Affinity X (boss of X killed)

### Open questions
- One tree per phase or one big tree?
- Hidden / non-recipe advancements?
- Reward structure (XP? items? titles?)

---

## 11. Feature-Park (interesting but not scheduled)

### Overworld raids from pocket dimensions
- Mobs + mini-bosses spill out into vanilla worlds
- Raid triggers TBD (boss kill? specific event? time-based?)
- Difficulty scales with barrier stage
- Reward: defend vanilla world + get bonus loot

### Corrupted-world scaling
- After Phase 7, dimensions get stronger over time (mob buffs, harder bosses, new effects)
- Optional toggle, balanced for hardcore players
- Could depend on Warrior/Bow existing for proper combat challenge

### More combat archetypes (beyond Mage/Warrior/Bow)
- Tinkerer (tool/craft-based buffs)
- Summoner (summon elemental minions)
- Healer / Cleric (party buff archetype)
- TBD if needed once core system proves stable

### Pocket leaderboards / time-trial scoring
- Track fastest pocket clears per affinity
- Display on Affinity Book
- Optional multiplayer competition

### Affinity-themed music per pocket
- 11 unique music tracks for the 11 pockets
- One ambient track per ring (outer, intermediate, center)
- Boss theme per boss (11 themes)

### Procedural dungeon generation
- Variant trap-room layouts per portal (variation pool from datapack)
- Random mini-boss placement
- Procedural resource distribution

### Affinity-specific player cosmetics
- Glow effects on player when affinity is high
- Footstep particles matching affinity
- Custom death animation per affinity?

---

## 12. Multiplayer

### Single-pocket scenarios (handled by current code)
- One player per pocket
- Boss kill = per-player advancement if shared
- Return position via per-player attachment

### Edge cases to handle per phase
- Phase 3: two players entering same pocket simultaneously
- Phase 3: race on dimension creation (handled by Infiniverse's per-portal key)
- Phase 4: boss kill sync across players → server-side flag + broadcast advancement
- Phase 4: return position race → per-player attachment (already implemented)

### Open questions
- Shared pocket world or per-player pocket?
- Cooperative boss mechanics or solo?
- Pocket entry party system?
- Shared vs private progression?

---

## 13. Performance

### Lean perf philosophy
- "As many particles as needed, as few as sensible"
- LOD-aware VFX (full close, half mid, minimal far)
- Boss phase transitions temporary
- Pocket gen caps to bounded ring

### Not targeting potato-PC
- Users opt into Sodium/Embeddium separately
- Mod assumes modern hardware

### LOD plan (rough)
- Spell VFX: full density <32 blocks, half 32-64, minimal beyond
- Boss aura particles: full <16 blocks, half 16-48
- Ambient particles: full <48 blocks, half beyond

---

## 14. Configuration

### Schema versioning
- `"schemaVersion": 1` per JSON
- Strict by default with `common.toml.allowSchemaMismatch` toggle for dev
- New fields default to safe fallbacks (forward-compat)

### Reload model
- Server configs hot-reload via `/reload` or `ModConfigEvent.Reloading`
- Lazy-apply: new entities read new values, existing entities keep snapshot
- Loader warns if changed field only takes effect on next generation
- Client configs hot-reload via ModConfigScreen
- Datapacks via standard `/reload` (F3+T)
- New `/elementalrealms reload` command

### Open questions
- JSON5 or plain JSON? (JSON5 allows comments — cleaner)
- Modpack vs datapack priority when both override shared field

---

## 15. Game-Testing & Workflow

### Test worlds
- `Test-FreshAffinities` — fresh survival; login roll, shard use, reveal flow
- `Test-DragonGate` — creative + End portal + dragon summoner; Dragon kill triggers School-portal spawn at Overworld spawn
- `Test-Pockets` — creative + pre-given affinity stones; pocket enter/exit + boss spawn
- `Test-ConfigTuning` — fresh world; verify `/elementalrealms reload` applies config edits

### Per-issue flow
1. Plan — issue spec updated in PLANS.md + GitHub
2. Branch — issue-first, off `dev`
3. Code — implement, run `mcp_intellij_build_project` until green
4. Push + Draft PR — push branch, open draft PR
5. Test — user game-tests from test worlds
6. Feedback — bugs in plain text or PR comments, fix on same branch
7. Sign-off — user says "fine" → I mark Ready for Review → you merge

---

## 16. Dropped / Rejected Ideas

> Track things we considered and dropped so we don't re-litigate later.

### Scroll of Awakening (replaced by Crystal Orb)
- Originally a scroll item
- Replaced with Crystal Orb for thematic reasons (orb feels more magical, scroll too mundane)

### Mana as Curios slot (replaced by separate bar)
- Originally Mana Core in Curios slot
- Replaced with separate bar + central mana system (more flexible, no Curios dep)

### 5 fixed rings for pockets (replaced by 3)
- Originally 5 rings (boss/innermost, hub, traps, resources, boundary)
- Replaced with 3 rings (outer spawn, intermediate, center) for tighter design

### Fixed 1000×1000 pocket size (replaced with variable)
- Originally hardcoded 1000×1000
- Replaced with variable per affinity, balanced during build

### Boss always drops stone (replaced with rare)
- Originally boss was guaranteed to drop affinity stone
- Replaced with stone being rare ultimate reward (not guaranteed)

### Single straight 5% shard (replaced with Small/Medium/Big)
- Originally single shard type, +5%
- Replaced with 3 sizes (1%/3%/5%) for loot-table variety and final-% flexibility

---

## 17. New Ideas (catch-all)

> Dump new ideas here. They'll get organized into sections above as they mature.

### Affinity scaling as visual
- Player character model changes based on dominant affinity (skin glow, particle aura)
- Tools / weapons change appearance when affinity matches?
- Player house/area in School reflects their dominant affinity?

### Affinity respec
- Void stone clears all (already in plan)
- Should there be a "less drastic" respec? (e.g. lower ONE affinity by 50%)
- Or is Void stone enough?

### Affinity stones from mini-bosses?
- Currently only boss drops stones
- Could mini-bosses drop stones at very low rate?
- Or are they exclusively boss-only?

### Pocket modifier events
- Random events per pocket run (boss appears mid-ring 2, special mini-boss spawns, etc.)
- Or is each pocket run predictable?

### Difficulty modes
- Easy / Normal / Hard modes per phase?
- Or single difficulty, balanced for hardcore?
- Open question

### Cross-affinity synergies (different from combo spells)
- Permanent player effects based on having 2+ affinities at high %
- Example: Fire 100% + Wind 100% = flying + fire resistance
- TBD if implemented

### Affinity-themed advancements
- One advancement per affinity milestone (Fire Master, Lightning Adept, etc.)
- Or just generic "Elemental Master"?

### Visual feedback for partial affinity %
- Player glow scales with affinity completion?
- Currently only shard/item usage shows progress
- Could be visual on character

### XP → Mana + Skill Tree ("Magic Book" brainstorm)

> Big-picture brainstorm, nicht lock-in. Größerer Scope-Shift — ersetzt Vanilla-XP-System komplett durch Mana + multi-layered Skill Tree.

**Kern-Shape:**
- XP komplett raus: keine XP-Drops mehr (Mobs, Ores, Smelting, Breeding, Fishing-Treasure-Bücher, Villager-Trades).
- Mending / Anvil-Combining / Rename-Cost / Enchanted-Book-Repair laufen über Mana statt XP.
- Bottles o' Enchanting entweder weg oder durch Mana-Potions ersetzt.
- Villager: keine XP-Voraussetzungen mehr — Unlock-Mechanik für Level-Gated-Trades (z.B. manche Enchanted Books) muss anders gelöst werden (Affinität? Skill-Level? Quest?).
- Enchanting-Table bleibt visuell, aber **Bookshelf-Scaling raus**. Bookshelves sind Deko oder liefern flaches Max-Level. Enchant-Stärke kommt rein aus Skill-Level.

**Skill Tree — multi-layered, "wirklich was Großes":**
- Mehrere Kategorien: Combat (Mage/Warrior/Bow-Stats), Magic (Spell-Power, Mana-Pool, Regen, Cooldown), Enchanting (Enchant-Power, Repair-Effizienz, Anvil-Discount), Utility (Mobilität, Harvesting, Defense, Looting, ...).
- "Multi-layered" = mehrere Tiefen-Ringe (Apprentice → Journeyman → Master → Grandmaster) mit Prerequisites zwischen Skills. Branches pro Archetyp denkbar (Mage-Tree vs Warrior-Tree vs Enchanting-Tree).
- Skill-Points aus was? Offene Optionen, kein Lock-in:
  - Strukturell an Milestones gekoppelt (Login-Roll, Dragon-Kill, jeder Boss-Kill, jede Affinität auf 100%)
  - Mana-Spend-basiert (Schwelle X verbraucht → Skill-Punkt)
  - Affinitäts-Tier-basiert (Tier-Aufstieg gibt Punkte)
  - Vanilla-XP-gespiegelte zweite Bar (würde Vanilla nur ersetzen statt rethinken — wahrscheinlich nicht die Idee)
- Respec: Void Stone räumt Affinitäten (§17, §5.3) — Skill-Punkte analog oder perm?

**Magic Book (vormals Affinity Book):**
- Tabs: **Affinitäten | Skill Tree | Mana Core**
- Mana-Core-Tab: Pool-Anzeige, Regen-Rate, evtl. Mana-Potion-Slot, evtl. Config zu beidem.
- §8.4 "Mana Core als Item (Curios-Slot)" wird damit vermutlich überflüssig — Mana Core als Tab statt Item. Bestätigung nötig: kommt das Item eh nicht?
- §7 + §10 müssen Affinity Book → Magic Book umbenennen. Phase 1 betroffen.

**Konsequenzen die ich sehe, du evtl. noch nicht:**
- **Mending wird potenziell sehr stark** wenn Mana-Payment: kein Anvil-Trip-Druck mehr → ständig vollrepariert. Skill "Repair-Effizienz" als Dämpfer? Oder Mending hinter Skill-Gate?
- **Infinity (per-arrow Mana-Drain)**: Infinity kostet statt "1 Pfeil im Inventar" eine Mana-Summe pro abgeschossenem Arrow. Macht das Enchantment balanced (kein infinite-Quiver mehr) und gibt Mana eine Combat-Senke jenseits der Spells. Konkreter Mechanik-Vorschlag: z.B. `costPerArrow = config["infinity"].manaPerArrow` (default 1 Mana / Arrow), Player-Cast prüft `player.getMana() >= cost` vor jedem Schuss, sonst Pfeil-Schuss geblockt oder Fallback auf normalen Arrow-Consumption. Tie-Breaker bei leerem Mana: entweder kein Schuss oder Schuss mit vollem Vanilla-Pfeil-Consume (deaktivierbar).
- **Anvil-Combining-Spirale**: im Vanilla ist 30+ Enchant auf 1 Item XP-Gated. Mit Mana-Gate → wie bounded man das? Exponentielles Mana-Cost-Scaling pro Combine, oder hartes Combine-Cap (z.B. max 3-4 Combines pro Item)?
- **Player-XP-Bar überm Kopf (Multiplayer)**: Vanilla zeigt XP-Level über jedem Spieler. XP = 0 → "0" oder leer über jedem Kopf? Ersatz: Affinitäts-Master-Level? Mana-Pool in %? Nichts?
- **Beacons**: Bezahlung ist XP. Behalten mit Mana-Bezahlung, oder ersatzlos raus?
- **Vanilla-Advancements mit XP-Bedingungen** (z.B. "Cover me in debris", Monster-Hunter-XP-Counts) → entweder weg oder umschreiben.
- **Enchanted Books aus Loot-Tables** (z.B. End-City, Dungeons, Fishing) droppen weiter — nur das Enchanting-Table-Use fällt weg bzw. wird durch Skill-Gate geregelt. Spieler können also Bücher finden aber nicht nutzen ohne Enchanting-Skill-Level.

**Skill-Tree-Scope "alle möglichen Bereiche" — was das konkret heißen könnte:**
- Combat: Nahkampf-Schaden, Bogen-Schaden, Spell-Power, Crit-Chance
- Magic: Mana-Pool, Mana-Regen-Rate, Cast-Speed, Cooldown-Reduction
- Enchanting: Enchant-Power-Level, Repair-Efficiency, Anvil-Discount (Mana-Cost)
- Mobility: Sprint-Speed, Jump-Height, Fall-Damage-Reduction
- Utility: Looting-Bonus, Harvesting-Speed, Hunger-Decay-Reduction
- Defense: Armor-Toughness-Bonus, Resistance-Duration, Status-Effect-Resist
- Progression-Gated: Pocket-Effect-Reduction (weniger Dimensional-Effects), Boss-Damage-Bonus
- Strukturierungsvorschlag, nicht vollständig, nicht Lock-in.

**Offene Fragen vor allem anderen:**
1. Skill-Points-Quelle: Milestone-strukturell / Mana-Spent / Tier-Up / Mix?
2. Skill-Tree-Struktur: kategorische Tabs oder ein großer Graph mit Prerequisites?
3. Beacons: behalten + Mana-Zahlung, oder ersatzlos raus?
4. Bottles o' Enchanting: komplett raus oder Mana-Potion-Pendant (Trank der sofort Mana füllt)?

**Phase-Einordnung (sehr grob):**
- Mana-System + Magic-Book-Grundgerüst + Enchanting-Table-Rewire → Phase 2 (Spells)
- Skill-Tree-Vollsystem → Phase 2 oder eigene Phase 2.5
- XP-Code-Path-Entfernung → muss quer durch alle Phasen mitgepatcht werden, kein einzelner Issue

---

## 18. Decisions Log (locked-in design choices)

> Mirror of §15 Controversial Design Decisions in PLANS.md. Keep in sync.

| # | Decision | Why |
|---|---|---|
| 1 | Dragon kill starts the mod | Progression gate — School + pockets + everything else unlocks after the kill |
| 2 | Pockets gated by barrier-progression, not by affinity | All accessible after Dragon kill; new tiers unlock via boss kills |
| 3 | Pockets don't require affinity to enter | Soft gate via dimensional effects + gear |
| 4 | Affinity state hidden until Reveal | Mystery, drives shard collection, makes Reveal a moment |
| 5 | Boss stone drop not guaranteed | Affinity stone is the ultimate reward |
| 6 | Boss death removes matching vanilla portal | Encourages variety over farming |
| 7 | Pockets persistent | Boss killed = stays killed. Configurable to regenerate |
| 8 | Eternal all-or-nothing | Lore: three eternals together are the foundation of existence |
| 9 | PvP not a design goal | Spells designed for PvE only |
| 10 | ER is vanilla-balanced | ER adds content on top of vanilla; sibling repo `dragonsrequiem-neoforge-1.21.1` owns the rework territory |

---

## 19. Open Questions (live)

> Working list of unresolved questions. Update as we go.

- [ ] Pocket size per affinity (final tuning)
- [ ] Dimensional effects list per pocket (full draft)
- [ ] Vanilla-MC effects per affinity at 100% (full list)
- [ ] Mana system implementation (menu / inventory slot / skills-tree UI)
- [ ] Combo spell list exact
- [ ] Boss walking / behavior per boss
- [ ] Dimension Staff use count
- [ ] Roll-logic distribution (exact skewing)
- [ ] Affinity mob spawn rates per tier
- [ ] Modded mob count
- [ ] Boss death → portal behavior (reappear? when?)
- [ ] Cross-affinity synergies implementation (yes/no)
- [ ] Difficulty modes (yes/no)

---

*Last updated: 2026-06-27*
