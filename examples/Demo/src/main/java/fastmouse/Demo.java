package fastmouse;

import java.util.List;

/**
 * FastMouse Demo — Demonstrates high-precision raw input deltas, hardware handles,
 * and window-focused client coordinates vs. global desktop capture.
 */
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==============================================");
        System.out.println("               ⚡ FastMouse Demo ⚡            ");
        System.out.println("==============================================");

        try (FastMouse mouse = FastMouse.open()) {
            // Show connected devices
            System.out.println("\n\033[90m[SYSTEM]\033[0m \033[97mEnumerating Mouse Hardware Devices...\033[0m");
            List<MouseDevice> devices = mouse.getConnectedDevices();
            for (MouseDevice device : devices) {
                System.out.printf(" \033[90m->\033[0m \033[97m%-24s\033[0m \033[90m(Handle: \033[97m%d\033[90m, Buttons: \033[97m%d\033[90m)\033[0m\n",
                    device.getName(), device.getHandle(), device.getButtonCount());
            }

            // Resolve current console window handle natively via FastMouse
            long consoleHwnd = FastMouse.getConsoleWindow();

            // Start in window-bound mode if console handle found, else global
            if (consoleHwnd != 0) {
                mouse.bindToWindow(consoleHwnd);
                System.out.println("\n\033[96m>>> [MODE] WINDOW-BOUND (Focus & Local Client-Relative Pixels) <<<\033[0m");
            } else {
                System.out.println("\n\033[93m>>> [MODE] GLOBAL DESKTOP CAPTURE <<<\033[0m");
            }

            System.out.println(">>> Commands: Press [B] to toggle Window Binding (Active Window vs. Global)");
            System.out.println("              Press [ESC] to exit\n");

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

            // Instant hardware key poller: No Enter key needed, no dirty characters in stdout!
            Thread inputThread = new Thread(() -> {
                boolean bWasPressed = false;
                while (mouse.isListening()) {
                    boolean bIsPressed = FastMouse.isKeyPressed(0x42); // VK_B
                    if (bIsPressed && !bWasPressed) {
                        if (mouse.isWindowBound()) {
                            mouse.unbindFromWindow();
                            System.out.println("\n\033[93m>>> [MODE CHANGED] GLOBAL DESKTOP CAPTURE <<<\033[0m\n");
                        } else if (consoleHwnd != 0) {
                            mouse.bindToWindow(consoleHwnd);
                            System.out.println("\n\033[96m>>> [MODE CHANGED] WINDOW-BOUND (Active Terminal: 0x" + Long.toHexString(consoleHwnd).toUpperCase() + ") <<<\033[0m\n");
                        }
                    }
                    bWasPressed = bIsPressed;

                    if (FastMouse.isKeyPressed(0x1B)) { // VK_ESCAPE
                        System.out.println("\nExiting FastMouse Demo...");
                        System.exit(0);
                    }

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            });
            inputThread.setDaemon(true);
            inputThread.start();

            while (mouse.isListening()) {
                Thread.sleep(500);
            }
        }
    }
}
