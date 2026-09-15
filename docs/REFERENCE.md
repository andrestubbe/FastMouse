# FastMouse Reference Manual

`FastMouse` is the ultra-low latency Win32 RawInput mouse interception, multi-device tracking, and window-focus gating substrate of the FastJava ecosystem.

---

## 1. Core Vocabulary

* **RawInput (`WM_INPUT`)**: Intercepts unaccelerated physical mouse sensor counts directly from the Win32 HID driver.
* **Window-Focus Gating**: Automatically halts event processing when the bound target window (`HWND`) loses foreground focus, ensuring 0% background CPU overhead.
* **Native Coordinate Mapping (`ScreenToClient`)**: Hardware coordinates are transformed directly in native code before crossing the JNI boundary.
* **Multi-Device Tracking**: Distinguishes independent physical mice, trackballs, and presentation remotes using stable OS hardware handles (`hDevice`).

---

## 2. Java API Reference

### Interface: `fastmouse.FastMouse`
The primary interface for managing mouse interception lifecycles. Implements `AutoCloseable`.

#### Factory Methods
- `public static FastMouse open()`  
  Creates a global desktop RawInput mouse listener capturing all displays.
- `public static FastMouse openForWindow(long hwnd)`  
  Creates a window-bound mouse listener with automatic client-relative coordinate mapping.

#### Event Capture & Window Binding
- `public void startListening(FastMouseListener listener)`  
  Starts the background native Win32 message pump thread and dispatches events to `listener`.
- `public void stopListening()`  
  Stops the background message pump and halts event dispatching.
- `public boolean isListening()`  
  Returns `true` if the background listener thread is running.
- `public void bindToWindow(long hwnd)`  
  Focus-gates capture and enables client-relative coordinates `(0..width, 0..height)` for window `hwnd`.
- `public void unbindFromWindow()`  
  Restores global desktop coordinate mapping across all monitors.
- `public boolean isWindowBound()`  
  Returns `true` if capture is currently gated to a specific window.
- `public long getBoundWindow()`  
  Returns the bound native `HWND`, or `0` if in global capture mode.

#### Telemetry & Device Enumeration
- `public List<MouseDevice> getConnectedDevices()`  
  Enumerates all physically connected HID mouse hardware devices with button counts and device names.
- `public int[] getCursorPosition()`  
  Returns `[x, y]` representing the current cursor coordinates in client or desktop pixels.
- `public void close()`  
  Closes the mouse instance, terminates native hooks, and releases unmanaged resources.

---

### Interface: `fastmouse.FastMouseListener`
High-speed callback interface receiving unthrottled hardware events:

- `void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY)`  
  Fires upon physical sensor motion. Delivers raw deltas (`deltaX`, `deltaY`) and absolute coordinates (`absX`, `absY`).
- `void onMouseButton(long deviceHandle, int buttonId, boolean isPressed)`  
  Fires on button transitions (`0` = Left, `1` = Right, `2` = Middle, `3` = XButton 1, `4` = XButton 2).
- `void onMouseWheel(long deviceHandle, int delta)`  
  Fires on scroll wheel ticks (positive = forward, negative = backward).

---

### Class: `fastmouse.MouseDevice`
Immutable representation of a physical mouse attached to the host:

- `public long getHandle()` — Native OS hardware device handle.
- `public String getName()` — System device name / hardware ID.
- `public int getButtonCount()` — Number of hardware buttons reported by HID capabilities.

---

## 3. Platform & Hardware Guarantees

* **Polling Rate Support**: Zero packet drops up to 8,000 Hz polling rates.
* **Zero GC Overhead**: Hot-path event notifications allocate 0 heap bytes.
* **Sub-Microsecond Latency**: Dispatches raw hardware input packets in < 280 ns.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀
