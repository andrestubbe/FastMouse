package fastmouse;

import java.util.List;

/**
 * FastMouse Demo — Demonstrates high-precision raw input deltas, hardware handles,
 * and window-focused client coordinates vs. global desktop capture.
 */
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==============================================");
        System.out.println("   ⚡ FastMouse Native Demo (FastJava Standard) ⚡");
        System.out.println("==============================================");

        try (FastMouse mouse = FastMouse.open()) {
            // Show connected devices
            System.out.println("\n\033[90m[SYSTEM]\033[0m \033[97mEnumerating Mouse Hardware Devices...\033[0m");
            List<MouseDevice> devices = mouse.getConnectedDevices();
            for (MouseDevice device : devices) {
                System.out.printf(" \033[90m->\033[0m \033[97m%-24s\033[0m \033[90m(Handle: \033[97m%d\033[90m, Buttons: \033[97m%d\033[90m)\033[0m\n",
                    device.getName(), device.getHandle(), device.getButtonCount());
            }

            // Get console window handle if available
            long consoleHwnd = 0;
            try {
                consoleHwnd = (long) Class.forName("com.sun.jna.platform.win32.Kernel32")
                        .getMethod("GetConsoleWindow").invoke(null);
            } catch (Throwable ignored) {
            }

            // Start in window-bound mode if console handle found, else global
            if (consoleHwnd != 0) {
                mouse.bindToWindow(consoleHwnd);
                System.out.println("\n\033[96m>>> [MODE] WINDOW-BOUND (Focus & Local Client-Relative Pixels) <<<\033[0m");
            } else {
                System.out.println("\n\033[93m>>> [MODE] GLOBAL DESKTOP CAPTURE <<<\033[0m");
            }

            System.out.println("\033[90mListening for high-speed raw mouse events (Move, Click, Wheel)... Press Ctrl+C to exit.\033[0m\n");

            mouse.startListening(new FastMouseListener() {
                @Override
                public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                    String coordLabel = mouse.isWindowBound() ? "Client" : "Screen";
                    System.out.printf("\033[90m[MOVE]\033[0m   \033[90mDev:\033[0m\033[97m%d\033[0m \033[90m|\033[0m \033[90mDelta:\033[0m (\033[97m%+4d\033[0m, \033[97m%+4d\033[0m) \033[90m|\033[0m \033[90m%s:\033[0m (\033[97m%4d\033[0m, \033[97m%4d\033[0m)\n",
                        deviceHandle, deltaX, deltaY, coordLabel, absoluteX, absoluteY);
                }

                @Override
                public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                    String state = isPressed ? "\033[92mPRESSED \033[0m" : "\033[90mRELEASED\033[0m";
                    String name = switch (buttonId) {
                        case 0 -> "Left";
                        case 1 -> "Right";
                        case 2 -> "Middle";
                        case 3 -> "Button 4";
                        case 4 -> "Button 5";
                        default -> "Button " + buttonId;
                    };
                    System.out.printf("\033[93m[BUTTON]\033[0m \033[90mDev:\033[0m\033[97m%d\033[0m \033[90m|\033[0m \033[90mButton:\033[0m \033[97m%-8s\033[0m \033[90m|\033[0m %s\n",
                        deviceHandle, name, state);
                }

                @Override
                public void onMouseWheel(long deviceHandle, int delta) {
                    System.out.printf("\033[95m[WHEEL]\033[0m  \033[90mDev:\033[0m\033[97m%d\033[0m \033[90m|\033[0m \033[90mScroll Delta:\033[0m \033[97m%+d\033[0m\n",
                        deviceHandle, delta);
                }
            });

            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
