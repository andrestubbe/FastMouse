package fastmouse;

import java.util.List;

/**
 * FastMouse - Native Windows Raw Input Mouse API for Java.
 * 
 * Provides ultra-low latency mouse input by bypassing Windows mouse ballistics
 * and using the Win32 Raw Input API (WM_INPUT) directly.
 * Supports window-focus gating and native ScreenToClient coordinate conversion.
 */
public interface FastMouse extends AutoCloseable {

    /**
     * Creates a new global FastMouse instance.
     */
    static FastMouse open() {
        return new FastMouseImpl();
    }

    /**
     * Creates a new FastMouse instance bound to a specific Win32 window handle (HWND).
     * Coordinates are automatically converted to local client pixels (0..width, 0..height),
     * and events are only dispatched when the window has active focus.
     *
     * @param targetWindowHandle Native HWND
     */
    static FastMouse openForWindow(long targetWindowHandle) {
        return new FastMouseImpl(targetWindowHandle);
    }

    // ═══════════════════════════════════════════════════════════
    // Events & Lifecycle
    // ═══════════════════════════════════════════════════════════

    /**
     * Starts listening for raw mouse events.
     * 
     * @param listener The callback interface for mouse events
     */
    void startListening(FastMouseListener listener);

    /**
     * Stops listening for mouse events and cleans up native resources.
     */
    void stopListening();

    @Override
    default void close() {
        stopListening();
    }

    // ═══════════════════════════════════════════════════════════
    // Normal Methods (Binding & Devices)
    // ═══════════════════════════════════════════════════════════

    /**
     * Binds mouse capture to a specific Win32 window handle.
     * When bound, absolute coordinates become client-relative (0..width, 0..height)
     * and events are filtered so only active window events are dispatched.
     * Pass 0 to restore global desktop capture.
     *
     * @param targetWindowHandle Native HWND
     */
    void bindToWindow(long targetWindowHandle);

    /**
     * Restores global desktop mouse capture.
     */
    default void unbindFromWindow() {
        bindToWindow(0);
    }

    /**
     * Returns all currently connected mouse devices.
     * @return List of MouseDevice objects representing connected mice
     */
    List<MouseDevice> getConnectedDevices();

    // ═══════════════════════════════════════════════════════════
    // Is / Has
    // ═══════════════════════════════════════════════════════════

    /**
     * Checks if the listener is currently active.
     */
    boolean isListening();

    /**
     * Returns whether mouse capture is currently bound to a specific window.
     */
    boolean isWindowBound();

    // ═══════════════════════════════════════════════════════════
    // Getter
    // ═══════════════════════════════════════════════════════════

    /**
     * Retrieves the current cursor position.
     * If window-bound, returns [x, y] in local client pixels; otherwise in screen pixels.
     * 
     * @return An array of [x, y] coordinates
     */
    int[] getCursorPosition();

    /**
     * Returns the currently bound window handle (HWND), or 0 if listening globally.
     */
    long getBoundWindow();

    /**
     * Helper to retrieve the current process console window handle (HWND), or 0 if not running in a console.
     */
    static long getConsoleWindow() {
        return FastMouseImpl.getConsoleWindowHandle();
    }

    /**
     * Checks if a specific virtual key (e.g. 0x42 for 'B', 0x1B for ESC) is currently physically pressed.
     */
    static boolean isKeyPressed(int vKey) {
        return FastMouseImpl.isKeyPressed(vKey);
    }
}
