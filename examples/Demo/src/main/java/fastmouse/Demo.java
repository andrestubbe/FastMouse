package fastmouse;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastmouse.MouseDevice;

/**
 * Demo for FastMouse - Raw Input Mouse API
 */
public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== FastMouse Demo ===");
        
        FastMouse mouse = FastMouse.open();
        
        // Show connected devices
        System.out.println("\nConnected Mouse Devices:");
        for (MouseDevice device : mouse.getConnectedDevices()) {
            System.out.println("  - " + device.getName() + " (Handle: " + device.getHandle() + ", Buttons: " + device.getButtonCount() + ")");
        }
        
        // Start listening for mouse events
        System.out.println("\nListening for mouse events (move your mouse, press buttons, scroll wheel)...");
        System.out.println("Press Ctrl+C to stop.\n");
        
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                System.out.println("[MOVE] Device: " + deviceHandle + " | Delta: (" + deltaX + ", " + deltaY + ") | Absolute: (" + absoluteX + ", " + absoluteY + ")");
            }
            
            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                System.out.println("[BUTTON] Device: " + deviceHandle + " | Button: " + buttonId + " | " + (isPressed ? "PRESSED" : "RELEASED"));
            }
            
            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                System.out.println("[WHEEL] Device: " + deviceHandle + " | Delta: " + delta);
            }
        });
        
        // Keep running
        Thread.sleep(Long.MAX_VALUE);
    }
}
