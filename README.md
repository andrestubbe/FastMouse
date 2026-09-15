# FastMouse 0.1.1 [ALPHA-2026-08-19] — Ultra-Low Latency Native RawInput Mouse Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastMouse/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastMouse)

---

**⚡ High-speed Win32 RawInput mouse interception, multi-device tracking, and native window-focus coordinate gating for Java.**

**FastMouse** delivers true unaccelerated sensor deltas, high-polling gaming mouse support (1,000 to 8,000 Hz), multi-mouse hardware identification, and native `ScreenToClient` client-pixel conversion directly from Win32 RawInput (`WM_INPUT`) with zero JVM Garbage Collection overhead.

[**Watch Showcase Demo (YouTube)**](https://youtu.be/f_NdYUV0kkU) | Watch JMH Benchmark (YouTube)

[![FastMouse Showcase](docs/screenshot.png)](https://youtu.be/f_NdYUV0kkU)

---

## Quick Start

```java
import fastmouse.FastMouse;
import fastmouse.FastMouseListener;

public class Demo {
    public static void main(String[] args) {
        // Global desktop capture or window-bound focus
        try (FastMouse mouse = FastMouse.open()) {

            // Optional: Bind to specific window (HWND) for local (0..width, 0..height) coordinates
            // mouse.bindToWindow(window.getHWND());

            mouse.startListening(new FastMouseListener() {
                @Override
                public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                    System.out.printf("Move: Delta(%+d, %+d) | Pos(%d, %d)\n", deltaX, deltaY, absX, absY);
                }

                @Override
                public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                    System.out.printf("Button %d: %s\n", buttonId, isPressed ? "DOWN" : "UP");
                }

                @Override
                public void onMouseWheel(long deviceHandle, int delta) {
                    System.out.printf("Wheel: %+d\n", delta);
                }
            });

            // Keep main thread alive
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## Table of Contents

- [Quick Start](#quick-start)
- [Why FastMouse?](#why-fastmouse)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Window Binding & Client Coordinates](#window-binding--client-coordinates)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastMouse?

Standard Java mouse handling (AWT `MouseMotionListener`, Swing, or JavaFX) introduces critical bottlenecks for real-time and high-performance applications:

- **OS Pointer Ballistics**: Standard APIs report accelerated, curved pointer coordinates instead of raw physical sensor counts.
- **Polling Rate Clamping**: Windows message queues throttle standard mouse events, dropping packets on 1,000 Hz – 8,000 Hz gaming mice.
- **Event Thread Contention**: AWT mouse events run on the Event Dispatch Thread (EDT), causing input lag during rendering spikes.
- **Global / Local Mismatch**: Global hooks require manual `ScreenToClient` calculations in Java, introducing rounding errors and multi-monitor DPI offsets.

**FastMouse** solves this fundamentally:

- **Bypasses Mouse Ballistics**: Reads raw hardware sensor deltas (`lLastX`, `lLastY`) directly from the HID driver.
- **High Polling Rate Ready**: Flawlessly processes 1,000 Hz, 4,000 Hz, and 8,000 Hz mice without dropping packets.
- **Native Client-Coordinate Conversion**: When bound to an `HWND`, Win32 `ScreenToClient` converts coordinates natively before dispatching to Java.
- **Focus Gating**: Ignores clicks and movements when the target window is in the background — **zero CPU overhead** when inactive.

---

## Key Features

- ⚡ **Direct Win32 RawInput (`WM_INPUT`)** — Pure hardware sensor stream.
- 🎯 **Native Window-Focus Gating** — Bind capture to any window (`bindToWindow(hwnd)`).
- 📐 **Instant Local Coordinates** — Automatically maps to `(0..width, 0..height)` client pixels.
- 🖱️ **Multi-Mouse Disambiguation** — Tracks individual physical mouse handles (`hDevice`).
- 🔄 **High-Resolution Scroll Wheel** — Full support for precision wheels and 5-button mice.
- 📦 **Zero GC Pressure** — High-performance event dispatching with zero heap allocations in the hot path.
- 🧹 **Clean FastJava Lifecycle** — Implements `AutoCloseable` with complete native cleanup.

---

## Real-World Use Cases

- 🎯 **Esports & 8,000 Hz Gaming Input**: Uncompressed 1:1 hardware sensor deltas without Windows pointer acceleration curves or smoothing ballistics.
- 🪟 **High-FPS Canvas & Vulkan Viewports ([FastVulkan](https://github.com/andrestubbe/FastVulkan))**: Native client-coordinate conversion (`ScreenToClient`) delivers zero-latency mouse picking, viewport panning, and zooming without JNI overhead.
- 🖱️ **Multi-Mouse Workstations & CAD**: Simultaneous independent tracking of multiple mice, presenter remotes, or trackballs via unique `hDevice` IDs.
- 🤖 **Desktop Automation & Screen Telemetry**: High-precision global cursor tracking and telemetry for automated recording and headless testing bots.

---

## Performance Benchmarks

FastMouse is rigorously profiled using **JMH** to guarantee zero overhead.

| Benchmark / Operation | Score (ops/ms) | Ops per Second |
|---|---|---|
| **`benchmarkGetConnectedDevices`** | **~265,718 ops/ms** | **> 265.7 Million** |
| **Direct Cursor Position Query** | **~88,400 ops/ms** | **> 88.4 Million** |

*Measured on Windows 11, Intel Core i5-1135G7 (Surface Pro 8), JDK 21.0.12.1. Native message pump processes 1,000 Hz to 8,000 Hz unthrottled event streams with sub-microsecond latency (< 280 ns) and 0 bytes GC allocation.*

---

## API Quick Reference

| Method | Return Type | Description | Docs |
|---|---|---|---|
| `FastMouse.open()` | `FastMouse` | Creates a global desktop RawInput capture instance. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `FastMouse.openForWindow(hwnd)` | `FastMouse` | Creates window-bound capture with auto client mapping. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `startListening(listener)` | `void` | Begins background raw input mouse message processing. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `stopListening()` | `void` | Stops the message pump thread and halts dispatching. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `bindToWindow(hwnd)` | `void` | Focus-gates capture and enables client coordinates. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `unbindFromWindow()` | `void` | Restores global desktop coordinate mapping. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `getConnectedDevices()` | `List<MouseDevice>` | Enumerates all connected HID mouse hardware devices. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `getCursorPosition()` | `int[]` | Retrieves `[x, y]` in client or desktop screen pixels. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |
| `close()` | `void` | Releases native hooks and frees unmanaged resources. | [Reference](docs/REFERENCE.md#interface-fastmousefastmouse) |

---

## Window Binding & Client Coordinates

FastMouse seamlessly toggles between **Global Desktop Interception** and **Window-Bound UI Capture**:

```java
FastMouse mouse = FastMouse.open();

// 1. Global Mode: Absolute coordinates are in screen pixels (Multi-Monitor aware)
mouse.unbindFromWindow();

// 2. Window Mode: Coordinates are strictly local client pixels, only active when window has focus
mouse.bindToWindow(window.getHWND());
```

> [!NOTE]
> Coordinate conversion and focus verification happen in native C++ via `ScreenToClient` and `GetForegroundWindow()`. Out-of-focus mouse movements cause **0 JNI traversals** and **0 JVM allocations**.

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Interactive Terminal Demo** | [Demo.java](examples/Demo/src/main/java/fastmouse/Demo.java) | `run-demo.bat` | High-speed mouse delta, button, and wheel monitor styled with FastANSI gray & bright-white theme. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastmouse/benchmark/Benchmark.java) | `run-benchmark.bat` | Performance benchmark measuring dispatch rates and query latency under heavy polling loads. |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

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
        <artifactId>FastMouse</artifactId>
        <version>0.1.1</version>
    </dependency>
    <!-- Required Native JNI loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
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
    implementation 'com.github.andrestubbe:FastMouse:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastMouse-0.1.1.jar](https://github.com/andrestubbe/FastMouse/releases/download/0.1.1/FastMouse-0.1.1.jar)** (The Core Library with embedded native DLL)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

---

## Documentation

- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
- **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions and method reference.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-allocation performance.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Complete version history and release notes.

---

## Platform Support

| Platform | Status |
|---|:---:|
| **Windows 10 / 11 (x64)** | ✅ Fully Supported (Native Win32 RawInput) |
| **Linux / macOS** | 🚧 Planned |

---

## Related Projects

- **[`FastCore`](https://github.com/andrestubbe/FastCore)** — Native Library Loader & JNI Utilities for Java
- **[`FastHotkey`](https://github.com/andrestubbe/FastHotkey)** — Low-Latency Global Hotkey API for Java
- **[`FastKeyboard`](https://github.com/andrestubbe/FastKeyboard)** — Ultra-Fast Native RawInput Keyboard Engine
- **[`FastTouch`](https://github.com/andrestubbe/FastTouch)** — Native Multi-Touch Digitizer API for Java
- **[`FastVulkan`](https://github.com/andrestubbe/FastVulkan)** — High-Performance Native Vulkan 2D Rendering Engine
- **[`FastTerminal`](https://github.com/andrestubbe/FastTerminal)** — Native High-Speed Terminal & TUI Engine

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀
