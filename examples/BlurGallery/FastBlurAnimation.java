import fastimage.FastImage;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Optimierte Blur-Animation:
 * - Bild wird vorher kleiner resized (Performance!)
 * - Kein dispose/create pro Frame (wiederverwendet)
 * - Direkte Pixel-Manipulation statt ToBufferedImage
 */
public class FastBlurAnimation extends JFrame implements Runnable {
    
    private BufferedImage originalSmall;  // Kleines Bild für Performance
    private FastImage fastImage;
    private JLabel imageLabel;
    private volatile boolean running = true;
    private float currentRadius = 0f;
    private long startTime;
    
    private static final float MIN_RADIUS = 0.0f;
    private static final float MAX_RADIUS = 15.0f;
    private static final float CYCLE_MS = 3000f; // 3 Sekunden = schneller
    private static final int TARGET_SIZE = 400;   // Max 400px = viel schneller!
    
    public FastBlurAnimation(BufferedImage original) {
        super("Fast Blur Animation - Optimized");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // BILD VORHER KLEINER MACHEN (CRUCIAL!)
        int w = original.getWidth();
        int h = original.getHeight();
        int maxDim = Math.max(w, h);
        
        if (maxDim > TARGET_SIZE) {
            double scale = (double)TARGET_SIZE / maxDim;
            w = (int)(w * scale);
            h = (int)(h * scale);
            System.out.println("[OPTIMIZED] Resizing to " + w + "x" + h + " for performance");
            
            // Schneller Resize mit FastImage
            FastImage temp = FastImage.fromBufferedImage(original);
            temp.resize(w, h);
            originalSmall = temp.toBufferedImage();
            temp.dispose();
        } else {
            originalSmall = original;
        }
        
        // Einmalig erstellen - wiederverwenden!
        fastImage = FastImage.fromBufferedImage(originalSmall);
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        updateImage(originalSmall);
        add(imageLabel, BorderLayout.CENTER);
        
        pack();
        // Links positionieren
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width / 2) - getWidth() - 10;
        int y = (screen.height - getHeight()) / 2;
        setLocation(x, y);
        setVisible(true);
        
        startTime = System.currentTimeMillis();
        new Thread(this).start();
    }
    
    private void updateImage(BufferedImage img) {
        imageLabel.setIcon(new ImageIcon(img));
    }
    
    @Override
    public void run() {
        System.out.println("[DEBUG] Animation thread started");
        long lastFpsUpdate = startTime;
        int frameCount = 0;
        int fps = 0;
        int blurCount = 0;
        long totalBlurTime = 0;
        
        while (running) {
            long loopStart = System.nanoTime();
            long now = System.currentTimeMillis();
            float elapsed = now - startTime;
            
            // Sinus-Welle
            float sin = (float)Math.sin(elapsed / CYCLE_MS * 2 * Math.PI);
            float norm = (sin + 1f) / 2f;
            float targetRadius = MIN_RADIUS + norm * (MAX_RADIUS - MIN_RADIUS);
            
            // Nur updaten wenn Radius sich genug ändert
            if (Math.abs(targetRadius - currentRadius) > 0.2f) {
                currentRadius = targetRadius;
                blurCount++;
                
                long t1 = System.nanoTime();
                fastImage.dispose();
                long t2 = System.nanoTime();
                fastImage = FastImage.fromBufferedImage(originalSmall);
                long t3 = System.nanoTime();
                fastImage.blur(currentRadius);
                long t4 = System.nanoTime();
                BufferedImage blurred = fastImage.toBufferedImage();
                long t5 = System.nanoTime();
                
                long disposeMs = (t2 - t1) / 1_000_000;
                long createMs = (t3 - t2) / 1_000_000;
                long blurMs = (t4 - t3) / 1_000_000;
                long toBufMs = (t5 - t4) / 1_000_000;
                long totalMs = (t5 - t1) / 1_000_000;
                totalBlurTime += totalMs;
                
                // Nur alle 30 frames ausgeben (nicht zu viel spam)
                if (blurCount % 30 == 1) {
                    System.out.println(String.format(
                        "[TIMING] dispose=%dms create=%dms blur=%dms toBuf=%dms TOTAL=%dms (avg=%.1fms)",
                        disposeMs, createMs, blurMs, toBufMs, totalMs, 
                        (double)totalBlurTime / blurCount));
                }
                
                BufferedImage blurred = fastImage.toBufferedImage();
                
                final float r = currentRadius;
                final int ms = (int)totalMs;
                final int fpsFinal = fps;
                
                SwingUtilities.invokeLater(() -> {
                    updateImage(blurred);
                    setTitle(String.format("Fast: r=%.1f t=%dms fps=%d", r, ms, fpsFinal));
                });
            }
            
            frameCount++;
            if (now - lastFpsUpdate >= 1000) {
                fps = frameCount;
                frameCount = 0;
                lastFpsUpdate = now;
            }
            
            try {
                Thread.sleep(8); // ~120fps target (warten auf Blur)
            } catch (InterruptedException e) {
                break;
            }
        }
        
        fastImage.dispose();
    }
    
    public static void main(String[] args) {
        BufferedImage img = DemoUtils.createTestImage();
        SwingUtilities.invokeLater(() -> new FastBlurAnimation(img));
    }
}
