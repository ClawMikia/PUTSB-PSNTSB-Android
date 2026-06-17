# PUTSB-PSNTSB (Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot)

## The Neural Debt Matrix

A specialized financial node tracker designed for high-stakes debt management in the sprawl. This application helps you keep track of every credit and debit in your personal network with a sleek cyberpunk aesthetic.

[![System Status: Online](https://img.shields.io/badge/System-Online-gold?style=for-the-badge&logo=android)](https://github.com/your-repo)
[![Version: 1.0.0](https://img.shields.io/badge/Version-1.0.0-gold?style=for-the-badge)](https://github.com/your-repo)

### "The Golden Rule of the Streets"
> *"Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot"*

This quote, inspired by the legendary street wisdom, encapsulates the fluid nature of financial respect in the matrix.

### Key Modules
- **Neural Dashboard:** Real-time visualization of your total exposure and receivables.
- **Node Analytics:** Data-driven insights via **MPAndroidChart** to visualize your financial standing across the network.
- **Credit/Debit Matrix:** Initialize, modify, and settle debt nodes with precision.
- **System Settings:** Configure frequency reminders and system parameters to keep your nodes in check.
- **Cyberpunk Interface:** Matte black surfaces, gold accents (`@color/cyber_gold`), and neon alerts for a high-tech feel.

### Technical Stack
- **Kotlin & Coroutines:** Asynchronous node processing and fluid matrix traversal.
- **Dagger Hilt:** Dependency injection for a modular and scalable architecture.
- **Room Database:** Persistent local storage for all financial records.
- **Navigation Component:** Seamless transitions between system modules.
- **ViewBinding:** Type-safe access to the UI layer.
- **WorkManager:** Background tasks for automated overdue reminders.

### Project Structure
```text
app/src/main/
├── java/com/cyberpunk/debttracker/
│   ├── data/            # Room DB, models, repository
│   ├── di/              # Hilt dependency injection modules
│   ├── notification/    # Broadcast receivers & background workers
│   ├── ui/              # Activities, Fragments, ViewModels, Adapters
│   │   ├── about/
│   │   ├── adddebt/
│   │   ├── dashboard/
│   │   ├── debtdetail/
│   │   ├── settings/
│   │   └── splash/
│   └── util/            # Helpers, extensions, formatters
└── res/
    ├── anim/            # Transition animations
    ├── color/           # Color state selectors
    ├── drawable/        # Cyber-themed vector assets & backgrounds
    ├── layout/          # UI definitions
    ├── menu/            # Navigation & popup menus
    ├── navigation/      # Nav graph definitions
    └── values/          # Colors, strings, dimens, themes
```

### Dependencies
| Library | Purpose |
|---------|---------|
| AndroidX Core KTX | Core Android extensions |
| Material Components | Cyberpunk UI theming |
| Room + KSP | Local database with compile-time query validation |
| Dagger Hilt | Dependency injection |
| Navigation Component | Fragment navigation |
| MPAndroidChart | Pie & bar chart analytics |
| WorkManager + Hilt | Scheduled overdue reminders |
| Coroutines | Async operations |

### Installation
1. Clone the repository into your local terminal.
2. Build the project using Android Studio Hedgehog (2023.1.1) or higher.
3. Sync Gradle dependencies.
4. Deploy to your handheld device (API 26+).
5. **System Online.**

---
*Developed by: The Bumbay Operative*
*Protocol: Android 14 (API 34)*
