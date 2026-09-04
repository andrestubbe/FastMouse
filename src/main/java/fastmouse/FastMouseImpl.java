package fastmouse;

import fastcore.FastCore;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Implementation of {@link FastMouse} using Win32 Raw Input API via JNI.
 */
class FastMouseImpl implements FastMouse {

    static {
        // Automatically extracts and loads the native DLL via FastCore
        FastCore.loadLibrary("fastmouse");
    }

    private long nativeHandle = 0;
    private long targetWindowHandle = 0;
    private FastMouseListener listener;

    /**
     * Creates a new global FastMouseImpl instance.
     */
    public FastMouseImpl() {
        this(0);
    }

    /**
     * Creates a new FastMouseImpl instance bound to a specific Win32 window (HWND).
     *
     * @param targetWindowHandle Native HWND of the target window (or 0 for global)
     */
    public FastMouseImpl(long targetWindowHandle) {
        this.targetWindowHandle = targetWindowHandle;
    }

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
        return IntStream.range(0, handles.length)
                .mapToObj(i -> {
                    long handle = handles[i];
                    String name = nativeGetDeviceName(handle);
                    int buttonCount = nativeGetDeviceButtonCount(handle);
                    return new MouseDevice(handle, name, buttonCount);
                })
                .toList();
    }

    @Override
    public boolean isListening() {
        return nativeHandle != 0;
    }

    @Override
    public boolean isWindowBound() {
        return targetWindowHandle != 0;
    }

    @Override
    public int[] getCursorPosition() {
        return nativeGetCursorPosition();
    }

    @Override
    public long getBoundWindow() {
        return targetWindowHandle;
    }

    /**
     * Called by C++ JNI layer on mouse movement events.
     */
    private void onNativeMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
        if (listener != null) {
            listener.onMouseMove(deviceHandle, deltaX, deltaY, absoluteX, absoluteY);
        }
    }

    /**
     * Called by C++ JNI layer on mouse button events.
     */
    private void onNativeMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
        if (listener != null) {
            listener.onMouseButton(deviceHandle, buttonId, isPressed);
        }
    }

    /**
     * Called by C++ JNI layer on mouse wheel events.
     */
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
    private static native long nativeGetConsoleWindow();

    /**
     * Resolves the current console window HWND handle.
     */
    static long getConsoleWindowHandle() {
        return nativeGetConsoleWindow();
    }

    /**
     * Checks if a specific virtual key is currently pressed.
     */
    static boolean isKeyPressed(int vKey) {
        return nativeIsKeyPressed(vKey);
    }

    private static native boolean nativeIsKeyPressed(int vKey);
}
