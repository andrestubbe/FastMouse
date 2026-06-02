# FastMouse REFERENCE

## 1. Java API Reference

### `FastMouse` (Interface)
Die Haupt-Schnittstelle zur Interaktion mit dem Raw Input Subsystem.

| Methode | Beschreibung | Parameter | Rückgabewert |
| :--- | :--- | :--- | :--- |
| `static FastMouse open()` | Instanziiert das native JNI-Backend. | — | `FastMouse` |
| `void startListening(FastMouseListener l)` | Startet den nativen Message-Thread. | `FastMouseListener` | — |
| `void stopListening()` | Stoppt den nativen Thread und gibt Handles frei. | — | — |
| `List<MouseDevice> getConnectedDevices()` | Zählt alle aktiven Hardware-Mäuse auf. | — | `List<MouseDevice>` |
| `int[] getCursorPosition()` | Fragt die absoluten Bildschirm-Koordinaten ab. | — | `int[]` (Format: `[x, y]`) |

### `FastMouseListener` (Callback-Interface)
Erhält Hardware-Events direkt aus dem nativen Thread.

```java
public interface FastMouseListener {
    void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY);
    void onMouseButton(long deviceHandle, int buttonId, boolean isPressed);
    void onMouseWheel(long deviceHandle, int delta);
}
```

*   `deviceHandle`: Stabile ID des physischen Eingabegeräts.
*   `deltaX`/`deltaY`: Rohdaten-Bewegungsdeltas vom Maussensor (ohne OS-Zeigerbeschleunigung).
*   `absoluteX`/`absoluteY`: Absoluter Desktop-Pixelort des Mauszeigers.
*   `buttonId`: `0` = Links, `1` = Rechts, `2` = Mitte, `3` = Daumen vor, `4` = Daumen zurück.

---

## 2. Native Win32 JNI Schnittstelle

Die nativen Signaturen sind in C++ exportiert über `fastmouse.dll`:

```cpp
JNIEXPORT jlong JNICALL Java_fastmouse_FastMouseImpl_nativeInitialize(JNIEnv*, jobject);
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
