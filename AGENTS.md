# AGENTS.md

> **For AI coding agents (Hermes, GitHub Copilot, Cursor, Claude Code, etc.) working in this repo.**
>
> This is the canonical "what's in this repo" doc. Auto-picked up by most AI tools when present at the repo root. If you're an AI agent reading this: read the whole file before doing anything substantive.
>
> **Human conventions for this file:**
> - Update when code lands, new branches get cut, or the build state changes meaningfully.
> - Keep it terse. Tables over prose.
> - This is operating notes, not marketing — for what the mod is and how to install it, see `README.md` or `docs/PLANS.md`.

## Repo metadata

- **GitHub:** `Piggidragon/elementalrealms-neoforge-1.21.1`
- **Local path:** `/home/piggidragon/Coding/elementalrealms-neoforge-1.21.1`
- **Minecraft:** 1.21.1
- **Loader:** NeoForge
- **Build system:** Gradle (NeoGradle)
- **Java version:** check `build.gradle`

## Sibling repo

`Piggidragon/dragonsrequiem-neoforge-1.21.1` — separate. All vanilla-side boss reworks, enchantment balance, structure tweaks go there. See `elementalrealms-codebase` skill §Scope split.

## Branches

| Branch | Purpose |
|---|---|
| `main` | Stable. Protected, no direct push. Only via PR. |
| `dev` | Integration. Direct push OK for mini-fixes. |
| feature branches | Off `dev`. PR into `dev`. |

`main` ← `dev` only on release.

## Issue-First (workflow rule)

Every larger unit of work starts as a GitHub issue BEFORE code. Branch name = issue title, kebab-case. PR stays Draft until user says "fine" / "good" / "ready". No milestones, conventional commits.

Full workflow details: see the `elementalrealms-workflow` skill (auto-loaded when working in this repo).

## Code state (Phase 0, PR #9 merged)

| File | Role | Status |
|---|---|---|
| `src/main/resources/elementalrealms.mixins.json` | Mixin registration. | Active (empty mixins list — vanilla-rework mixins moved to `dragonsrequiem-neoforge-1.21.1` per #52). |
| `LICENSE` (repo root) | LGPL-3.0 license for our code. | Active. |
| `gradle.properties` → `mod_license=LGPL-3.0` | License field for mod metadata. | Active. |

## What's NOT in the codebase yet

- Affinity system (3-stage roll)
- 12 affinity shards + 1 void stone items
- Crystal Orb of Awakening
- School dimension + content
- 11 pocket dimensions
- 11 boss entities
- Custom mobs (affinity mobs + modded mobs)
- Spell API + 12 mage samples
- Mana system
- Affinity book GUI
- Breeze variant / Trial Vault Big-Shard add (structure quick-wins, both deferred to dragons_requiem)
- Anti-cheese measures on dragon (deferred to dragons_requiem)

## Docs in the repo

| Doc | Purpose |
|---|---|
| `docs/PLANS.md` | Master plan, features + build phases |
| `docs/IDEAS.md` | Scratchpad, brainstorming, open questions |
| `docs/ASSET-MODELS.md` / `docs/ASSET-TEXTURES.md` / `docs/ASSET-SOUNDS.md` | Asset pipeline |

Repo constraints / naming / iteration style live in **skills**, not docs — they're auto-loaded when needed:
- `elementalrealms-codebase` — hard NO-lists (Lodestone scope, lore-safety, config-first, scope split, locked-in design decisions)
- `elementalrealms-naming` — Java conventions, ResourceLocation IDs, display names, folder layout
- `elementalrealms-workflow` — issue-first Git workflow, branches, pre-PR checklist
- `user-piggy-style` — how the user iterates, what they prefer/don't prefer, "merge ich" trigger, "strukturierte angehen" pattern

## Active GitHub Issues

Use `gh issue list --repo Piggidragon/elementalrealms-neoforge-1.21.1 --state open` or GitHub MCP. No Projects V2 access via API — add to project board manually via UI.

Notable open issues:
- **#52** — Move vanilla-dragon laser code (EnderDragonMixin, DragonConfig, laser packets, laser sound) to dragonsrequiem-neoforge-1.21.1

## Recently merged

- PR #9 — Stationary-player laser attack on Ender Dragon. (Code moved to `dragonsrequiem-neoforge-1.21.1` per #52.)

## Last updated

2026-06-27 — created as `AGENTS.md` (was `docs/REPO-CONTEXT.md`). When code changes, update the relevant section.

**Recent housekeeping** (2026-06-27): moved REPO-CONTEXT → AGENTS.md (canonical AI-tool pattern), removed `docs/PROJECT-CONSTRAINTS.md` / `docs/ITERATION-STYLE.md` / `docs/NAMING-CONVENTIONS.md` (now in skills: `elementalrealms-codebase` / `user-piggy-style` / `elementalrealms-naming`), switched license from "All Rights Reserved" to **LGPL-3.0** (see `LICENSE`).