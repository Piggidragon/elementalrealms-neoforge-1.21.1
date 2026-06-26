# Naming Conventions

> **Status:** Phase 0 — locked in for all NEW code (Phase 1+).
> **Audience:** everyone writing Java for `elementalrealms`.
> **Scope:** code style only. Display names live in `lang/en_us.json` or are generated on the fly by the code that uses them — no central naming registry.
> **Lore-safety guardrail:** no characters, places, creatures, or plot points from any existing fictional work. Generic fantasy-academy tone only — never derivative.
> **Tone of this doc:** guidelines, not law. Project size and team preference can override. When in doubt, match the closest existing code in the same area of the codebase.

When in doubt, follow Standard Java / Google Java Style. This doc lists the mod-specific rules on top of that.

---

## 1. Java Conventions

### 1.1 Classes

- **PascalCase.** One noun per class. No abbreviations.
- **Main class per topic** uses the `Mod<Thema>` prefix and owns the registry / registration logic for that topic.
- **Subclasses / values** drop the `Mod` prefix. Enum constants, per-element subclasses, payload records, etc. live under the main class but are not `Mod`-prefixed.

Examples in current code:

| Topic | Main class | Subclasses / values |
|-------|------------|---------------------|
| Affinities | `ModAffinities` | `Affinity`, `AffinityType`, `AffinitiesRoll` |
| Items | `ModItems` | `AffinityStone`, `AffinityShard`, `DimensionStaff`, `SchoolStaff`, `HandEquipmentItems` |
| Blocks | `ModBlocks` | (per-block subclasses) |
| Configs | `ModConfigs` | `AffinityConfig`, `DimensionsConfig`, `DragonConfig`, … |
| Creative tabs | `ModCreativeTabs` | (per-tab subclasses) |
| Sounds | `ModSounds` | (per-sound subclasses) |
| Commands | `ModCommands` | `ElementalRealmsCommand` |
| Packets | `ModPacketHandler` | `AffinitiesOpenBookPacket`, `AffinitiesSuccessPacket`, `EnderDragonLaserBeamPacket`, … |
| Rarities | `ModRarities` | (rarity proxies: `LEGENDARY`, `MYTHIC`) |

`Mod` is reserved for **main-class-of-a-topic** names. Don't slap `Mod` on utilities, helpers, or one-off subclasses.

### 1.2 Inheritance

- If a class is only ever a base for other classes, **mark it `abstract`**. Don't ship empty concrete parents.
- Abstract base + per-element subclass is the default for any "family" of similar entities (bosses, spells, custom mobs).

```
BossEntity (abstract)
  ├── BossFireEntity
  ├── BossWaterEntity
  └── ...
```

- Prefer **composition over inheritance** for shared behaviour. Inheritance only when the subclass genuinely *is a* base.

### 1.3 Common Suffixes

| Suffix | Meaning |
|--------|---------|
| `Entity` | `Entity` subclass (mob, projectile) |
| `Block` | `Block` subclass |
| `BlockEntity` | `BlockEntity` subclass |
| `Item` | `Item` subclass |
| `Config` | JSON5 / TOML loader |
| `Packet` | Network payload |
| `Command` | Brigadier command |
| `Handler` | Event handler / lifecycle hook |
| `Provider` | Datagen provider |
| `Codec` | Codec definition (often a `record`) |

Pick one suffix per class. Don't mix: no `BossManager`, `FireSpellClass`, `TidalEntityHandler`.

### 1.4 Avoid (as class-name suffixes / prefixes)

These suffixes / prefixes add noise without information when used as class names:

- `Util`, `Utils` — too vague. The class probably does too many things.
- `Helper` as a **class name** — same problem. A folder called `helper/` is fine (it groups helper code for a topic); a class called `AffinitiesHelper` is not. Use a specific role instead: `AffinitiesRoll`, `AffinitiesCalculator`, etc.
- `Base` — already implicit in `abstract` classes.
- `Impl` — prefer specific names.
- `Legacy`, `New`, `Old`, `V2` — rename properly instead.
- `Manager` — usually overlaps with the `Mod<Thema>` main class or a `Handler`.

The rule of thumb: if a class needs `Util`/`Helper` to describe itself, name it after its concrete role instead. `AffinitiesRoll` is a good name. `AffinitiesHelper` is not.

### 1.5 Interfaces

- One-method interfaces: verb-object form (`Builder`, `Renderer`, `Codec`).
- Multi-method interfaces / capability interfaces: noun form (`Spell`, `Affinities`, `CustomMob`).
- Prefer **functional interfaces** (`@FunctionalInterface`) for single-method contracts.

### 1.6 Records

Use Java `record` for:

- Immutable value types: configs, payload payloads, codec targets.
- Data carriers between systems: `AffectedPlayer`, `PocketSpawn`, etc.

Use regular classes when:

- The type needs mutation, inheritance, or builder ergonomics.

---

## 2. Method Naming

Standard Java conventions, applied consistently:

| Pattern | Use |
|---------|-----|
| `getXxx()` / `setXxx()` | Field accessors |
| `addXxx()` / `removeXxx()` | Collection mutation |
| `isXxx()`, `hasXxx()`, `canXxx()` | Boolean predicates — `is` for state, `can` for ability, `has` for possession |
| `matchesXxx()`, `isValidXxx()` | Predicates returning `boolean` |
| `onXxx()` | Event hooks / lifecycle (`onLoad`, `onReload`, `onDeath`) |
| `createXxx()`, `buildXxx()` | Factory methods |
| `registerXxx()` | DeferredRegister / event-bus registration |
| `loadXxx()`, `saveXxx()` | Persistence entry points |

Avoid Hungarian-style prefixes (`bIsFire`, `iCount`) — IntelliJ inspections flag these.

---

## 3. Variables, Parameters, Fields

- `lowerCamelCase` for variables, parameters, non-static fields.
- `UPPER_SNAKE_CASE` for `static final` constants. Group constants at the top of the class.
- `lowerCamelCase` for non-final static fields too — only `static final` gets the UPPER_SNAKE_CASE.
- Boolean variables use `is`/`has`/`can` prefix in the same way as predicates: `isActive`, `hasMana`, `canCast`.
- One variable per line. Avoid multi-assign (`a = b = c = 0`).

---

## 4. Packages and Folder Structure

### 4.1 Top-level layout

```
de.piggidragon.elementalrealms
├── ElementalRealms.java          # main mod entry
├── ElementalRealmsClient.java    # client entry
├── client/                       # client-only code
├── datagen/                      # data generation providers
├── events/                       # Forge event handlers
├── magic/                        # gameplay mechanics (affinities, spells, mana)
├── mixin/                        # vanilla mixins
├── packets/                      # network payloads + handler
├── registries/                   # registration (items, blocks, entities, configs, …)
├── saveddata/                    # SavedData / world-attached data
└── util/                         # topic-layer for code that doesn't fit elsewhere — see §4.5
```

Layer convention: `client/`, `events/`, `magic/`, `packets/`, `registries/`, `util/` are top-level. Each owns one concern. New topics that don't fit get a new top-level layer (e.g. `mana/`, `quests/`) rather than being squeezed into an existing one.

### 4.2 Subfolders inside a layer

Inside a layer folder, group by **topic** (the thing the code is about), not by Java type (the shape of the file). Topics grow as the mod grows — start flat, split when a topic gets crowded.

Current shape of `registries/`:

```
registries/
├── items/
│   └── magic/
│       ├── affinities/         # AffinityStone, AffinityShard, etc.
│       │   └── custom/         # concrete Item subclasses
│       └── equipment/          # hand-held gear, eventually armor, etc.
│           └── hand/
│               └── custom/     # SchoolStaff, etc.
├── blocks/                     # ModBlocks + per-block subclasses
├── entities/
│   ├── client/                 # client-side entity code
│   │   └── renderer/
│   │       └── misc/           # PortalEmptyRenderer, etc.
│   ├── custom/                 # server-side custom entities
│   │   └── misc/               # PortalEntity, etc.
│   └── ModEntities.java        # main registration class
├── configs/                    # one *Config loader per JSON
├── commands/                   # ModCommands + each Command subclass
├── level/                      # dimension + dimension-manager code
├── sounds/                     # ModSounds + per-sound subclasses
├── worldgen/
│   ├── chunkgen/
│   ├── features/
│   │   └── custom/
│   │       └── entities/       # PortalSpawnFeature, etc.
│   └── structures/
│       └── school/             # SchoolDimensionPlatform, etc.
├── attachments/                # ModAttachments + sync code
├── guis/                       # menus, screens
└── rarities/                   # ModRarities (LEGENDARY, MYTHIC)
```

Current shape of `packets/`:

```
packets/
├── ModPacketHandler.java
├── ModStreamCodecs.java
└── custom/
    ├── affinities/             # AffinitiesOpenBookPacket, AffinitiesSuccessPacket, etc.
    ├── enderdragon/            # EnderDragonLaserBeamPacket, etc.
    └── …
```

Current shape of `magic/`:

```
magic/
└── affinities/
    ├── Affinity.java
    ├── AffinityType.java
    ├── ModAffinities.java
    └── helper/                 # AffinitiesRoll, future AffinitiesCalculator, etc.
```

The `helper/` folder inside a topic is allowed when a topic accumulates more than one helper class — see §1.4.

### 4.3 Cross-layer code

Code that touches multiple topics goes in the layer that owns the *trigger*, not the topic:

- A boss-kill handler that fires on entity death and updates an attachment: `events/BossDeathHandler.java`, not `registries/entities/`.
- A client-side GUI for the Affinity Book: `client/gui/AffinityBookScreen.java`, not `magic/affinities/`.

When in doubt: the layer is decided by the *first thing that fires*. Event handlers live in `events/`. Init code lives in `ElementalRealms.java` / `ElementalRealmsClient.java`. Registration code lives in `registries/`.

### 4.4 The `registries/` vs `util/` split

`registries/` is for **registration code** — anything that hooks into NeoForge's `DeferredRegister`, datagen, codecs, packet registration. If a class touches the registry system, it belongs here.

`util/` is for **everything else that doesn't fit a layer** — generic helpers, topic-specific utilities that don't have a natural home. Examples in current code:

- `util/entities/portal/PortalUtils.java` — portal-related helpers that aren't a registry concern.
- `util/math/` (future), `util/nbt/` (future) — generic mod-helpers if/when they show up.

The rule of thumb: if the class knows about a specific topic (affinities, portals, dragons, …), put it in `util/<topic>/` rather than in the topic's main folder. Keeps the topic folder for the *primary* code (registrations, main classes, subclasses) and pushes helpers one level away.

### 4.5 Folder depth

Don't go deeper than 4 levels without a good reason. Current max is:

```
registries/items/magic/affinities/custom/  →  5 levels, OK because it's the leaf for item subclasses
```

If a folder splits into "main" + "custom/", the "custom/" folder is the leaf for subclass files, the parent holds the registration class (`ModItems`, `ModEntities`, …).

If a topic is small enough to fit in one or two files, keep it flat. Split into subfolders when you have ≥3 files on the same concern.

---

## 5. Resource Locations and IDs

All `ResourceLocation` IDs in the mod use the namespace `elementalrealms`.

- **IDs:** `lowercase_snake_case`. Underscores separate words, hyphens are fine for compound concepts (`spell-fire-bolt` is allowed; `spell_fire_bolt` is the preferred form).
- **No plural in IDs.** `affinity_stone_fire`, not `affinity_stones_fire`.
- **No article prefixes.** No `the_`, no `a_`, no `an_`.
- **Affinity values** (used in code, JSON, NBT) are a closed set — see below. Do not introduce `flame`, `fuego`, `blaze`, `pyro` etc. Adding a new affinity is a separate decision.

Canonical affinity identifiers (locked):

```
fire, water, wind, earth, lightning, ice, sound, gravity, life, space, time, void
```

### 5.1 Item ID prefixes

| Subsystem | Prefix | Example |
|-----------|--------|---------|
| Affinity stones | `affinity_stone_` | `affinity_stone_fire` |
| Affinity shards | `affinity_shard_` | `affinity_shard_fire` |
| Affinity essence / misc | `affinity_` | `affinity_essence_fire` |
| Boss summon items | `boss_summon_` | `boss_summon_lord_of_embers` |
| Spell items | `spell_` | `spell_fire_bolt` |
| Generic crafting mats | none, descriptive | `mana_crystal`, `bronze_ingot` |

When a size prefix exists (`small`, `medium`, `big`), put it **before the affinity**: `affinity_shard_small_fire`, not `affinity_shard_fire_small`.

### 5.2 Lang keys

Lang keys mirror the item ID: `item.elementalrealms.affinity_stone_fire`. Display strings live in `src/main/resources/assets/elementalrealms/lang/en_us.json`. English only.

---

## 6. Display Names

There is **no central naming registry**. Display names live in one of two places, depending on what they are:

1. **Static names** (dimension staff, custom items that aren't tied to a system) — hardcoded in `lang/en_us.json`.
2. **Names derived from system state** (shard names from affinity + size, boss titles from affinity, spell names from spell id) — generated on the fly by the system that uses them. Format string lives next to the code that formats it.

Examples of on-the-fly generation:

```java
// In ModAffinities or wherever the shard item is built:
public static String shardName(Affinity affinity, ShardSize size) {
    return size.label() + " Affinity Shard of " + affinity.displayName();
}

// In a future BossTitles class (Phase 5):
public static String titleFor(Affinity affinity) {
    return switch (affinity) {
        case FIRE      -> "Lord of Embers";
        case WATER     -> "Tidal Sovereign";
        case LIGHTNING -> "Tempest Conductor";
        ...
    };
}
```

The point: format strings live with the code that emits them. If a format string is used in more than one place, refactor it into a `static final` on the relevant `Mod<Thema>` class (or a small dedicated helper class in the same package) — not into a separate registry.

### 6.1 Lore-safety

Display names follow the same lore-safety guardrail as the rest of the mod:

- Generic fantasy-academy tone only.
- No references to characters, places, or plot points from any existing fictional work.
- "Fire Bolt" is fine. Anything that sounds like it belongs to a specific franchise is not.

---

## 7. Reference Checklist (per issue)

When creating a new issue that introduces code, the issue body should answer:

- [ ] Class names follow §1 (PascalCase, `Mod<Thema>` for the topic main class, no `Util`/`Helper` as class names)
- [ ] Method names follow §2
- [ ] Folder placement follows §4 (matches the closest existing code in the same area of the codebase)
- [ ] Item / entity / dimension IDs follow §5 (`lowercase_snake_case`, no article prefixes, canonical affinity values)
- [ ] Display names follow §6 (lang file or generated on the fly from the code that uses them — no central registry)
- [ ] No references to characters, places, creatures, or plot points from any existing fictional work (lore-safety guardrail at top)

Reviewers will bounce issues that don't answer these.

---

## 8. Examples — good vs bad

| Bad | Why | Good |
|-----|-----|------|
| `ModFireBossHelper` | mixes `Mod`, `Helper`, topic-in-name | `BossFireEntity extends BossEntity` |
| `AffinitiesHelper` | `Helper` as class name adds no info | `AffinitiesRoll`, `AffinitiesCalculator` (specific role) |
| `BossManager` | `Manager` noise; overlaps with `ModBosses` | `ModBosses` + per-boss handlers |
| `HeroicFireSpell` | "Heroic" sounds trademarked | `FireBoltSpell` |
| `spell_FireBolt` | camelCase in id | `spell_fire_bolt` |
| `fire_shard_small` | wrong order (size before affinity) | `affinity_shard_small_fire` |
| `Lord_of_Embers_Boss` | redundant `Boss` suffix on the id | `lord_of_embers` (or `boss_summon_lord_of_embers` for the item) |
| `NamingRegistry.bossName(Affinity.FIRE)` | we don't have a central registry anymore | `BossTitles.titleFor(Affinity.FIRE)` next to the boss code |