package fastmouse;

import fastcore.FastCore;
import java.util.List;

/**
 * Implementation of FastMouse using JNI to call native C++ code.
 */
class FastMouseImpl implements FastMouse {

    // Load the native library once upon class initialization
    static {
        FastCore.loadLibrary("fastmouse");
    }

    private long nativeHandle = 0;
    private long targetWindowHandle = 0;
    private FastMouseListener listener;

    public FastMouseImpl() {
        this(0);
    }

    public FastMouseImpl(long targetWindowHandle) {
        this.targetWindowHandle = targetWindowHandle;
    }

    // ═══════════════════════════════════════════════════════════
    // Events & Lifecycle
    // ═══════════════════════════════════════════════════════════

    @Override
    public void startListening(FastMouseListener listener) {
        if (nativeHandle == 0) {
            nativeHandle = nativeInitializeForWindow(targetWindowHandle);
        }
        this.listener = listener;
        nativeStartListening(nativeHandle);
    }

    @Override
    public void stopListening() {
        if (nativeHandle != 0) {
            nativeStopListening(nativeHandle);
            nativeHandle = 0;
            listener = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Normal Methods (Binding & Devices)
    // ═══════════════════════════════════════════════════════════

    @Override
    public void bindToWindow(long targetWindowHandle) {
        this.targetWindowHandle = targetWindowHandle;
        if (nativeHandle != 0) {
            nativeBindWindow(nativeHandle, targetWindowHandle);
        }
    }

    @Override
    public List<MouseDevice> getConnectedDevices() {
        long[] handles = nativeGetConnectedDevices();
        return java.util.stream.IntStream.range(0, handles.length)
                .mapToObj(i -> {
                    long handle = handles[i];
                    String name = nativeGetDeviceName(handle);
                    int buttonCount = nativeGetDeviceButtonCount(handle);
                    return new MouseDevice(handle, name, buttonCount);
                })
                .toList();
    }

    // ═══════════════════════════════════════════════════════════
    // Is / Has
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean isListening() {
        return nativeHandle != 0;
    }

    @Override
    public boolean isWindowBound() {
        return targetWindowHandle != 0;
    }

    // ═══════════════════════════════════════════════════════════
    // Getter
    // ═══════════════════════════════════════════════════════════

    @Override
    public int[] getCursorPosition() {
        return nativeGetCursorPosition();
    }

    @Override
    public long getBoundWindow() {
        return targetWindowHandle;
    }

    // ═══════════════════════════════════════════════════════════
    // Native Callbacks & Methods
    // ═══════════════════════════════════════════════════════════

    private void onNativeMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
        if (listener != null) {
            listener.onMouseMove(deviceHandle, deltaX, deltaY, absoluteX, absoluteY);
        }
    }

    private void onNativeMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
        if (listener != null) {
            listener.onMouseButton(deviceHandle, buttonId, isPressed);
        }
    }

    private void onNativeMouseWheel(long deviceHandle, int delta) {
        if (listener != null) {
            listener.onMouseWheel(deviceHandle, delta);
        }
    }

    private native long nativeInitialize();
    private native long nativeInitializeForWindow(long targetWindowHandle);
    private native void nativeBindWindow(long handle, long targetWindowHandle);
    private native void nativeStartListening(long handle);
    private native void nativeStopListening(long handle);
    private native long[] nativeGetConnectedDevices();
    private native String nativeGetDeviceName(long handle);
    private native int nativeGetDeviceButtonCount(long handle);
    private native int[] nativeGetCursorPosition();
}
