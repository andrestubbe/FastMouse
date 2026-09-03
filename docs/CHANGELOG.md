# Changelog

All notable changes to this project will be documented in this file.

## [0.1.1] - 2026-08-19

### Added
- **Native Window-Focus Gating & ScreenToClient Conversion**: Added `openForWindow(hwnd)`, `bindToWindow(hwnd)`, and `unbindFromWindow()`. Coordinates are automatically mapped to local client pixels (`0..width, 0..height`) via Win32 `ScreenToClient` in C++, and events are filtered via `GetForegroundWindow()`.
- **FastJava Standard Method Ordering**: Enforced strict ordering: Events -> Normal Methods -> Is/Has -> Getter -> Setter -> Native.
- **AutoCloseable**: FastMouse now implements `AutoCloseable` with automatic cleanup in try-with-resources.
- **FastANSI Demo**: Interactive terminal demo with FastANSI gray/bright-white theme and high-frequency delta monitoring.
- **Modern FastJava Documentation**: Updated `README.md`, `CHANGELOG.md`, and `REFERENCE.md` matching the FastVulkan / FastAnimation blueprint standard.

## [0.1.0] - 2026-05-23

### Added
- Initial release
- Win32 RawInput (`WM_INPUT`) mouse interception bypassing OS ballistics
- Multi-mouse hardware enumeration and high polling-rate support
