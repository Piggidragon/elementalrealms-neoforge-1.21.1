# Naming Conventions

> **Status:** Phase 0 — locked in for all NEW content (Phase 1+).
> **Audience:** everyone writing code, lang files, configs, or asset briefs for `elementalrealms`.
> **Replaces:** ad-hoc naming decisions that used to live in chat.
> **Lore-safety guardrail:** no characters, places, creatures, or plot points from any existing fictional work. Generic fantasy-academy tone only — never derivative.

When in doubt: pick the boring, generic option. A spell called "Fire Bolt" is fine; anything that sounds like it belongs to a specific franchise is not.

---

## 1. Display Names (lang files)

All user-facing strings live in `assets/elementalrealms/lang/en_us.json` (and de_de.json for German). Display names must read like a generic fantasy academy setting — no franchise references, no inside jokes.

### 1.1 Items

| Category | Pattern | Examples |
|----------|---------|----------|
| Affinity Shards | `<Size> Affinity Shard of <Element>` | `Small Affinity Shard of Fire`, `Big Affinity Shard of Lightning` |
| Affinity Stones | `Affinity Stone of <Element>` | `Affinity Stone of Wind` |
| Spell tomes | `<Spell Name> Tome` | `Fire Bolt Tome`, `Tidal Wave Tome` |
| Generic crafting mats | `<Adjective> <Material>` | `Refined Mana Crystal`, `Tarnished Bronze Ingot` |

`<Size>` ∈ { `Small`, `Big`, `Greater`, `Lesser` }. No `Tiny`, no `Massive`, no `Mega`. Stick to the four.

### 1.2 Bosses

Pattern: **Title + Element** (or "the <noun> of <element>").

Examples (canonical):
- `The Lord of Embers` (fire)
- `Tidal Sovereign` (water)
- `Stormcaller` (lightning)
- `Glacier Warden` (ice)

Boss names must be **registered centrally** in `NamingRegistry` (see §5). No hardcoded `"The Lord of Embers"` literals in entity classes or lang files — code reads `NamingRegistry.bossName(Affinity.FIRE)`.

### 1.3 Spells

Pattern: **Action verb + Element** (or just Action if element-agnostic).

Examples:
- `Fire Bolt`, `Fireball`, `Ember Lance`
- `Tidal Wave`, `Ice Shard`, `Frostbite`
- `Lightning Arc`, `Thunder Strike`
- `Wind Gust`, `Stone Fist`, `Gravity Well`

Spell names do **not** go through `NamingRegistry` — they're lang-file-only, since spells have no configurable flavour text yet.

### 1.4 Dimensions / Biomes / Structures

Pattern: `<Adjective> <Noun>` or `<Element> <Noun>`.

Examples: `Ember Wastes`, `Tidal Depths`, `Skyreach Spire`, `Void Sanctum`.

### 1.5 Affinity Display Names

| Identifier | Display Name | Notes |
|------------|--------------|-------|
| `fire` | `Fire` | |
| `water` | `Water` | |
| `wind` | `Wind` | |
| `earth` | `Earth` | |
| `lightning` | `Lightning` | |
| `ice` | `Ice` | |
| `sound` | `Sound` | |
| `gravity` | `Gravity` | |
| `life` | `Life` | |
| `space` | `Space` | |
| `time` | `Time` | |
| `void` | `Void` | |

Display names are *the same as the identifier* except for capitalisation. Modpack authors who want a custom name override `affinities.json → naming.affinities.<id>` (see §5).

---

## 2. Item IDs (Java + ResourceLocation)

Pattern: `lowercase_snake_case`, no plural, no `the_`, no underscores-as-separators in user-facing strings.

### 2.1 Prefixes

| Subsystem | Prefix | Example |
|-----------|--------|---------|
| Affinity shards | `affinity_shard_` | `affinity_shard_small_fire`, `affinity_shard_big_fire` |
| Affinity stones | `affinity_stone_` | `affinity_stone_lightning` |
| Boss summon items | `boss_summon_` | `boss_summon_lord_of_embers` |
| Spells (item form) | `spell_` | `spell_fire_bolt`, `spell_tidal_wave` |
| Generic crafting mats | no prefix, descriptive | `mana_crystal`, `bronze_ingot` |

### 2.2 Affinity Values (canonical identifiers)

These exact strings are used in code, JSON, and NBT. **Do not** introduce `flame`, `fuego`, `blaze`, `pyro`, etc.

```
fire, water, wind, earth, lightning, ice, sound, gravity, life, space, time, void
```

Adding a new element is a separate decision (PLANS.md §A.5). Don't sneak one in under "just a variant".

---

## 3. Class Naming (Java)

### 3.1 Capitalisation Rules

| Form | Use for | Example |
|------|---------|---------|
| `Affinities` | Enum / collection class | `public enum Affinities { ... }` |
| `affinity` | Variable / parameter | `Affinity affinity = Affinity.FIRE;` |
| `affinities.json` | Config file | `config/elementalrealms/affinities.json` |
| `AffinityConfig` | Loader class for that JSON | `public class AffinityConfig { ... }` |

Avoid `AffinityEnum`, `AffinityManager`, `AffinityHelper` — they're noise. Use the noun (`Affinities`) or the role (`AffinityConfig`).

### 3.2 Inheritance Pattern

Abstract base + per-element subclass:

```
BossEntity (abstract, in registries/entities/)
  ├── BossFireEntity       (extends BossEntity)
  ├── BossWaterEntity      (extends BossEntity)
  └── ...
```

If a class is only ever an abstract base, **mark it `abstract`** — don't ship empty concrete parents.

### 3.3 Common Suffixes

| Suffix | Meaning | Example |
|--------|---------|---------|
| `Entity` | `Entity` subclass (mob / projectile) | `BossFireEntity`, `SpellProjectileEntity` |
| `Block` | `Block` subclass | `ManaCoreBlock` |
| `BlockEntity` | `BlockEntity` subclass | `ManaCoreBlockEntity` |
| `Item` | `Item` subclass | `AffinityShardItem` |
| `Config` | JSON5 / TOML loader | `AffinityConfig`, `DimensionsConfig` |
| `Registry` | Central string / data registry | `NamingRegistry` |
| `Packet` | Network payload class | `ReloadConfigPacket` |
| `Command` | Brigadier command | `ReloadConfigCommand` |

Don't mix suffixes: `BossManager`, `FireSpellClass`, `TidalEntityHandler` — pick one convention.

### 3.4 Avoid

- `Util`, `Utils`, `Helper` (too vague)
- `Base` (already implicit in abstract classes)
- `Impl` (prefer specific names)
- `Legacy`, `New`, `Old`, `V2` (rename properly instead)

---

## 4. Method Naming

Standard Java conventions, applied consistently:

| Pattern | Use |
|---------|-----|
| `getXxx()` / `setXxx()` | Field accessors |
| `addXxx()` / `removeXxx()` | Collection mutation |
| `isXxx()`, `hasXxx()`, `canXxx()` | Boolean predicates (pick `is` for state, `can` for ability, `has` for possession) |
| `matchesXxx()`, `isValidXxx()` | Predicates returning `boolean` |
| `onXxx()` | Event hooks / lifecycle (`onLoad`, `onReload`, `onDeath`) |
| `createXxx()`, `buildXxx()` | Factory methods |
| `registerXxx()` | DeferredRegister / event-bus registration |

Avoid Hungarian-style prefixes (`bIsFire`, `iCount`) — IntelliJ inspections flag these.

---

## 5. NamingRegistry (single source of truth)

`NamingRegistry` lives at `net.piggidragon.elementalrealms.registries.naming.NamingRegistry`. It is the **only** place boss names, dimension names, and affinity display names live at runtime.

### 5.1 What goes through it

- Boss display names (per affinity / per boss id)
- Dimension display names (per dimension id)
- Affinity display names (per affinity id)
- Anything else a modpack author might reasonably want to rename

### 5.2 What does NOT go through it

- Spell names (lang-file-only for now)
- Generic item names (lang-file-only)
- Tooltip text (lang-file-only)
- Log messages (internal)

### 5.3 API sketch

```java
// In Java code:
NamingRegistry.bossName(Affinity.FIRE);              // -> "The Lord of Embers" or override
NamingRegistry.dimensionName("pocket");             // -> "Pocket" or override
NamingRegistry.affinityName(Affinity.LIGHTNING);    // -> "Lightning" or override
```

### 5.4 Modpack override

`affinities.json` will gain a `naming` block (deferred to the phase that actually consumes it):

```json5
{
  naming: {
    affinities: {
      fire: "Flame",
      lightning: "Storm",
    },
    bosses: {
      lord_of_embers: "The Cinder King",
    },
    dimensions: {
      pocket: "Pocket Realm",
    },
  },
}
```

Overridden names survive `/elementalrealms reload` (already implemented in #18).

### 5.5 Rule of thumb

**If a display name will appear in more than one place (boss card + kill announcement + lore book), it goes through `NamingRegistry`.** If it only appears once (a single item tooltip), it stays in the lang file.

---

## 6. Reference Checklist (per issue)

When creating a new issue that introduces a content item (item, block, entity, dimension, spell), the issue body should answer:

- [ ] Display name(s) listed, following §1 patterns
- [ ] Item / entity / dimension ID follows §2.1 / §2.2
- [ ] Class names follow §3 (no `Manager` / `Helper` / `Util`)
- [ ] If a display name is reused in ≥2 systems, registered in `NamingRegistry` (§5)
- [ ] No references to characters, places, creatures, or plot points from any existing fictional work (lore-safety guardrail at top)

Reviewers will bounce issues that don't answer these.

---

## 7. Examples — good vs bad

| Bad | Why | Good |
|-----|-----|------|
| `HeroicFireSpell` | "Heroic" sounds trademarked | `FireBoltSpell` |
| `The_Mystic_Tower` | "Mystic Tower" is a stock franchise name | `ArcaneSpire` |
| `TideSovereignEntity_Boss` | redundant `Boss` suffix | `BossWaterEntity` |
| `ManaUtil` | vague | `ManaCalculator` |
| `spell_FireBolt` | camelCase in id, leading `spell_` ok but value should be snake_case | `spell_fire_bolt` |
| `fire_shard_small` | wrong order (size before element) | `affinity_shard_small_fire` |
| `Lord_of_Embers_Boss` | boss suffix already in registry id | `lord_of_embers` |
| `The Void` | generic but article feels off | `Void` (capitalised identifier) |

---

## 8. Open Questions

- [ ] Do spell display names need to go through `NamingRegistry` once modpack-rename support becomes a real feature? (Defer until first modpack request.)
- [ ] Are dimension subtypes (pocket vs overworld variant) one registry or two? (Defer to Phase 2.)
- [ ] Pluralisation rule for "Affinities" vs "Affinity" in display text? (Defer to Phase 1 when first UI shows up.)

When a question resolves, update this doc in the same PR that introduces the change.