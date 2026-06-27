# 🌟 Elemental Realms

> **A Minecraft magic & dimension mod for NeoForge 1.21.1**
>
> *Discover elemental affinities, travel through dimensional portals, and face an empowered Ender Dragon.*

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.214-orange.svg)](https://neoforged.net)
[![Mod Version](https://img.shields.io/badge/Version-0.3.2-blue.svg)](https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1/releases)
[![License](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE)
[![Build](https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1/actions/workflows/build.yml/badge.svg)](https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1/actions/workflows/build.yml)

---

## ✨ Features

### 🔮 Affinity System

Harness the power of 12 elemental affinities across 3 tiers. New players receive random affinities on their first login.

| Tier          | Description                               | Affinities                                                       |
|---------------|-------------------------------------------|------------------------------------------------------------------|
| **ELEMENTAL** | Basic elemental powers                    | Fire, Water, Wind, Earth                                         |
| **DEVIANT**   | Advanced — requires base affinity at 100% | Lightning (←Fire), Ice (←Water), Sound (←Wind), Gravity (←Earth) |
| **ETERNAL**   | Ultimate — only one per player            | Life, Space, Time                                                |
| **VOID**      | No affinity / resets all affinities       | Void                                                             |

**Items:**

- **Affinity Stones** — Right-click to instantly gain 100% completion of a specific affinity (consumed on use).
- **Affinity Shards** — Right-click to increment affinity progress by +5% (consumed on use).
- **Void Stone** — Clears all current affinities.

**Commands** (OP level 2):

- `/elementalrealms affinities list` — Shows your current affinities
- `/elementalrealms affinities set <affinity>` — Sets a specific affinity
- `/elementalrealms affinities clear` — Removes all affinities
- `/elementalrealms affinities reroll` — Re-rolls random affinities
- `/elementalrealms affinities roll show` — Lists every field in the
  affinities roll config with its current effective value (see §5.2 of
  `docs/PLANS.md` for field semantics).
- `/elementalrealms affinities roll set <field> <value>` — Mutates a single
  roll-config field on disk and reloads without server restart. Int fields
  accept `0..100`; array fields (any name ending in `SkewPercent`) accept
  comma-separated integers that must sum to `≤ 100`.

**Login roll (first login):** one random ELEMENTAL affinity at hardcoded 100%
(the player's anchor element, always full strength), plus a rare chance of the
matching DEVIANT as a partial (capped at the configured `deviantMaxCompletion`,
default 80%), plus a decaying loop that may add additional ELEMENTAL partials
(capped at `elementalMaxCompletion`, default 80%). Partial completions use a
continuous left-skew distribution (`(int)(max * U^slope)`, default `slope=3`)
so low values are common and high values are rare — the @ max bucket itself
is < 1%. ETERNAL affinities are never assigned at login — they come from
boss stones only. Tune the partial caps and skew via
`config/elementalrealms/affinities.json` or the two `roll` commands above.

### 🪄 Dimension Staff

A staff with **16 durability** that opens a temporary portal to the **School Dimension**.

- **Right-click** to fire a 2-second spiral beam animation.
- A portal appears at the target location and lasts **10 seconds**.
- Only usable in vanilla dimensions (Overworld, Nether, End).
- Using the staff again removes your previous portal first.
- Damageable — 16 uses before breaking.
- All affinities grant mastery over different elements (Fire, Water, Earth, Wind, Lightning, Ice, Sound, Gravity, Time,
  Space, Life, Void).

### 🐉 Dragon Laser

The **Ender Dragon** has a new attack! If a player stays still for **3 seconds**, the dragon fires a **piercing laser
beam** from its head.

- **5-second cooldown** between laser attacks per player.
- Destroys blocks in the beam's path (drops items).
- Custom particle effects and a unique `laser_beam.ogg` sound.
- Sound volume scales with distance from the beam line.
- Rewards players who keep moving during the fight.

### 🌍 Dimensions

#### School Dimension

`elementalrealms:school`

A mystical realm accessible through the **Dimension Staff** or via a naturally spawned portal after defeating the Ender
Dragon.

- Flat void world with no natural terrain.
- Contains a **spawn platform** assembled from 4 jigsaw variants (wood, nether, end, grass).
- Bed works, piglins are not safe, no raids.
- Overworld-like ambient lighting and sky effects.

#### Dynamic Realms

Dynamically generated dimensions created per-portal using the **Infiniverse API**. Each gets its own isolated **bounded
world** (21×21 chunks / 336×336 blocks) with a custom generator.

- Worlds are placed in a ring pattern around origin, expanding outward.
- Generation centers persist across server restarts via `GenerationCenterData`.
- Portals back to the Overworld are created at the destination.
- Dimensions are cleaned up when their portal is removed.

### 🏗️ World Generation

Portals generate naturally in all biomes:

| Type               | Rarity            | Placement              |
|--------------------|-------------------|------------------------|
| Surface Portal     | ~1 per 250 chunks | On terrain surface     |
| Underground Portal | ~1 per 500 chunks | Between Y=-60 and Y=40 |

Underground portals cause an **explosion** on spawn to clear space (configurable via `primed` flag).

### 🏆 Advancements

- **The Journey Begins...** — Defeat the Ender Dragon to unlock dimensional travel.
- **Dimensional Traveler** — Obtain a Dimension Staff.

### 🎨 Creative Tabs

- **Affinity Items** — All affinity stones, shards, and essences.
- **Items** — Dimension Staff and other utility items.
- **Blocks** — Future block additions (reserved).

### 🔧 Technical

- **Custom Rarities:** LEGENDARY (gold text) for deviant stones, MYTHIC (dark purple text) for eternal stones —
  registered via NeoForge's `@EnumExtension`.
- **Custom Damage Type:** `laser` — bypasses armor, shields, enchantments, and knockback.
- **Custom Chunk Generator:** `BoundedChunkGenerator` limits worldgen to a configurable radius.
- **Data Attachments:** Affinities, portal target levels, and return positions persist via NeoForge attachment system.
- **Network:** Payload-based packet system with client/server handlers for affinity effects, laser beam rendering, block
  destruction, and entity damage.
- **Datagen:** Automatic generation of item models, recipes, advancements, loot tables, block/item tags, sounds, and
  biome modifiers.

## 📋 Requirements

| Dependency      | Version           |
|-----------------|-------------------|
| **Minecraft**   | 1.21.1            |
| **Mod Loader**  | NeoForge 21.1.214 |
| **Java**        | 21+               |
| **Infiniverse** | 2.0.1.0           |
| **Lodestone**   | 1.7.0             |
| **Curios API**  | 9.5.1             |

## 📥 Installation

### For Players

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1.
2. Download the latest release from [Releases](https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1/releases).
3. Place the `.jar` in `.minecraft/mods/`.
4. Launch Minecraft with the NeoForge profile.

### For Developers

```bash
git clone https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1.git
cd elementalrealms-neoforge-1-21-1
./gradlew build
```

The built mod jar will be in `build/libs/`.

### Run Configurations

- `runClient` — Launches the mod in a Minecraft client.
- `runServer` — Launches a dedicated server.
- `runGameTests` — Runs registered game tests.
- `runData` — Runs data generators (outputs to `src/generated/resources/`).

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/de/piggidragon/elementalrealms/
│   │   ├── ElementalRealms.java          # Main mod class
│   │   ├── ElementalRealmsClient.java     # Client setup
│   │   ├── client/                        # Client rendering, GUI, particles
│   │   ├── datagen/                       # Data generators (advancements, recipes, etc.)
│   │   ├── events/                        # Server event handlers
│   │   ├── magic/affinities/              # Affinity enum, types, management, rolls
│   │   ├── mixin/                         # EnderDragon mixin (laser beam)
│   │   ├── packets/                       # Network packets & handlers
│   │   ├── registries/                    # All DeferredRegister registrations
│   │   ├── saveddata/                     # Persistent world data
│   │   └── util/                          # Utilities (rarities, portal utils)
│   ├── resources/                         # Static assets (textures, models, data packs)
│   └── templates/                         # Metadata templates (gradle-expanded)
└── generated/resources/                   # Auto-generated assets (datagen output)
```

## 🤝 Contributing

This project is developed by **Piggidragon**. Bug reports and feature suggestions are welcome
via [GitHub Issues](https://github.com/Piggidragon/elementalrealms-neoforge-1-21-1/issues).

Pull requests require passing CI checks (GitHub Actions build workflow) and should include JavaDoc documentation for new
code.

## 📝 License

This project is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**. See [`LICENSE`](LICENSE) for the full text.

**What this means in plain terms:**
- You can use, modify, and redistribute this mod freely.
- If you distribute a modified version, you must make the source code of your modifications available under LGPL-3.0 as well.
- You can include this mod in modpacks (both open-source and proprietary) without the modpack itself having to be LGPL.
- This mod can dynamically link to other mods without those other mods having to be LGPL.

For the full legal text, see [`LICENSE`](LICENSE).

---

*Built with [NeoForge](https://neoforged.net/) | Powered
by [Infiniverse](https://github.com/Commoble/infiniverse) & [Lodestone](https://github.com/LodestarMC/Lodestone)*