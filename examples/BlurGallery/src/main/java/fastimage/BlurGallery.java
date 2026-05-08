package fastimage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.concurrent.CompletableFuture;

/**
 * BlurGallery - Asynchronous Performance Comparison.
 * Zero-Stutter UI using background processing.
 */
public class BlurGallery extends JFrame {
    
    private BufferedImage original;
    private JLabel labelJava, labelFast, labelStats;
    private JSlider slider;
    private JComboBox<String> blurType;
    private Color accentColor = new Color(0, 150, 255);
    private boolean isProcessing = false;
    
    public BlurGallery() {
        setTitle("FastImage Performance Showcase - SIMD vs Java2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        getContentPane().setBackground(new Color(25, 25, 25));
        setLayout(new BorderLayout(0, 0));
        
        original = generateShowcaseImage(800, 600);
        
        // --- Top Header / Controls ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(35, 35, 35));
        top.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("NATIVE IMAGE ACCELERATION");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title, BorderLayout.NORTH);
        
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        ctrlRow.setOpaque(false);
        
        slider = new JSlider(0, 50, 0);
        slider.setPreferredSize(new Dimension(300, 40));
        slider.setBackground(new Color(35, 35, 35));
        slider.addChangeListener(e -> { if(!slider.getValueIsAdjusting()) startAsyncUpdate(); });
        ctrlRow.add(slider);
        
        blurType = new JComboBox<>(new String[]{"Gaussian (High Quality)", "Stack (UI Standard)", "Box (Fast)"});
        blurType.addActionListener(e -> startAsyncUpdate());
        ctrlRow.add(blurType);
        
        top.add(ctrlRow, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        
        // --- Center Comparison ---
        JPanel center = new JPanel(new GridLayout(1, 2, 2, 0));
        center.setBackground(new Color(15, 15, 15));
        
        labelJava = createDisplayLabel("Standard Java2D (CPU)");
        labelFast = createDisplayLabel("FastImage (SIMD / AVX2)");
        
        center.add(labelJava);
        center.add(labelFast);
        add(center, BorderLayout.CENTER);
        
        // --- Bottom Stats Bar ---
        labelStats = new JLabel("READY - SELECT FILTER AND RADIUS");
        labelStats.setForeground(accentColor);
        labelStats.setFont(new Font("Consolas", Font.BOLD, 16));
        labelStats.setHorizontalAlignment(SwingConstants.CENTER);
        labelStats.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(labelStats, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JLabel createDisplayLabel(String title) {
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.TOP);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setForeground(new Color(100, 100, 100));
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setIcon(new ImageIcon(original));
        l.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(40, 40, 40)));
        return l;
    }

    private void startAsyncUpdate() {
        if (isProcessing) return;
        
        int radius = slider.getValue();
        if (radius == 0) {
            labelJava.setIcon(new ImageIcon(original));
            labelFast.setIcon(new ImageIcon(original));
            labelStats.setText("ORIGINAL IMAGE (UNFILTERED)");
            return;
        }

        isProcessing = true;
        labelStats.setText("⚡ PROCESSING ON BACKGROUND THREAD...");
        labelStats.setForeground(Color.ORANGE);
        String type = (String)blurType.getSelectedItem();

        CompletableFuture.runAsync(() -> {
            try {
                // Java Bench
                long t1 = System.currentTimeMillis();
                BufferedImage resJava = applyJavaBlur(original, radius, type);
                long javaMs = System.currentTimeMillis() - t1;

                // FastImage Bench
                long t2 = System.currentTimeMillis();
                FastImage fi = FastImage.fromBufferedImage(original);
                if (type.contains("Gaussian")) fi.blurGaussian(radius);
                else if (type.contains("Stack")) fi.blurStack(radius);
                else fi.blurBox(radius);
                BufferedImage resFast = fi.toBufferedImage();
                fi.dispose();
                long fastMs = System.currentTimeMillis() - t2;

                // Back to UI Thread
                SwingUtilities.invokeLater(() -> {
                    labelJava.setIcon(new ImageIcon(resJava));
                    labelFast.setIcon(new ImageIcon(resFast));
                    double speedup = (double)javaMs / Math.max(1, fastMs);
                    labelStats.setText(String.format("JAVA: %d ms   |   FASTIMAGE: %d ms   |   ⚡ SPEEDUP: %.1f X", 
                        javaMs, fastMs, speedup));
                    labelStats.setForeground(speedup > 5.0 ? Color.GREEN : accentColor);
                    isProcessing = false;
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                isProcessing = false;
            }
        });
    }
    
    private BufferedImage applyJavaBlur(BufferedImage src, int radius, String type) {
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        if (type.contains("Gaussian")) {
            float sigma = radius / 3.0f;
            float twoSigmaSq = 2.0f * sigma * sigma;
            float sum = 0.0f;
            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    float v = (float) Math.exp(-(x * x + y * y) / twoSigmaSq);
                    data[(y + radius) * size + (x + radius)] = v;
                    sum += v;
                }
            }
            for (int i = 0; i < data.length; i++) data[i] /= sum;
        } else {
            for (int i = 0; i < data.length; i++) data[i] = 1.0f / (size * size);
        }
        return new ConvolveOp(new Kernel(size, size, data), ConvolveOp.EDGE_NO_OP, null).filter(src, null);
    }
    
    private BufferedImage generateShowcaseImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20, 20, 20)); g.fillRect(0, 0, w, h);
        g.setColor(new Color(40, 40, 50));
        for (int i = 0; i < w; i += 50) g.drawLine(i, 0, i, h);
        for (int i = 0; i < h; i += 50) g.drawLine(0, i, w, i);
        g.setColor(new Color(255, 60, 60)); g.fillOval(150, 100, 300, 300);
        g.setFont(new Font("Arial Black", Font.PLAIN, 100));
        g.setColor(Color.WHITE); g.drawString("SIMD", 150, 450);
        g.dispose();
        return img;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlurGallery().setVisible(true));
    }
}
