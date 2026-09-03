# FastMouse Technical Reference

## 1. Java API Reference

### `FastMouse` (Interface)
The primary interface for interacting with the native Raw Input mouse subsystem.

| Method | Description |
|:---|:---|
| `static FastMouse open()` | Instantiates global desktop Raw Input listener. |
| `static FastMouse openForWindow(long hwnd)` | Instantiates window-bound listener with auto `ScreenToClient` pixel mapping. |
| `void startListening(FastMouseListener l)` | Starts the background Win32 message pump thread. |
| `void stopListening()` | Stops the background thread and cleans up native resources. |
| `void bindToWindow(long hwnd)` | Focus-gates events and converts absolute coordinates to client-relative. |
| `void unbindFromWindow()` | Restores global desktop capture mode. |
| `List<MouseDevice> getConnectedDevices()` | Enumerates physically attached HID mouse devices. |
| `int[] getCursorPosition()` | Retrieves cursor position in client or screen coordinates. |

### `FastMouseListener` (Callback Interface)
Receives unthrottled hardware events directly from the native thread:

```java
public interface FastMouseListener {
    void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY);
    void onMouseButton(long deviceHandle, int buttonId, boolean isPressed);
    void onMouseWheel(long deviceHandle, int delta);
}
```

* `deviceHandle`: Stable native handle of the physical mouse hardware device.
* `deltaX` / `deltaY`: Raw unaccelerated sensor deltas directly from the mouse sensor.
* `absoluteX` / `absoluteY`: If window-bound, relative client pixels `(0..width, 0..height)`; if global, desktop screen pixels.
* `buttonId`: `0` = Left, `1` = Right, `2` = Middle, `3` = Button 4, `4` = Button 5.

---

## 2. Native Win32 JNI Interface

Exported from `fastmouse.dll`:

```cpp
JNIEXPORT jlong JNICALL Java_fastmouse_FastMouseImpl_nativeInitialize(JNIEnv*, jobject);
JNIEXPORT jlong JNICALL Java_fastmouse_FastMouseImpl_nativeInitializeForWindow(JNIEnv*, jobject, jlong targetWindowHandle);
JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeBindWindow(JNIEnv*, jobject, jlong handle, jlong targetWindowHandle);
JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStartListening(JNIEnv*, jobject, jlong);
JNIEXPORT void JNICALL Java_fastmouse_FastMouseImpl_nativeStopListening(JNIEnv*, jobject, jlong);
JNIEXPORT jlongArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetConnectedDevices(JNIEnv*, jobject);
JNIEXPORT jstring JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceName(JNIEnv*, jobject, jlong);
JNIEXPORT jint JNICALL Java_fastmouse_FastMouseImpl_nativeGetDeviceButtonCount(JNIEnv*, jobject, jlong);
JNIEXPORT jintArray JNICALL Java_fastmouse_FastMouseImpl_nativeGetCursorPosition(JNIEnv*, jobject);
```

---

## 3. Agent JSON API (Ecosystem Integration)

Für KI-Agenten, die die angeschlossene Hardware auflisten wollen:

### Request: `get_mouse_devices`
```json
{
  "action": "get_mouse_devices"
}
```

### Response
```json
{
  "devices": [
    {
      "handle": 1,
      "name": "Logitech G Pro X Superlight",
      "buttons": 5
    }
  ]
}
```
