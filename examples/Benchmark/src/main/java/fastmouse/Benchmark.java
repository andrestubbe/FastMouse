package fastmouse;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;

/**
 * Performance Benchmark comparing FastMouse to standard Java alternatives.
 */
public class Benchmark {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== FastMouse Benchmark ===");
        
        FastMouse mouse = FastMouse.open();
        
        int iterations = 10000;
        
        // 1. Benchmark Standard Java (simulated)
        long startJava = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Math.sqrt(i);
        }
        long javaTimeMs = (System.nanoTime() - startJava) / 1_000_000;
        
        // 2. Benchmark FastMouse Native (event count)
        final int[] eventCount = {0};
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                eventCount[0]++;
            }
            
            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                eventCount[0]++;
            }
            
            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                eventCount[0]++;
            }
        });
        
        System.out.println("Move your mouse rapidly for 3 seconds...");
        Thread.sleep(3000);
        mouse.stopListening();
        
        System.out.println("Iterations: " + iterations);
        System.out.println("Standard Java (simulated): " + javaTimeMs + " ms");
        System.out.println("FastMouse events captured: " + eventCount[0]);
        System.out.println("Events per second: " + (eventCount[0] / 3.0));
    }
}
