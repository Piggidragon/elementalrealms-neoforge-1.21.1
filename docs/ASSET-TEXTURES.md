# Asset Tracker — Textures (2D)

> Skeleton + initial inventory of all 2D textures the mod currently ships.

## How to use this file

Every 2D texture the mod ships is listed here with type, source, and the affinity/tier it belongs to. Update this file **before** requesting a texture from the artist — the brief is the contract between the brief author and the artist.

### Column legend

| Column | Meaning |
|---|---|
| ID | Short identifier used in code (e.g. `affinity_stone_fire`). |
| Path | Runtime path under `assets/elementalrealms/textures/`. |
| Type | `item` / `block` / `entity` / `particle` / `gui` / `environment`. |
| Status | `TODO` / `IN PROGRESS` / `REVIEW` / `DONE`. |
| Resolution | `WxH` px. Items/blocks typically 16x16 (or 32x32 for hero assets); entities 64x64; GUI per slot. |
| Palette | Hex codes that must appear; cite the affinity/tier they belong to. |
| Mood-board | Link to inspiration references. |
| Phase | Phase the asset ships with. |
| Notes | Free text — must-include / must-avoid, transparent BG rules, animation frames count. |

### Affinity palettes (canonical)

When a Phase ships textures per affinity, they should draw from a canonical palette for that affinity. **No canonical palette is locked in yet** — current stone/shard textures were painted ad-hoc by Piggidragon and the colour-per-affinity mapping is implicit in the existing PNGs. Once the first per-affinity hero asset is requested, this section gets the actual hex codes extracted from the existing PNGs as the baseline palette.

| Affinity | Primary | Secondary | Accent |
|---|---|---|---|
| Fire    | _TBD_ | _TBD_ | _TBD_ |
| Water   | _TBD_ | _TBD_ | _TBD_ |
| Wind    | _TBD_ | _TBD_ | _TBD_ |
| Earth   | _TBD_ | _TBD_ | _TBD_ |
| Lightning | _TBD_ | _TBD_ | _TBD_ |
| Ice     | _TBD_ | _TBD_ | _TBD_ |
| Sound   | _TBD_ | _TBD_ | _TBD_ |
| Gravity | _TBD_ | _TBD_ | _TBD_ |
| Life    | _TBD_ | _TBD_ | _TBD_ |
| Space   | _TBD_ | _TBD_ | _TBD_ |
| Time    | _TBD_ | _TBD_ | _TBD_ |
| Void    | _TBD_ | _TBD_ | _TBD_ |

## Textures — Item (Affinity Stones)

Datagen-registered. Each row is one `affinity_stone_<affinity>.png` in `textures/item/`. Tier per affinity follows `AffinityItems`: ELEMENTAL=EPIC rarity, DEVIANT=LEGENDARY, ETERNAL=MYTHIC, VOID=EPIC.

| ID | Path | Type | Status | Resolution | Palette | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|
| `affinity_stone_fire` | `item/affinity_stone_fire.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_stone_water` | `item/affinity_stone_water.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_stone_wind` | `item/affinity_stone_wind.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_stone_earth` | `item/affinity_stone_earth.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_stone_lightning` | `item/affinity_stone_lightning.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Fire). |
| `affinity_stone_ice` | `item/affinity_stone_ice.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Water). |
| `affinity_stone_sound` | `item/affinity_stone_sound.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Wind). |
| `affinity_stone_gravity` | `item/affinity_stone_gravity.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Earth). |
| `affinity_stone_life` | `item/affinity_stone_life.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ETERNAL tier. |
| `affinity_stone_space` | `item/affinity_stone_space.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ETERNAL tier. |
| `affinity_stone_time` | `item/affinity_stone_time.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | ETERNAL tier. |
| `affinity_stone_void` | `item/affinity_stone_void.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | VOID — placeholder rarity (EPIC), per `affinities.json.rarities` override. |

## Textures — Item (Affinity Shards)

Datagen-registered. No ETERNAL or VOID shards per spec (shards = +1/+3/+5% partials, ETERNAL is all-or-nothing, VOID is the "no affinity" sentinel).

| ID | Path | Type | Status | Resolution | Palette | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|
| `affinity_shard_fire` | `item/affinity_shard_fire.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_shard_water` | `item/affinity_shard_water.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_shard_wind` | `item/affinity_shard_wind.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_shard_earth` | `item/affinity_shard_earth.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | ELEMENTAL tier. |
| `affinity_shard_lightning` | `item/affinity_shard_lightning.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Fire). |
| `affinity_shard_ice` | `item/affinity_shard_ice.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Water). |
| `affinity_shard_sound` | `item/affinity_shard_sound.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Wind). |
| `affinity_shard_gravity` | `item/affinity_shard_gravity.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | DEVIANT tier (←Earth). |

## Textures — Item (Dimension Staff)

Hand-authored multi-layer item, 4 texture parts.

| ID | Path | Type | Status | Resolution | Palette | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|
| `dimension_staff.crystal_big` | `item/dimension_staff/texture_crystal_big.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | Big crystal element on the staff head. |
| `dimension_staff.crystal_small` | `item/dimension_staff/texture_crystal_small.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | Small crystal element on the staff head. |
| `dimension_staff.deco` | `item/dimension_staff/texture_deco.png` | item | DONE | 32×32 | _TBD_ | _TBD_ | Phase 0 | Decorative band on the staff head. |
| `dimension_staff.handle` | `item/dimension_staff/texture_handle.png` | item | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | Wooden / metallic handle. |

## Textures — Entity (Portal)

| ID | Path | Type | Status | Resolution | Palette | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|
| `portal_entity_elemental` | `entity/portal/portal_entity_elemental.png` | entity | DONE | 256×256 | _TBD_ | _TBD_ | Phase 0 | Portal variant for ELEMENTAL-tier affinity. |
| `portal_entity_deviant` | `entity/portal/portal_entity_deviant.png` | entity | DONE | 256×256 | _TBD_ | _TBD_ | Phase 0 | Portal variant for DEVIANT-tier affinity. |
| `portal_entity_eternal` | `entity/portal/portal_entity_eternal.png` | entity | DONE | 256×256 | _TBD_ | _TBD_ | Phase 0 | Portal variant for ETERNAL-tier affinity. |
| `portal_entity_school` | `entity/portal/portal_entity_school.png` | entity | DONE | 256×256 | _TBD_ | _TBD_ | Phase 0 | Portal variant for the School dimension (post-Dragon-kill spawn). |

## Textures — GUI

| ID | Path | Type | Status | Resolution | Palette | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|
| `affinity_book` | `gui/affinity_book.png` | gui | DONE | 256×256 | _TBD_ | _TBD_ | Phase 0 | Affinity book overlay background. |
| `affinity_book.blank_button` | `gui/sprites/affinity_book/blank_button.png` | gui | DONE | 20×18 | _TBD_ | _TBD_ | Phase 0 | Unselected blank tab button in the affinity book. |
| `affinity_book.blank_button_highlighted` | `gui/sprites/affinity_book/blank_button_highlighted.png` | gui | DONE | 20×18 | _TBD_ | _TBD_ | Phase 0 | Hovered / selected blank tab button in the affinity book. |
| `advancements.elementalrealms_background` | `gui/advancements/backgrounds/elementalrealms.png` | gui | DONE | 16×16 | _TBD_ | _TBD_ | Phase 0 | Advancements-tab background override (16×16 — non-standard; the typical vanilla background is 256×256, so this may be a placeholder or an intentionally tileable texture). |