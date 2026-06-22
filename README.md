# Decode

APK Reverse Engineering & Editor Suite for Android

Decode is an open-source APK reverse engineering and editing suite that lets you open, analyze, modify, rebuild, and sign APK files directly on your Android device — all offline and without modifying the original file.

## Features

- **APK Analysis** — Inspect APK structure, manifest, resources, and DEX files
- **DEX Decompilation** — Decompile DEX bytecode to Java using JADX
- **Smali Assembly** — Assemble and disassemble DEX bytecode
- **Resource Editing** — Modify Android resources, layouts, and assets
- **APK Rebuilding** — Rebuild modified APKs with proper structure
- **APK Signing** — Sign APKs with v1/v2/v3 signature schemes
- **File Editor** — Built-in code editor with syntax highlighting
- **ELF Analysis** — Inspect native library headers
- **SVG Rendering** — Preview and convert SVG resources
- **Image Optimization** — Compress and optimize PNG images
- **Project Management** — Track recent projects with Room database

## Built With

- [Kotlin](https://kotlinlang.org/) — Modern programming language
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Modern UI toolkit
- [Material 3](https://m3.material.io/) — Material Design 3 components
- [Room](https://developer.android.com/training/data-storage/room) — SQLite ORM
- [Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) — Async processing
- [AndroidX](https://developer.android.com/jetpack/androidx) — Support libraries

## Internal Tools

Decode integrates the following open-source tools internally:

| Tool | Description | License |
|------|-------------|---------|
| [Apktool](https://ibotpeaches.github.io/Apktool/) | Resource decompilation and recompilation | Apache 2.0 |
| [AAPT2](https://developer.android.com/studio/command-line/aapt2) | Android Asset Packaging Tool | Apache 2.0 |
| [Smali/Baksmali](https://github.com/JesusFreke/smali) | DEX assembler/disassembler | BSD 3-Clause |
| [DexLib2](https://github.com/JesusFreke/smali) | DEX bytecode library | BSD 3-Clause |
| [JADX](https://github.com/skylot/jadx) | DEX to Java decompiler | Apache 2.0 |
| [APK Signature](https://developer.android.com/tools/apksigner) | APK signing tool | Apache 2.0 |
| [ZipAlign](https://developer.android.com/studio/command-line/zipalign) | APK alignment | Apache 2.0 |
| [Commons IO](https://commons.apache.org/proper/commons-io/) | IO utilities | Apache 2.0 |
| [Guava](https://github.com/google/guava) | Core libraries | Apache 2.0 |
| [ANTLR4](https://www.antlr.org/) | Parser generator | BSD 3-Clause |
| [AndroidSVG](https://github.com/opencollab/androidsvg) | SVG rendering | Apache 2.0 |
| [PhotoView](https://github.com/Baseflow/PhotoView) | Image viewer | Apache 2.0 |
| [Coil](https://coil-kt.github.io/coil/) | Image loading | Apache 2.0 |

## Getting Started

### Prerequisites

- Android 8.0 (API 26) or higher
- 4GB+ RAM recommended for large APKs

### Download

Download the latest APK from the [Releases](https://github.com/Llucs/Decode/releases) page.

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/Llucs/Decode.git
   ```

2. Open in Android Studio or build via command line:
   ```bash
   ./gradlew assembleRelease
   ```

3. The APK will be generated at `app/build/outputs/apk/release/`

## Usage

1. **Open an APK** — Tap the FAB (+) button or the "Open APK" card to select an APK file
2. **Analyze** — The APK is extracted to an isolated workspace for inspection
3. **Edit** — Modify resources, smali code, or decompiled Java files
4. **Rebuild** — Reconstruct the APK with your changes
5. **Sign** — Sign the APK for installation
6. **Export** — Save the final APK ready for installation

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the Apache License 2.0 — see [LICENSE](LICENSE) for details.

## Credits

Developed by [Llucs](https://github.com/Llucs).

Decode uses several open-source libraries internally. Full credits are available in the app's About screen.
