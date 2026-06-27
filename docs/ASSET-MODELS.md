# Asset Tracker — Models (3D)

> Skeleton + initial inventory of all hand-authored 3D models the mod ships.
> **Datagen-generated item models (Affinity Stones, Affinity Shards) are NOT tracked here** — they are derived from the Affinity registry at build time and never authored in Blockbench.

## How to use this file

Every 3D model the mod ships is listed here with status, source, and specs. Update this file **before** requesting a model from Blockbench — the spec is the contract between the spec author and the modeller.

### Column legend

| Column | Meaning |
|---|---|
| ID | Short identifier used in code (e.g. `dimension_staff`). |
| Name | Display name (lang key + Blockbench project name). |
| Type | `item` / `block` / `entity` / `projectile` / `structure-decor`. |
| Status | `TODO` / `IN PROGRESS` / `REVIEW` / `DONE`. |
| Dimensions | Blockbench bounding box in pixels (1 px = 1/16 block for items/blocks; entity units otherwise). |
| Bones | Top-level bone hierarchy summary. |
| Animations | Comma-separated animation names. `static` if none. |
| Mood-board | Link to inspiration references. |
| Phase | Phase the asset ships with (e.g. `Phase 1 — Dragon`). |
| Notes | Free text — attachment points, render quirks, "must match palette X". |

### Status workflow

`TODO → IN PROGRESS → REVIEW → DONE`. A model only flips to `DONE` after the in-game render matches the spec.

### Datagen vs hand-authored

This tracker covers hand-authored models only. The 20 Affinity Stone / Affinity Shard item models (12 stones, 8 shards) live in `src/generated/resources/assets/elementalrealms/models/item/` and are emitted by the datagen provider from `AffinityItems`. They have no Blockbench source. When we eventually ship Blockbench-authored models for the stones (e.g. per-affinity hero models), they enter this tracker as new rows.

## Models

| ID | Name | Type | Status | Dimensions | Bones | Animations | Mood-board | Phase | Notes |
|---|---|---|---|---|---|---|---|---|---|
| `dimension_staff` | Dimension Staff | item | DONE | 16×16 (item), 4 textures (crystal_big, crystal_small, deco, handle) | flat item model, 4 layer textures | static | _TBD_ | Phase 0 | Hand-authored in `models/item/dimension_staff.json` + `items/dimension_staff.json`. Textures in `textures/item/dimension_staff/`. Used by SchoolDimension access path. |