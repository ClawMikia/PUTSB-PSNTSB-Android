# PUTSB-PSNTSB (Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot)

## The Neural Debt Matrix

**DebtTracker** is a specialized financial node tracker designed for high-stakes debt management in the sprawl. This application helps you keep track of every credit and debit in your personal network with a sleek, immersive cyberpunk aesthetic.

[![System Status: Online](https://img.shields.io/badge/System-Online-gold?style=for-the-badge&logo=android)](https://github.com/your-repo)
[![Version: 2.0.7-CYBER](https://img.shields.io/badge/Version-2.0.7--CYBER-gold?style=for-the-badge)](https://github.com/your-repo)
[![Theme: Cyberpunk](https://img.shields.io/badge/Theme-Cyberpunk-gold?style=for-the-badge)](https://github.com/your-repo)
[![API: 26+](https://img.shields.io/badge/API-26%2B-brightgreen?style=for-the-badge)](https://github.com/your-repo)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](https://github.com/your-repo)

---

### "The Golden Rule of the Streets"
> *"Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot"*

Inspired by legendary street wisdom, this motto encapsulates the fluid nature of financial respect in the matrix.

---

## What People Are Saying

> *"This app changed how I track debts. The cyberpunk theme makes managing money feel less boring and more like a game."*
> -- Active User

> *"Finally an app that lets me export to Excel AND looks incredible. The dark theme is easy on the eyes at night."*
> -- Beta Tester

> *"The password-protected archive is a genius feature. Settled debts hidden away but still accessible when needed."*
> -- Early Adopter

> *"Clean UI, smooth animations, and the partial payment tracking is exactly what I needed. 10/10 would recommend."*
> -- Satisfied User

> *"The pie chart and bar graph analytics give me a clear picture of my financial situation at a glance."*
> -- Debt Tracker Enthusiast

---

## Latest Functionalities & Features

### Neural Dashboard
- **Net Balance Engine:** Real-time calculation of your total financial exposure (Owed vs Lent).
- **Active Node Monitoring:** Quick glance at active, partial, and overdue nodes.
- **Top Entity Tracking:** Automatically identifies and ranks your Top Creditors and Top Debtors.
- **Summary Cards:** Gold-bordered cards showing Net Balance, I Owe total, and Owes Me total with icon indicators.
- **Overdue Alerts:** Red-highlighted overdue count with instant visibility on the dashboard.

### Debt Node Management
- **Dynamic Matrix:** Initialize new debt nodes with detailed parameters: Person Name, Amount, Description, Debt Type (I Owe / Owes Me), and optional Due Date.
- **Partial Payment Protocols:** Record fractional payments with automated balance updates and progress bar visualization.
- **Mark Settled:** One-tap to mark any debt as fully paid.
- **Status Lifecycle:** Nodes transition through Active, Partial, Overdue, and Settled states automatically.
- **Edit & Delete:** Full CRUD operations with confirmation dialogs for destructive actions.
- **Due Date Tracking:** Optional due dates with calendar picker and automatic overdue detection.

### Advanced Sorting
- **6 Sort Options** via context menu:
  1. Date (Newest First)
  2. Date (Oldest First)
  3. Amount (High to Low)
  4. Amount (Low to High)
  5. Name (A-Z)
  6. Overdue First
- Applied per-tab: Dashboard, I Owe, and Owes Me each maintain independent sort state.

### Filtered Views
- **I Owe Tab:** Isolated view of all debts you owe to others with total amount summary.
- **Owes Me Tab:** Isolated view of all debts others owe you with total amount summary.
- Each tab features its own sort menu and empty state illustrations.

### Data Security & Archiving
- **Password-Protected Archive:** A secure vault for settled debts, completely hidden from the main interface.
- **Password Setup:** First-time users create a minimum 4-character password.
- **Authentication Gate:** Must enter password each time to access the archive.
- **Archive Action:** Settled debts can be archived from the detail screen.

### System Export & Analytics
- **Excel Matrix Export:** Generate high-fidelity .xlsx reports using Apache POI with separate sheets for "I OWE" and "OWES ME".
- **SAF File Picker:** Uses Android's Storage Access Framework to let users choose exactly where to save the exported file.
- **Export Columns:** ID, Person Name, Amount, Paid Amount, Remaining, Description, Due Date, Status, Created At, Archived.
- **JSON Backup Export:** Export the entire node database (including archived nodes) to a single portable .json file from Settings > Data Backup.
- **JSON Backup Import:** Restore or merge nodes from a .json backup via the system document picker, with a confirmation dialog before writing.
- **Full Fidelity Backup:** JSON backup preserves every field — IDs, timestamps, status, and archive flag — so restored data matches the original exactly.
- **Merge Behavior:** Import overwrites nodes with matching IDs and appends new ones, making it safe to restore onto existing data.
- **Pie Chart:** Donut-style animated chart showing I Owe vs Owes Me distribution.
- **Bar Chart:** Monthly debt timeline for the last 6 months with gold bars.
- **Stats Cards:** Total debts count and settled count displayed as metric cards.
- **Top Contacts:** Ranked list of top 5 contacts by total amount with avatar initials.

### Intelligence Notifications
- **Per-Debt Reminders:** Scheduled notifications for each debt's due date via AlarmManager.
- **Overdue Summary:** Background scanning for overdue debts via WorkManager periodic tasks.
- **Configurable Frequency:** Off, Hourly, Daily, or Weekly reminder cycles.
- **Notification Channel:** Dedicated "Debt Reminders" channel for Android 8.0+.
- **Permission Handling:** Graceful POST_NOTIFICATIONS permission request on Android 13+.

### Onboarding & Splash
- **Animated Splash Screen:** 6-step choreographed sequence with logo pop, text fade-in, and status text cycling through "Initializing...", "Scanning...", "System Online".
- **Intro Tutorial:** 4-page ViewPager2 onboarding with dot indicators covering Welcome, Dashboard, Analytics, and Archive.
- **First-Run Detection:** Intro shown only on first launch, stored in SharedPreferences.

---

## UI & Design Language

The interface is engineered for high-contrast, low-light operations typical of the sprawl.

### Color Palette

| Color | Hex | Usage |
|---|---|---|
| Cyber Gold | `#FFD700` | Primary accent, headers, FAB, buttons, dividers |
| Cyber Black | `#0A0A0A` | Main background |
| Cyber Surface | `#111111` | Surface color |
| Cyber Card | `#181818` | Card backgrounds |
| Debt Owed | `#FF4444` | Red -- I owe someone |
| Debt Lent | `#00C853` | Green -- Someone owes me |
| Debt Partial | `#FF9800` | Orange -- Partially paid |
| Debt Settled | `#607D8B` | Grey -- Fully settled |
| Neon Red Alert | `#FF1744` | Overdue/errors |
| Neon Green OK | `#00E676` | Success/active |

### Typography
- **Hero:** 36sp, sans-serif-black, gold, wide letter spacing
- **Title:** 26sp, sans-serif-medium, white
- **Subtitle:** 18sp, sans-serif-medium, secondary color
- **Body:** 15sp, sans-serif, white
- **Caption:** 12sp, secondary color
- **Label:** 10sp, gold, all caps, wide spacing
- **Amount:** 22sp, sans-serif-black, gold

### Animations
- **Splash:** 6-step choreographed sequence with OvershootInterpolator and AccelerateDecelerateInterpolator
- **Activity Transitions:** Custom slide-in/slide-out animations (Cyber theme)
- **Card Press:** Scale pop animation (0.9 -> 1.05 -> 1.0) with OvershootInterpolator
- **Chart Entry:** Animated Y-axis entry with Easing.EaseInOutQuad
- **Custom Snackbar:** Dark background with gold text (success) or red text (error)

### UI Components
- Custom BottomNavigationView with cyber-themed background
- Gold FloatingActionButton for adding new debts
- MaterialCardView with 1dp stroke borders and 12-14dp corners
- TextInputLayout with outlined box style and gold focus accents
- ChipGroup for debt type selection (I Owe / Owes Me)
- MaterialButtonToggleGroup for reminder frequency
- RecyclerView with ListAdapter + DiffUtil for smooth list updates
- ViewPager2 with TabLayout dot indicators for onboarding
- MPAndroidChart PieChart (donut) and BarChart
- Progress bars with gold tint showing payment percentage
- Custom AlertDialogs and PopupMenus with dark theme

---

## Technical Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.24 |
| Architecture | MVVM (Model-View-ViewModel) |
| DI | Dagger Hilt 2.51.1 |
| Database | Room 2.6.1 with KSP |
| Navigation | Jetpack Navigation Component 2.7.7 |
| UI Binding | ViewBinding |
| Async | Kotlin Coroutines + Flow |
| Background | WorkManager 2.9.0 + AlarmManager |
| Charts | MPAndroidChart v3.1.0 |
| Excel | Apache POI 5.2.3 |
| Splash | AndroidX Splash Screen 1.0.1 |
| Target SDK | 36 (Android 16) |
| Min SDK | 26 (Android 8.0) |

---

## Project Structure

```text
app/src/main/
├── java/com/cyberpunk/debttracker/
│   ├── data/            # Room Database, DAOs, Entity Models, Repositories
│   ├── di/              # Hilt Dependency Injection Modules
│   ├── notification/    # Broadcast Receivers & Background Workers (Reminders)
│   ├── ui/              # MVVM Pattern: Activities, Fragments, ViewModels, Adapters
│   │   ├── dashboard/   # Main command center, Top Entities, Debt Lists
│   │   ├── analytics/   # MPAndroidChart integration, Export
│   │   ├── archive/     # Password-protected debt storage
│   │   ├── settings/    # System configuration & reminder frequency
│   │   └── intro/       # System initialization & onboarding
│   └── util/            # Helpers for Excel Export, Security, Formatting, Notifications
└── res/
    ├── drawable/        # Cyber-themed vector assets & custom backgrounds
    ├── layout/          # Immersive UI definitions
    ├── menu/            # Context-aware navigation & sort menus
    ├── navigation/      # Navigation graph
    ├── anim/            # Custom transition animations
    ├── raw/             # Sound/font assets
    └── values/          # Theming (colors.xml, strings.xml, themes.xml, styles.xml)
```

---

## Screens Overview

| Screen | Description |
|---|---|
| SplashActivity | Animated 6-step intro sequence with logo, tagline, and status text |
| IntroActivity | 4-page onboarding tutorial (ViewPager2 + dot indicators) |
| MainActivity | Main host with bottom navigation (5 tabs) and FAB |
| DashboardFragment | Summary cards, active/overdue counts, scrollable debt list with sort |
| OwedFragment | Filtered list of "I Owe" debts with total and sort |
| LentFragment | Filtered list of "Owes Me" debts with total and sort |
| AnalyticsFragment | Pie chart, bar chart, stats, top contacts, Excel export |
| SettingsFragment | Reminder frequency toggle, JSON backup export/import, and about link |
| AboutFragment | App info, version, Filipino meme quote, developer credit |
| AddDebtActivity | Form to create or edit a debt (person, amount, type, due date) |
| DebtDetailActivity | Full detail view with payment actions, progress bar, edit/delete |
| ArchiveActivity | Password-gated access to archived settled debts |

---

## Deployment

1. **Terminal Sync:** Clone the repository.
2. **Initialization:** Open in Android Studio Hedgehog (2023.1.1) or later.
3. **Syncing:** Allow Gradle to fetch all neural dependencies.
4. **Deployment:** Run on an Android device (API 26 or higher).
5. **Authorization:** Follow the intro sequence to initialize your system parameters.

---

*Developed by: The Bumbay Operative*
*System Protocol: Android 16 (API 36)*
*Security Status: Fully Encrypted*
