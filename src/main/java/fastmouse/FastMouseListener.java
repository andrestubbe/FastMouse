package fastmouse;

/**
 * Listener interface for receiving raw mouse events via Win32 Raw Input API.
 * All callbacks are invoked from a native background thread.
 */
public interface FastMouseListener {

    /**
     * Called when the mouse moves.
     * @param deviceHandle The hardware handle of the mouse device
     * @param deltaX Raw horizontal delta from mouse sensor (bypasses Windows ballistics)
     * @param deltaY Raw vertical delta from mouse sensor (bypasses Windows ballistics)
     * @param absoluteX Current absolute horizontal cursor position in screen pixels
     * @param absoluteY Current absolute vertical cursor position in screen pixels
     */
    void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY);

    /**
     * Called when a mouse button is pressed or released.
     * @param deviceHandle The hardware handle of the mouse device
     * @param buttonId Button identifier (0=left, 1=right, 2=middle, 3=X1, 4=X2, etc.)
     * @param isPressed true if button was pressed, false if released
     */
    void onMouseButton(long deviceHandle, int buttonId, boolean isPressed);

    /**
     * Called when the mouse wheel is scrolled.
     * @param deviceHandle The hardware handle of the mouse device
     * @param delta Wheel delta (positive = scroll up/away, negative = scroll down/toward)
     */
    void onMouseWheel(long deviceHandle, int delta);
}
