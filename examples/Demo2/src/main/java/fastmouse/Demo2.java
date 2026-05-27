package fastmouse;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Demo2 - Two Undecorated Windows Side-by-Side
 * 
 * LEFT WINDOW (100, 100):  Swing draggable - drag to move window
 * RIGHT WINDOW (520, 100): FastMouse draggable - raw input, zero latency
 * 
 * Move mouse rapidly between windows to feel the latency difference.
 */
public class Demo2 {
    
    // LEFT: Swing window - standard mouse drag
    private JFrame swingFrame;
    private int swingDragX, swingDragY;
    private boolean swingDragging = false;
    
    // RIGHT: FastMouse window - raw input drag
    private JFrame fastFrame;
    private volatile int fastWindowX = 520, fastWindowY = 100;  // Current window position
    private volatile int fastDragStartMouseX, fastDragStartMouseY;  // Accumulated mouse at drag start
    private volatile int fastDragStartWindowX, fastDragStartWindowY;  // Window pos at drag start
    private volatile boolean fastDragging = false;
    
    public static void main(String[] args) {
        // Force 1.0 UI scale for consistent behavior across displays
        // System.setProperty("sun.java2d.uiScale", "1.0");
        // System.setProperty("sun.java2d.dpiaware", "true");
        
        SwingUtilities.invokeLater(() -> new Demo2().run());
    }
    
    private void run() {
        createSwingWindow();   // LEFT
        createFastWindow();    // RIGHT
        setupFastMouse();
        startRenderLoops();
    }
    
    private void createSwingWindow() {
        swingFrame = new JFrame("SWING - Drag Here");
        swingFrame.setUndecorated(true);
        swingFrame.setSize(400, 400);
        swingFrame.setLocation(100, 100);  // LEFT side
        swingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255, 220, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Standard Swing drag behavior
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                swingDragging = true;
                swingDragX = e.getXOnScreen() - swingFrame.getX();
                swingDragY = e.getYOnScreen() - swingFrame.getY();
                panel.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                swingDragging = false;
                panel.repaint();
            }
        });
        
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen() - swingDragX;
                int y = e.getYOnScreen() - swingDragY;
                swingFrame.setLocation(x, y);
            }
        });
        
        swingFrame.add(panel);
        swingFrame.setVisible(true);
    }
    
    private void createFastWindow() {
        fastFrame = new JFrame("FASTMOUSE - Drag Here");
        fastFrame.setUndecorated(true);
        fastFrame.setSize(400, 400);
        fastFrame.setLocation(520, 100);  // RIGHT side (400px gap)
        fastFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(100, 220, 255));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.BLUE);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        
        // Note: Dragging is handled by FastMouse raw input (left button)
        // This listener just provides visual feedback
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                panel.repaint();
            }
        });
        
        fastFrame.add(panel);
        fastFrame.setVisible(true);
    }
    
    private void setupFastMouse() {
        FastMouse mouse = FastMouse.open();
        
        mouse.startListening(new FastMouseListener() {
            private int accumulatedMouseX = 0, accumulatedMouseY = 0;
            
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                // Use raw deltas directly - no multiplier
                accumulatedMouseX += deltaX;
                accumulatedMouseY += deltaY;
                
                if (fastDragging) {
                    // Only move if mouse is over the FastMouse window
                    Rectangle windowBounds = fastFrame.getBounds();
                    
                    if (windowBounds.contains(absoluteX, absoluteY)) {
                        // 1:1 tracking: window moves exactly with mouse (like Swing)
                        int newX = fastDragStartWindowX + (accumulatedMouseX - fastDragStartMouseX);
                        int newY = fastDragStartWindowY + (accumulatedMouseY - fastDragStartMouseY);
                        
                        fastWindowX = newX;
                        fastWindowY = newY;
                        
                        // Update on EDT for thread safety
                        SwingUtilities.invokeLater(() -> {
                            fastFrame.setLocation(fastWindowX, fastWindowY);
                        });
                    }
                }
            }
            
            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                // Left button (0) controls dragging
                if (buttonId == 0) {
                    fastDragging = isPressed;
                    if (isPressed) {
                        // Capture positions at drag start for 1:1 tracking
                        fastDragStartMouseX = accumulatedMouseX;
                        fastDragStartMouseY = accumulatedMouseY;
                        fastDragStartWindowX = fastWindowX;
                        fastDragStartWindowY = fastWindowY;
                    }
                }
            }
            
            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                // Resize window
                int newSize = fastFrame.getWidth() + (delta > 0 ? 20 : -20);
                newSize = Math.max(200, Math.min(600, newSize));
                final int size = newSize;
                SwingUtilities.invokeLater(() -> {
                    fastFrame.setSize(size, size);
                });
            }
        });
    }
    
    private void startRenderLoops() {
        // 60 FPS repaint for both windows
        Timer timer = new Timer(16, e -> {
            swingFrame.repaint();
            fastFrame.repaint();
        });
        timer.start();
    }
}
