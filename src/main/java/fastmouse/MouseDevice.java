package fastmouse;

/**
 * Represents a physical mouse device.
 */
public class MouseDevice {
    private final long handle;
    private final String name;
    private final int buttonCount;

    public MouseDevice(long handle, String name, int buttonCount) {
        this.handle = handle;
        this.name = name;
        this.buttonCount = buttonCount;
    }

    /**
     * @return The native device handle (used to identify which mouse sent an event)
     */
    public long getHandle() {
        return handle;
    }

    /**
     * @return The device name as reported by Windows
     */
    public String getName() {
        return name;
    }

    /**
     * @return Number of buttons on this mouse
     */
    public int getButtonCount() {
        return buttonCount;
    }

    @Override
    public String toString() {
        return "MouseDevice{" +
                "handle=" + handle +
                ", name='" + name + '\'' +
                ", buttonCount=" + buttonCount +
                '}';
    }
}
