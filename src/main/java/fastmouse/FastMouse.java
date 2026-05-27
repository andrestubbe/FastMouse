package fastmouse;

import fastcore.FastCore;
import java.util.List;

/**
 * FastMouse - Native Windows Raw Input Mouse API for Java.
 * 
 * Provides ultra-low latency mouse input by bypassing Windows mouse ballistics
 * and using the Win32 Raw Input API (WM_INPUT) directly.
 */
public interface FastMouse {

    /**
     * Creates a new FastMouse instance.
     * @return A new FastMouse instance
     */
    static FastMouse open() {
        return new FastMouseImpl();
    }

    /**
     * Starts listening for raw mouse events.
     * This registers a message-only window and begins processing WM_INPUT messages
     * on a background thread.
     * 
     * @param listener The callback interface for mouse events
     */
    void startListening(FastMouseListener listener);

    /**
     * Returns all currently connected mouse devices.
     * @return List of MouseDevice objects representing connected mice
     */
    List<MouseDevice> getConnectedDevices();

    /**
     * Stops listening for mouse events and cleans up native resources.
     */
    void stopListening();

    /**
     * Retrieves the current absolute cursor position in screen coordinates.
     * 
     * @return An array of [x, y] in screen pixels
     */
    int[] getCursorPosition();
}
