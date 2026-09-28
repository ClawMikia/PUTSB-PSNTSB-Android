# PUTSB-PSNTSB — The Neural Debt Matrix

> *"Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot"*

A cyberpunk-themed debt tracker for Android that treats who-owes-whom as a
living system: contacts become NPCs, repayments move a shared Patience gauge,
and a full gamification layer (100 achievements, 137 rotating quests, procedural
icon art) sits on top of the ledger.

- **Package:** `com.cyberpunk.debttracker`
- **Version:** 1.0.0 (versionCode 1)
- **Min SDK:** 26 (Android 8.0) · **Target / Compile SDK:** 36 (Android 16)
- **Language:** Kotlin 1.9.24 · **Build:** Gradle 8.11.1 (Kotlin DSL)

---

## Table of Contents

1. [Features](#features)
2. [Gamification System](#gamification-system)
3. [Data & Storage](#data--storage)
4. [Architecture](#architecture)
5. [Tech Stack](#tech-stack)
6. [Project Structure](#project-structure)
7. [Screens](#screens)
8. [Design Language](#design-language)
9. [System Bar / Edge-to-Edge Handling](#system-bar--edge-to-edge-handling)
10. [Build & Run](#build--run)
11. [Testing](#testing)
12. [Known Issues](#known-issues)

---

## Features

### Debt Ledger
- Create nodes with person, amount, description, type (**I Owe** / **Owes Me**), optional due date.
- Partial payments with live remaining-balance and progress bar.
- One-tap **Mark Settled**, plus edit and delete with confirmation.
- Automatic status lifecycle: `ACTIVE → PARTIAL → OVERDUE → SETTLED`.
- 6 sort modes (date asc/desc, amount asc/desc, name, overdue-first) with **independent state per tab**.
- Net balance = total owed-to-you minus total you-owe, computed reactively.

### Archive
- Password-gated vault for settled nodes, hidden from the main ledger.
- Minimum 4-character password, hashed and stored locally.
- Re-prompted on every entry to the archive.

### Backup & Export
- **JSON backup** export/import covering every field (id, timestamps, status, archive flag).
- Import **merges** by id — safe to restore onto existing data.
- **Excel (.xlsx)** export via Apache POI, split into `I OWE` and `OWES ME` sheets.
- Uses the Storage Access Framework, so the user picks the destination.

### Analytics
- Animated donut chart (I Owe vs Owes Me).
- 6-month bar timeline of debt volume.
- Total / settled metric cards and a top-5 contacts ranking.

### Notifications
- Per-debt due-date reminders via `AlarmManager`.
- Overdue sweep via periodic `WorkManager` jobs.
- Frequency: Off / Hourly / Daily / Weekly.
- Android 13+ `POST_NOTIFICATIONS` requested gracefully.

---

## Gamification System

All gamification state lives in a **separate Room database** (`gamification_db`)
that is deliberately **excluded from backup** so a campaign always starts fresh on
a new device. Debt data (`debt_tracker_db`) *is* backed up.

### Player progression
- **Level curve** with a strictly increasing XP requirement per level.
- **Rank titles** that shift with level (e.g. `FRESH IN THE MATRIX` → `ROOKIE NODE`).
- Three tracked meters: **XP**, **₱ Coins** (spent on NPC actions), and **Nerve**
  (0–100, capped). Nerve is a net resource: 44 reward definitions grant it and 31
  penalties drain it.
- **Daily streak** with rollover handling for missed days.

### Achievements — 100 total
Defined in `game/AchievementCatalog.kt`, each with a code, title, blurb, target
`Stat`, and XP/coin/nerve payout. Evaluated against live stat counters.

### Quests — 137 total, on three cycles
| Cycle | Count | Resets |
|---|---|---|
| Daily | 7 | Midnight, local time |
| Monthly | 30 | 1st of the month |
| Annual | 100 | 1st of January |

Cycle keys are derived from the device's local time zone via
`Cycles.dailyKey/monthlyKey/annualKey` in `game/LevelCurve.kt`.

### Rewards & penalties
- Reward definitions pay out **XP, coins, and nerve**; penalties drain them.
- 33 penalty entries cover overdue debts, negative net balance, coin bankruptcy,
  excessive NPC anger, and idle-creditor neglect.

### NPCs
- Every contact that appears in the debt table is promoted to an NPC.
- One of **16 archetypes** is deterministically assigned from the person's name
  (`NpcArchetype.forPerson`), so a given contact always keeps the same identity.
- Per-NPC state: `patience` (0…max), `relation` (−100…100), `level`, `xp`, `rages`,
  and a `mood` derived from open/overdue/settled counts.
- `GameEngine.syncNpcs()` rebuilds aggregates from the live debt table on every
  mutation, so the roster can never drift from the ledger.

#### NPC actions
| Action | Cost | Effect | Rule |
|---|---|---|---|
| **NUDGE** | free | +1 relation | Once per contact per day |
| **PACIFY** | 50 coins | +25 patience, +10 relation | Requires sufficient balance |
| **CONFRONT** | free | −12 patience, −6 relation | Applies the `NPC_CONFRONT` penalty |

Buttons surface their own state: NUDGE greys out and relabels to `NUDGED` once
used for the day, and PACIFY dims with a `✕` on its price when unaffordable,
rather than failing silently on tap.

### Procedural iconography
No image assets. `game/IconForge.kt` generates every icon at runtime from a
stable string seed:

- **Teddy bears** for rewards, achievements, and quests — `BearDesign` varies head
  and ear shape, eye and nose style, muzzle, fur palette, proportions, and accessories.
- **White skulls** for penalties — `SkullDesign` varies cranium shape, eye-socket
  geometry, jaw, teeth count, cracks, and bone tinting.
- Drawables are memoised in a 512-entry `LruCache` keyed by type + seed.

`IconUniquenessTest` fingerprints every generated design and asserts no two
rewards share a bear and no two penalties share a skull.

### Campaign purge
`GameEngine.purgeCampaign()` wipes the gamification database for a true
fresh-install reset **without** touching the debt ledger. Reachable from the
`PURGE` button in the Game Hub toolbar, behind a confirmation dialog.

---

## Data & Storage

Two independent Room databases, both provided through Hilt in `di/DatabaseModule.kt`:

| Database | Contents | Backup |
|---|---|---|
| `debt_tracker_db` | Debt nodes, archive flags | **Included** |
| `gamification_db` | Profile, rollover, achievements, quests, stats, NPCs, log | **Excluded** |

`android:allowBackup="true"` is kept, with the gamification database (plus its
`-wal`, `-shm`, and `-journal` sidecars) explicitly excluded in both
`res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml`. The result:
cloud backup and device-to-device transfer preserve real debts but never carry
campaign progress across a reinstall.

---

## Architecture

MVVM with a repository layer and unidirectional data flow.

```
UI (Activity/Fragment, ViewBinding)
  └── ViewModel  — StateFlow/LiveData, no Android framework deps in logic
        └── Repository — single source of truth, suspend + Flow APIs
              ├── DebtDao / DebtDatabase
              └── GameEngine → GameDao / GameDatabase
```

- **Hilt** for DI; all activities and fragments are `@AndroidEntryPoint`.
- **Coroutines + Flow** throughout; Room queries return `Flow` and the UI collects
  with `repeatOnLifecycle(STARTED)`.
- **GameEngine** is injected directly into `DebtRepository` so every ledger
  mutation (insert, update, delete, archive, import, payment, settle) automatically
  drives the gamification layer. This keeps the two systems from drifting apart.

`DebtTrackerApp` performs a cold-start `GameEngine.bootstrap()` to roll the day
over, refresh derived stats, and re-sync NPCs before any screen appears.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.24 |
| Architecture | MVVM + Repository |
| DI | Dagger Hilt 2.51.1 (KSP) |
| Database | Room 2.6.1 (KSP) — two databases |
| Navigation | Navigation Component 2.7.7 |
| UI | ViewBinding, Material Components 1.12.0 |
| Async | Coroutines 1.7.3 + Flow |
| Background | WorkManager 2.9.0, hilt-work 1.2.0, AlarmManager |
| Charts | MPAndroidChart v3.1.0 |
| Excel | Apache POI 5.2.3 |
| Splash | core-splashscreen 1.0.1 |
| Preferences | androidx.preference 1.2.1 |
| Tests | JUnit 4.13.2 |

---

## Project Structure

```text
app/src/main/java/com/cyberpunk/debttracker/
├── data/
│   ├── db/            # DebtDatabase, DebtDao, GameDatabase, GameDao, Converters
│   ├── model/         # Debt entity
│   │   └── game/      # GameModels — entities + enums
│   └── repository/    # DebtRepository (bridge to GameEngine)
├── di/                # DatabaseModule — Hilt providers
├── game/              # Gamification domain (framework-free)
│   ├── GameEngine.kt      # Orchestration, events, rollover, evaluation
│   ├── LevelCurve.kt      # Level math, QuestCycle, Cycles
│   ├── Stat.kt            # Stat keys + gauge groups
│   ├── RewardCatalog.kt   # Reward & penalty definitions
│   ├── AchievementCatalog.kt
│   ├── QuestCatalog.kt
│   ├── NpcArchetype.kt    # 16 archetypes
│   ├── IconForge.kt       # Seeded RNG + drawable cache
│   ├── BearIcon.kt        # Procedural teddy bear
│   └── SkullIcon.kt       # Procedural skull
├── notification/      # AlarmManager receivers, WorkManager jobs
├── ui/
│   ├── dashboard/     # MainActivity, Dashboard/Owed/Lent/Analytics/Archive
│   ├── adddebt/       # AddDebtActivity
│   ├── debtdetail/    # DebtDetailActivity
│   ├── game/          # GameHubActivity, adapters, EventBanner
│   ├── settings/      # SettingsFragment
│   ├── about/         # AboutFragment
│   ├── intro/         # IntroActivity
│   └── splash/        # SplashActivity
└── util/              # Formatting, Excel, JSON backup, security, insets
```

The `game/` package has **no Android UI imports** — catalogs, level math, and icon
*design* generation are pure Kotlin, which is what makes them unit-testable on the JVM.

---

## Screens

| Screen | Purpose |
|---|---|
| `SplashActivity` | Animated 6-step init sequence; routes to intro or main |
| `IntroActivity` | 4-page first-run tutorial (ViewPager2) |
| `MainActivity` | Host for 5 bottom-nav tabs + FAB; owns the bottom-nav inset logic |
| `DashboardFragment` | Net balance cards, top entities, active list, Game Hub entry card |
| `OwedFragment` / `LentFragment` | Filtered per-direction lists with independent sorting |
| `AnalyticsFragment` | Charts, stats, top contacts, Excel export |
| `ArchiveActivity` | Password-gated settled-debt vault |
| `AddDebtActivity` | Create/edit form |
| `DebtDetailActivity` | Detail view, partial payments, settle/edit/delete |
| `GameHubActivity` | 6 tabs: Daily / Monthly / Annual quests, Feats, Contacts, Log |
| `SettingsFragment` | Reminder frequency, JSON backup, campaign purge, about |
| `AboutFragment` | Version and credits |

---

## Design Language

### Palette

| Token | Hex | Use |
|---|---|---|
| Cyber Gold | `#FFD700` | Primary accent, headers, FAB, dividers |
| Cyber Black | `#0A0A0A` | Background |
| Cyber Surface | `#111111` | Surfaces |
| Neon Amber | `#FF9800` | Partial / warning |
| Neon Red Alert | `#FF1744` | Overdue, errors, `CONFRONT` |
| Neon Green OK | `#00E676` | Success, settled-positive |
| Debt Owed | `#FF4444` | I Owe |
| Debt Lent | `#00C853` | Owes Me |

### Typography
- Hero `36sp` sans-serif-black · Title `26sp` medium · Body `15sp`
- Labels `10sp` all-caps with wide letter spacing (`.08`)

### Motion
- Splash: 6-step choreographed sequence with overshoot easing
- Card press: scale pop `0.9 → 1.05 → 1.0`
- Chart entry: animated Y-axis
- Event banner: slide-in toast for rewards/penalties/achievements

---

## System Bar / Edge-to-Edge Handling

`targetSdk 36` means **Android 15+ enforces edge-to-edge** — the system bars draw
*over* the app window, so every screen must inset its own content or toolbars and
buttons end up underneath the status and navigation bars.

Handled in `util/WindowInsetsExt.kt`:

- `enableCyberEdgeToEdge()` — opts in on **every** API level (not just where the
  platform forces it) so spacing is identical across devices. Reuses the existing
  `status_bar_color` / `nav_bar_color` as the pre-API-29 scrim.
- `applySystemBarInsets()` — pads a view by `systemBars + displayCutout` insets,
  capturing the layout's original padding first so it composes with declared padding.
- `applySystemBarAndImeInsets()` — same, but also lifts above the soft keyboard
  (used by `AddDebtActivity`).
- `padBottomByNavBar()` / `marginBottomByNavBar()` — for hosts that paint their
  own background edge-to-edge.

Applied in all seven activities before `setContentView`. `MainActivity` grows the
bottom navigation bar by the nav-bar inset and pushes the nav host and FAB up by
the same amount. The six nav fragments need no changes: they live inside
`MainActivity`'s nav host, which is already inset.

---

## Build & Run

```bash
# Clone
git clone <repo-url> && cd PUTSB-PSNTSB-Android

# Build
./gradlew :app:assembleDebug

# Compile only (fast type check)
./gradlew :app:compileDebugKotlin

# Unit tests
./gradlew :app:testDebugUnitTest
```

Requires JDK 17 (Android Studio's bundled JBR works) and the Android SDK with
platform 36. `local.properties` must point at your SDK — it is git-ignored by design.

> **Note:** this checkout currently has no committed Gradle wrapper JAR or
> `gradlew` scripts. Use Android Studio, or invoke a local Gradle 8.11.1
> distribution directly.

---

## Testing

`app/src/test/java/com/cyberpunk/debttracker/game/`

- **`GameCatalogTest`** — asserts exactly 100 achievements, 7/30/100 quests, unique
  codes across every catalog, no reward/penalty code collisions, 16 unique
  archetypes, and the settlement/late-day reward brackets.
- **`LevelCurveTest`** — level-curve monotonicity, boundary XP, multi-level jumps,
  max-level cap, rank titles, and quest cycle key formatting.
- **`IconUniquenessTest`** — fingerprints every generated bear and skull design and
  asserts all rewards/achievements/quests/penalties render distinctly.

---

## Known Issues

- `gradlew` / `gradle-wrapper.jar` are not committed. See Build & Run above.
- `DebtDatabase` emits a Room warning about a missing schema export directory.
- `PACIFY` costs 50 coins and new profiles start at 0, so it is unreachable until
  the first rewards land. The button now shows this state explicitly.
- `nudgesToday` is incremented but never reset, so it is a lifetime counter
  rather than a daily one despite the name.

---

## License

MIT
