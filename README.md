# FastMouse 0.1.0 [ALPHA-2026-06] — � High-Performance Native Mouse API for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastMouse/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastMouse)

---

**âš¡ A zero-latency native mouse module for the FastJava ecosystem. Real-time mouse movement, button events, and global
hooks via JNI.**

FastMouse provides an ultra-fast alternative to the standard Java AWT mouse system. By using native Windows hooks and
direct JNI calls, it delivers movement and button events with zero latency.

---

[![FastKeyboard Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)


---

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [License](#license)

---

## Features
- **ðŸš€? Global Hooks**: Capture mouse events system-wide with zero latency.
- **? Raw Speed**: Bypasses the heavy Java AWT/Swing event thread.
- **ðŸš€ Minimal Overhead**: Optimized JNI implementation for high-frequency input.
- **ðŸš€ Native Reliability**: Direct Win32 API integration.

---

## Installation

### Option 1: Maven (Recommended)
Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastMouse Library -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastmouse</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastmouse:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. ðŸš€ **[fastmouse-0.1.0.jar](https://github.com/andrestubbe/FastMouse/releases/download/0.1.0/fastmouse-0.1.0.jar)** (The Core Library)
2. ðŸš€ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, border configurations, and codepoint index.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-allocation performance.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.

---
## License
MIT License  See [LICENSE](LICENSE) file for details.

---

## Related Projects
- [FastCore](https://github.com/andrestubbe/FastCore)  Native Library Loader & JNI Utilities for Java
- [FastHotkey](https://github.com/andrestubbe/FastHotkey)  Low-Latency Global Hotkey API for Java
- [FastMouse](https://github.com/andrestubbe/FastMouse)  Native Windows RawInput API for Java
- [FastKeylogger](https://github.com/andrestubbe/FastKeylogger)  Behavioral Typing Logic for Java
- [FastTouch](https://github.com/andrestubbe/FastTouch)  Native touchscreen input for Java
- [FastStylus](https://github.com/andrestubbe/FastStylus)  Native Stylus/Pen Input for Java

---
**Part of the FastJava Ecosystem**  *Making the JVM faster.*


