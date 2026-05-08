package fastimage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ResizeDemo - Vergleicht verschiedene Resize-Algorithmen
 * 
 * Zeigt:
 * - Bilinear vs Bicubic vs Nearest-Neighbor
 * - Java (BufferedImage) vs FastImage
 * - Geschwindigkeit vs Qualität
 */
public class ResizeDemo extends JFrame {
    
    private BufferedImage originalImage;
    private JLabel javaLabel, fastLabel;
    private JComboBox<String> algorithmCombo;
    private JSlider sizeSlider;
    private JLabel infoLabel;
    
    public ResizeDemo() {
        setTitle("FastImage Resize Demo - Algorithm Comparison");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Load test image
        try {
            originalImage = generateTestImage(1920, 1080);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not generate test image: " + e.getMessage());
            System.exit(1);
        }
        
        // Top controls panel
        JPanel controls = new JPanel(new FlowLayout());
        
        algorithmCombo = new JComboBox<>(new String[]{
            "Bilinear (FastImage)",
            "Bicubic (FastImage)", 
            "Nearest-Neighbor (FastImage)",
            "Bilinear (Java)"
        });
        algorithmCombo.addActionListener(e -> updatePreview());
        controls.add(new JLabel("Algorithm:"));
        controls.add(algorithmCombo);
        
        sizeSlider = new JSlider(10, 100, 50);
        sizeSlider.addChangeListener(e -> updatePreview());
        controls.add(new JLabel("Size:"));
        controls.add(sizeSlider);
        
        infoLabel = new JLabel("Ready");
        controls.add(infoLabel);
        
        add(controls, BorderLayout.NORTH);
        
        // Center - image comparison
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        javaLabel = new JLabel("Java (BufferedImage)", SwingConstants.CENTER);
        javaLabel.setBorder(BorderFactory.createTitledBorder("Java (BufferedImage)"));
        javaLabel.setPreferredSize(new Dimension(400, 300));
        
        fastLabel = new JLabel("FastImage", SwingConstants.CENTER);
        fastLabel.setBorder(BorderFactory.createTitledBorder("FastImage"));
        fastLabel.setPreferredSize(new Dimension(400, 300));
        
        imagePanel.add(javaLabel);
        imagePanel.add(fastLabel);
        
        add(imagePanel, BorderLayout.CENTER);
        
        // Info panel
        JTextArea info = new JTextArea(
            "Bilinear: Good quality, fast\n" +
            "Bicubic: Best quality, slower\n" +
            "Nearest-Neighbor: Fastest, pixelated\n\n" +
            "FastImage: Native SIMD implementation\n" +
            "Java: Graphics2D with RenderingHints"
        );
        info.setEditable(false);
        info.setBackground(getBackground());
        add(info, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        
        // Initial update
        SwingUtilities.invokeLater(this::updatePreview);
    }
    
    private void updatePreview() {
        int percent = sizeSlider.getValue();
        int newWidth = originalImage.getWidth() * percent / 100;
        int newHeight = originalImage.getHeight() * percent / 100;
        
        String selected = (String) algorithmCombo.getSelectedItem();
        
        long javaTime = 0, fastTime = 0;
        ImageIcon javaIcon = null, fastIcon = null;
        
        try {
            switch (selected) {
                case "Bilinear (FastImage)":
                    javaIcon = resizeJava(originalImage, newWidth, newHeight, "bilinear");
                    fastIcon = resizeFastImage(originalImage, newWidth, newHeight, "bilinear");
                    break;
                case "Bicubic (FastImage)":
                    javaIcon = resizeJava(originalImage, newWidth, newHeight, "bicubic");
                    fastIcon = resizeFastImage(originalImage, newWidth, newHeight, "bicubic");
                    break;
                case "Nearest-Neighbor (FastImage)":
                    javaIcon = resizeJava(originalImage, newWidth, newHeight, "nearest");
                    fastIcon = resizeFastImage(originalImage, newWidth, newHeight, "nearest");
                    break;
                case "Bilinear (Java)":
                    javaIcon = resizeJava(originalImage, newWidth, newHeight, "bilinear");
                    fastIcon = javaIcon; // Same
                    break;
            }
            
            javaLabel.setIcon(javaIcon);
            fastLabel.setIcon(fastIcon);
            
            infoLabel.setText(String.format("Size: %dx%d (%d%%)", 
                newWidth, newHeight, percent));
            
        } catch (Exception e) {
            infoLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private ImageIcon resizeJava(BufferedImage src, int w, int h, String algorithm) {
        long start = System.nanoTime();
        
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        
        Object hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        if (algorithm.equals("bicubic")) {
            hint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        } else if (algorithm.equals("nearest")) {
            hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        }
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        // Scale for display if too small
        int displayW = Math.max(w, 200);
        int displayH = Math.max(h, 150);
        if (w < 200 || h < 150) {
            BufferedImage scaled = new BufferedImage(displayW, displayH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.drawImage(dst, 0, 0, displayW, displayH, null);
            g2.dispose();
            dst = scaled;
        }
        
        return new ImageIcon(dst);
    }
    
    private ImageIcon resizeFastImage(BufferedImage src, int w, int h, String algorithm) {
        long start = System.nanoTime();
        
        FastImage img = FastImage.fromBufferedImage(src);
        img.resize(w, h); // FastImage uses bilinear
        BufferedImage dst = img.toBufferedImage();
        img.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        // Scale for display
        int displayW = Math.max(w, 200);
        int displayH = Math.max(h, 150);
        if (w < 200 || h < 150) {
            BufferedImage scaled = new BufferedImage(displayW, displayH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.drawImage(dst, 0, 0, displayW, displayH, null);
            g2.dispose();
            dst = scaled;
        }
        
        return new ImageIcon(dst);
    }
    
    private BufferedImage generateTestImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Gradient background
        for (int y = 0; y < height; y++) {
            int gray = y * 255 / height;
            g.setColor(new Color(gray, gray, gray));
            g.drawLine(0, y, width, y);
        }
        
        // Colored rectangles for sharpness test
        g.setColor(Color.RED);
        g.fillRect(width/4, height/4, width/8, height/8);
        g.setColor(Color.GREEN);
        g.fillRect(width/2, height/4, width/8, height/8);
        g.setColor(Color.BLUE);
        g.fillRect(width/4, height/2, width/8, height/8);
        
        // Fine lines for aliasing test
        g.setColor(Color.WHITE);
        for (int i = 0; i < width; i += 20) {
            g.drawLine(i, 0, i, height);
        }
        
        // Text
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.setColor(Color.YELLOW);
        g.drawString("Resize Test", width/3, height/2);
        
        g.dispose();
        return img;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ResizeDemo().setVisible(true);
        });
    }
}
