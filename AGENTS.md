# Guidelines for AI Agents Working on Decode

This file provides context and conventions for AI coding agents working on the Decode project.

## Project Overview

Decode is an Android APK reverse engineering and editor suite. The app uses Jetpack Compose with Material 3 for UI, Kotlin Coroutines for async processing, and Room for data persistence.

## Architecture

```
com.decode.app/
├── DecodeApp.kt          # Application class
├── MainActivity.kt       # Single activity entry point
├── ui/
│   ├── theme/            # Material 3 theming
│   ├── navigation/       # NavHost routing
│   ├── screens/          # Screen composables
│   │   ├── home/         # Main screen
│   │   ├── editor/       # Code editor
│   │   ├── tools/        # Tool catalog
│   │   ├── project/      # Project workspace
│   │   └── about/        # About & credits
│   └── components/       # Reusable UI components
├── engine/               # Reverse engineering engines
│   ├── apktool/          # Resource decode/rebuild
│   ├── smali/            # DEX assembly/disassembly
│   ├── dex/              # DEX analysis & decompilation
│   ├── signing/          # APK signing
│   ├── zip/              # Zip manipulation
│   ├── axml/             # Android XML parser
│   ├── elf/              # ELF binary analyzer
│   ├── svg/              # SVG renderer
│   ├── image/            # Image tools
│   └── pngquant/         # PNG optimization
└── data/
    ├── model/            # Room entities
    ├── db/               # Database & DAOs
    └── repository/       # Data repositories
```

## Conventions

### Code Style
- Kotlin with Jetpack Compose for UI
- No comments in code (keep it self-documenting)
- Use Material 3 components exclusively
- Use `Modifier` parameter as first optional parameter
- Follow official Kotlin coding conventions

### Build System
- Gradle 8.5 with Kotlin DSL
- AGP 8.2.2
- Kotlin 1.9.22
- Version catalog at `gradle/libs.versions.toml`

### Dependencies
- Always check existing dependencies before adding new ones
- Prefer Maven Central and Google Maven repositories
- Use version catalog for all dependency versions
- Never add a dependency that the project doesn't already use without review

### UI Patterns
- Single activity architecture
- Navigation via Jetpack Compose Navigation
- State management with ViewModels and StateFlow
- Each screen is a self-contained composable
- Reusable components in `ui/components/`

### When Making Changes
1. Read the relevant files first
2. Understand the existing patterns before adding new code
3. Run `./gradlew assembleDebug` to verify the build
4. Update AGENTS.md if adding new patterns or conventions
5. Keep the project building at all times

## Project Status

The project is under active development by Llucs. The core architecture is established and new features should follow existing patterns.
