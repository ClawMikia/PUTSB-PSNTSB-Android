# PUTSB-PSNTSB (Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot)

## The Neural Debt Matrix

**DebtTracker** is a specialized financial node tracker designed for high-stakes debt management in the sprawl. This application helps you keep track of every credit and debit in your personal network with a sleek, immersive cyberpunk aesthetic.

[![System Status: Online](https://img.shields.io/badge/System-Online-gold?style=for-the-badge&logo=android)](https://github.com/your-repo)
[![Version: 2.0.7-CYBER](https://img.shields.io/badge/Version-2.0.7--CYBER-gold?style=for-the-badge)](https://github.com/your-repo)
[![Theme: Cyberpunk](https://img.shields.io/badge/Theme-Cyberpunk-gold?style=for-the-badge)](https://github.com/your-repo)

---

### "The Golden Rule of the Streets"
> *"Pag Utang Tawag Sakin Boss, Pag Singil na Tawag Sakin Bantot"*

Inspired by legendary street wisdom, this motto encapsulates the fluid nature of financial respect in the matrix.

---

## ⚡ Latest Functionalities & Features

### 🔹 Neural Dashboard
*   **Net Balance Engine:** Real-time calculation of your total financial exposure (Owed vs Lent).
*   **Active Node Monitoring:** Quick glance at active, partial, and overdue nodes.
*   **Top Entity Tracking:** Automatically identifies and ranks your **Top Creditors** and **Top Debtors**.

### 🔹 Debt Node Management
*   **Dynamic Matrix:** Initialize new debt nodes with detailed parameters: Entity Name, Amount, Description, and Optional Due Dates.
*   **Partial Payment Protocols:** Record fractional payments with automated balance updates and progress percentage visualization.
*   **Status Lifecycle:** Nodes transition through **Active**, **Partial**, **Overdue**, and **Settled** states.

### 🔹 Advanced Sorting & Search
*   **Multi-dimensional Sorting:** Organize your matrix by Date (Newest/Oldest), Amount (High/Low), Alphabetical (A-Z), or prioritize Overdue nodes.
*   **Entity Filtering:** Instant search across the entire network for specific nodes.

### 🔹 Data Security & Archiving
*   **Encrypted Archive:** A secure vault for settled or sensitive nodes, completely hidden from the main interface.
*   **Biometric/Password Lock:** Access the archive only after successful authentication with your private key.

### 🔹 System Export & Analytics
*   **Excel Matrix Export:** Generate high-fidelity `.xlsx` reports of your entire debt history using **Apache POI**.
*   **Visual Analytics:** Data-driven insights via **MPAndroidChart** providing pie charts for debt distribution and timeline visualizations.

### 🔹 Intelligence Notifications
*   **Overdue Reminders:** Background scanning for overdue nodes with configurable frequencies (Hourly, Daily, Weekly) powered by **WorkManager**.

---

## 🎨 UI & Design Language

The interface is engineered for high-contrast, low-light operations typical of the sprawl.

*   **Primary Palette:** 
    *   `Cyber Gold (#FFD700)`: Used for primary actions, progress tracks, and critical data.
    *   `Matte Black (#0A0A0A)`: Deep background surfaces to minimize visual fatigue.
*   **Semantic Indicators:**
    *   🔴 `Neon Red`: High-risk nodes (I Owe).
    *   🟢 `Neon Green`: High-value nodes (Owes Me).
    *   🟠 `Neon Amber`: Partial progress / Attention required.
*   **Cyber Components:**
    *   Custom bottom navigation with holographic-style selectors.
    *   Progress bars reflecting the exact percentage of debt settlement.
    *   Translucent overlays and glowing borders for active UI focus.

---

## 🛠 Technical Stack

*   **Language:** Kotlin with Coroutines & Flow for reactive data streams.
*   **Dependency Injection:** Dagger Hilt for a modular, clean architecture.
*   **Database:** Room Persistence Library with KSP for type-safe SQL queries.
*   **Navigation:** Jetpack Navigation Component with custom transition animations.
*   **Background Tasks:** WorkManager for reliable reminder scheduling.
*   **Charts:** MPAndroidChart for sophisticated data visualization.
*   **Office Integration:** Apache POI for Excel document generation.
*   **UI Framework:** Material Design 3 with custom Cyberpunk extensions.

---

## 📂 Project Structure

```text
app/src/main/
├── java/com/cyberpunk/debttracker/
│   ├── data/            # Room Database, DAOs, Entity Models, Repositories
│   ├── di/              # Hilt Dependency Injection Modules
│   ├── notification/    # Broadcast Receivers & Background Workers (Reminders)
│   ├── ui/              # MVVM Pattern: Activities, Fragments, ViewModels, Adapters
│   │   ├── dashboard/   # Main command center & Top Entities
│   │   ├── analytics/   # MPAndroidChart integration
│   │   ├── archive/     # Password-protected node storage
│   │   ├── settings/    # System configuration & reminder frequency
│   │   └── intro/       # System initialization & onboarding
│   └── util/            # Helpers for Excel Export, Security, and Formatting
└── res/
    ├── drawable/        # Cyber-themed vector assets & custom backgrounds
    ├── layout/          # Immersive UI definitions
    ├── menu/            # Context-aware navigation & sort menus
    └── values/          # Theming (colors.xml, strings.xml, themes.xml)
```

---

## 🚀 Deployment

1.  **Terminal Sync:** Clone the repository.
2.  **Initialization:** Open in Android Studio Hedgehog (2023.1.1) or later.
3.  **Syncing:** Allow Gradle to fetch all neural dependencies.
4.  **Deployment:** Run on an Android device (API 26 or higher).
5.  **Authorization:** Follow the intro sequence to initialize your system parameters.

---

*Developed by: The Bumbay Operative*
*System Protocol: Android 14 (API 34)*
*Security Status: Fully Encrypted*
