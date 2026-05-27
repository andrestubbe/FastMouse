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
    private FastMouseListener listener;

    /**
     * Native method: Initialize the Raw Input listener
     * @return Native handle for the listener
     */
    private native long nativeInitialize();

    /**
     * Native method: Start listening for mouse events
     * @param handle The native handle from initialize
     */
    private native void nativeStartListening(long handle);

    /**
     * Native method: Stop listening and cleanup
     * @param handle The native handle from initialize
     */
    private native void nativeStopListening(long handle);

    /**
     * Native method: Get connected mouse devices
     * @return Array of device handles
     */
    private native long[] nativeGetConnectedDevices();

    /**
     * Native method: Get device name for a handle
     * @param handle The device handle
     * @return Device name
     */
    private native String nativeGetDeviceName(long handle);

    /**
     * Native method: Get button count for a device
     * @param handle The device handle
     * @return Number of buttons
     */
    private native int nativeGetDeviceButtonCount(long handle);
    private native int[] nativeGetCursorPosition();

    /**
     * Callback from native code when mouse moves
     */
    private void onNativeMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
        if (listener != null) {
            listener.onMouseMove(deviceHandle, deltaX, deltaY, absoluteX, absoluteY);
        }
    }

    /**
     * Callback from native code when mouse button changes
     */
    private void onNativeMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
        if (listener != null) {
            listener.onMouseButton(deviceHandle, buttonId, isPressed);
        }
    }

    /**
     * Callback from native code when mouse wheel scrolls
     */
    private void onNativeMouseWheel(long deviceHandle, int delta) {
        if (listener != null) {
            listener.onMouseWheel(deviceHandle, delta);
        }
    }

    @Override
    public void startListening(FastMouseListener listener) {
        if (nativeHandle == 0) {
            nativeHandle = nativeInitialize();
        }
        this.listener = listener;
        nativeStartListening(nativeHandle);
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

    @Override
    public void stopListening() {
        if (nativeHandle != 0) {
            nativeStopListening(nativeHandle);
            nativeHandle = 0;
            listener = null;
        }
    }

    @Override
    public int[] getCursorPosition() {
        return nativeGetCursorPosition();
    }
}
