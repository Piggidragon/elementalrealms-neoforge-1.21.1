# Asset Tracker — Sounds

> Skeleton + initial inventory of all sounds the mod ships.

## How to use this file

Every sound the mod plays is listed here with source, license, and where it's wired. Sounds are either sourced (CC0/free online) or DIY (recorded / synthesized by Piggidragon). Update this file **before** writing the sounds spec in an issue — the spec references entries from this file.

### Column legend

| Column | Meaning |
|---|---|
| ID | Short identifier used in code (e.g. `dragon_laser_charge`). |
| Path | Runtime path under `assets/elementalrealms/sounds/`. |
| Source | `CC0-source:<url>` / `DIY` / `mixed` (CC0 source + DIY processing). |
| License | `CC0` / `CC-BY` / `CC-BY-SA` / `DIY`. Must match the actual file in the repo. |
| Duration | `Xs` — target length; loopable sounds note loop-seam here. |
| Peak loudness | Target peak in dBFS (e.g. `-3 dBFS`). Avoid clipping in-game. |
| Loop seamless | `yes` / `no`. Loopable sounds must be marked + crossfade verified. |
| Used at | Code call site: class + method (e.g. `EnderDragonMixin#tryFireLaser`). |
| Phase | Phase the sound ships with. |
| Notes | Free text — DIY recipe, processing chain, "must not sound like Minecraft vanilla X". |

### License rule

Every sound must have a license. CC-BY / CC-BY-SA files require attribution in `assets/elementalrealms/sounds.json` (the `comment` field of the event definition). DIY sounds need no attribution. CC0 still needs to be marked so future audits can prove provenance.

## Sounds

| ID | Path | Source | License | Duration | Peak loudness | Loop seamless | Used at | Phase | Notes |
|---|---|---|---|---|---|---|---|---|---|
| `laser_beam` | `sounds/laser_beam.ogg` | _TBD_ | _TBD_ | _TBD_ (Ogg Vorbis, mono, 44100 Hz, ~96 kbps — exact duration needs manual inspection in an audio editor) | _TBD_ | no | `EnderDragonMixin` (PR #9 — Ender Dragon laser attack) | Phase 0 | Source file shipped with the laser-beam feature (PR #9, merged into `main` on 2025-11-22). License and source URL not yet documented — needs an audit pass before this can be treated as a clean asset. |