package fastmouse;

import fastcore.FastCore;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Implementation of {@link FastMouse} using the Win32 Raw Input API via JNI.
 *
 * <p>Dispatches raw mouse input events (deltas, absolute client/screen coordinates,
 * button states, and wheel rotations) directly from the native Windows message loop.</p>
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
     * Creates a new global FastMouseImpl instance capturing mouse input across the entire desktop.
     */
    public FastMouseImpl() {
        this(0);
    }

    /**
     * Creates a new FastMouseImpl instance bound to a specific Win32 window (HWND).
     *
     * @param targetWindowHandle Native HWND of the target window (or 0 for global desktop capture)
     */
    public FastMouseImpl(long targetWindowHandle) {
        this.targetWindowHandle = targetWindowHandle;
    }

    /**
     * Starts listening for raw mouse events on a background native thread.
     *
     * @param listener The callback interface for receiving mouse input events
     */
    @Override
    public void startListening(FastMouseListener listener) {
        if (nativeHandle == 0) {
            nativeHandle = nativeInitializeForWindow(targetWindowHandle);
        }
        this.listener = listener;
        nativeStartListening(nativeHandle);
    }

    /**
     * Stops listening for mouse events and releases native resources.
     */
    @Override
    public void stopListening() {
        if (nativeHandle != 0) {
            nativeStopListening(nativeHandle);
            nativeHandle = 0;
            listener = null;
        }
    }

    /**
     * Binds mouse capture to a specific Win32 window handle.
     *
     * @param targetWindowHandle Native HWND of the window to bind to (or 0 for global)
     */
    @Override
    public void bindToWindow(long targetWindowHandle) {
        this.targetWindowHandle = targetWindowHandle;
        if (nativeHandle != 0) {
            nativeBindWindow(nativeHandle, targetWindowHandle);
        }
    }

    /**
     * Enumerates and returns all currently connected raw input mouse devices.
     *
     * @return List of {@link MouseDevice} descriptors
     */
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

    /**
     * Checks whether the native mouse listener thread is currently active.
     *
     * @return {@code true} if listening, {@code false} otherwise
     */
    @Override
    public boolean isListening() {
        return nativeHandle != 0;
    }

    /**
     * Checks whether mouse input capture is currently bound to a specific window.
     *
     * @return {@code true} if bound to an HWND, {@code false} if global
     */
    @Override
    public boolean isWindowBound() {
        return targetWindowHandle != 0;
    }

    /**
     * Retrieves the current cursor position as [x, y].
     *
     * @return An integer array where index 0 is x and index 1 is y
     */
    @Override
    public int[] getCursorPosition() {
        return nativeGetCursorPosition();
    }

    /**
     * Returns the native window handle (HWND) currently bound, or 0 if unbound.
     *
     * @return The native HWND handle
     */
    @Override
    public long getBoundWindow() {
        return targetWindowHandle;
    }

    /**
     * Called by C++ JNI layer on mouse movement events.
     *
     * @param deviceHandle Native raw input device handle
     * @param deltaX Raw relative movement in X
     * @param deltaY Raw relative movement in Y
     * @param absoluteX Absolute cursor X coordinate
     * @param absoluteY Absolute cursor Y coordinate
     */
    private void onNativeMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
        if (listener != null) {
            listener.onMouseMove(deviceHandle, deltaX, deltaY, absoluteX, absoluteY);
        }
    }

    /**
     * Called by C++ JNI layer on mouse button events.
     *
     * @param deviceHandle Native raw input device handle
     * @param buttonId Button identifier (0=Left, 1=Right, 2=Middle, 3=X1, 4=X2)
     * @param isPressed {@code true} if pressed, {@code false} if released
     */
    private void onNativeMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
        if (listener != null) {
            listener.onMouseButton(deviceHandle, buttonId, isPressed);
        }
    }

    /**
     * Called by C++ JNI layer on mouse wheel events.
     *
     * @param deviceHandle Native raw input device handle
     * @param delta Scroll wheel delta (typically multiples of 120, positive for up / negative for down)
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
     *
     * @return The HWND handle of the console window, or 0 if none
     */
    static long getConsoleWindowHandle() {
        return nativeGetConsoleWindow();
    }

    /**
     * Checks if a specific virtual key is currently pressed.
     *
     * @param vKey Win32 Virtual-Key code (e.g. 0x42 for 'B', 0x1B for ESC)
     * @return {@code true} if the key is down, {@code false} otherwise
     */
    static boolean isKeyPressed(int vKey) {
        return nativeIsKeyPressed(vKey);
    }

    private static native boolean nativeIsKeyPressed(int vKey);
}
