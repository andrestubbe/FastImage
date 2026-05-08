package fastimage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ChainDemo - Zeigt die Fluent API Chain-Operationen
 * 
 * Demonstration: .resize().blur().grayscale().brightness()
 */
public class ChainDemo extends JFrame {
    
    private BufferedImage original;
    private JLabel originalLabel, resultLabel;
    private JTextArea codeArea, infoArea;
    
    public ChainDemo() {
        setTitle("FastImage Chain API Demo - Fluent Operations");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Generate colorful test image
        original = generateColorfulImage(600, 450);
        
        // Left panel - controls
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 0));
        
        // Code display
        codeArea = new JTextArea(
            "// FastImage Chain API\n" +
            "FastImage img = FastImage\n" +
            "    .fromBufferedImage(source)\n" +
            "    .resize(400, 300)\n" +
            "    .blur(2.5f)\n" +
            "    .grayscale()\n" +
            "    .brightness(1.2f);\n\n" +
            "// Alles in einem Durchlauf!"
        );
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        codeArea.setEditable(false);
        codeArea.setBackground(new Color(40, 40, 40));
        codeArea.setForeground(Color.GREEN);
        leftPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);
        
        // Buttons for different chains
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnInstagram = new JButton("📸 Instagram Filter");
        btnInstagram.addActionListener(e -> applyInstagramFilter());
        
        JButton btnVintage = new JButton("🎞️ Vintage Look");
        btnVintage.addActionListener(e -> applyVintageLook());
        
        JButton btnPortrait = new JButton("👤 Portrait Enhancement");
        btnPortrait.addActionListener(e -> applyPortraitEnhance());
        
        JButton btnReset = new JButton("🔄 Reset Original");
        btnReset.addActionListener(e -> resetImage());
        
        buttonPanel.add(btnInstagram);
        buttonPanel.add(btnVintage);
        buttonPanel.add(btnPortrait);
        buttonPanel.add(btnReset);
        
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        
        // Right panel - image comparison
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        
        originalLabel = new JLabel("Original", SwingConstants.CENTER);
        originalLabel.setBorder(BorderFactory.createTitledBorder("Original"));
        originalLabel.setIcon(new ImageIcon(original));
        
        resultLabel = new JLabel("Result (Click a filter)", SwingConstants.CENTER);
        resultLabel.setBorder(BorderFactory.createTitledBorder("Chain Result"));
        
        rightPanel.add(originalLabel);
        rightPanel.add(resultLabel);
        
        add(rightPanel, BorderLayout.CENTER);
        
        // Info area
        infoArea = new JTextArea(
            "Select a filter chain above to see the Fluent API in action.\n\n" +
            "Each chain executes multiple operations in optimized native code."
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        add(infoArea, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void applyInstagramFilter() {
        long start = System.nanoTime();
        
        FastImage img = FastImage.fromBufferedImage(original);
        img.resize(400, 300);
        img.adjustBrightness(1.1f);
        img.adjustContrast(1.05f);
        // img.sepia(); // TODO: implement
        img.blur(0.5f); // Subtle glow
        
        BufferedImage result = img.toBufferedImage();
        img.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        resultLabel.setIcon(new ImageIcon(result));
        codeArea.setText(
            "// Instagram Filter Chain\n" +
            "FastImage img = FastImage\n" +
            "    .fromBufferedImage(source)\n" +
            "    .resize(400, 300)\n" +
            "    .brightness(1.1f)\n" +
            "    .contrast(1.05f)\n" +
            "    .blur(0.5f);  // Subtle glow\n\n" +
            "Result: " + time + "ms (native SIMD)"
        );
        infoArea.setText("Instagram: Brightness + Contrast + Glow (Sepia coming in v1.1)");
    }
    
    private void applyVintageLook() {
        long start = System.nanoTime();
        
        FastImage img = FastImage.fromBufferedImage(original);
        img.resize(400, 300);
        img.grayscale();
        img.adjustBrightness(0.9f); // Darker
        img.adjustContrast(0.8f);     // Softer
        img.blur(0.3f);               // Film softness
        
        BufferedImage result = img.toBufferedImage();
        img.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        resultLabel.setIcon(new ImageIcon(result));
        codeArea.setText(
            "// Vintage Film Chain\n" +
            "FastImage img = FastImage\n" +
            "    .fromBufferedImage(source)\n" +
            "    .resize(400, 300)\n" +
            "    .grayscale()\n" +
            "    .brightness(0.9f)\n" +
            "    .contrast(0.8f)\n" +
            "    .blur(0.3f);  // Film softness\n\n" +
            "Result: " + time + "ms (native SIMD)"
        );
        infoArea.setText("Vintage: Grayscale + Lower brightness/contrast + Soft blur");
    }
    
    private void applyPortraitEnhance() {
        long start = System.nanoTime();
        
        FastImage img = FastImage.fromBufferedImage(original);
        img.resize(400, 300);
        img.adjustBrightness(1.05f);  // Slightly brighter
        img.adjustContrast(1.1f);     // More contrast
        // img.sharpen(0.3f); // TODO: implement
        
        BufferedImage result = img.toBufferedImage();
        img.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        resultLabel.setIcon(new ImageIcon(result));
        codeArea.setText(
            "// Portrait Enhancement\n" +
            "FastImage img = FastImage\n" +
            "    .fromBufferedImage(source)\n" +
            "    .resize(400, 300)\n" +
            "    .brightness(1.05f)\n" +
            "    .contrast(1.1f);\n" +
            "    // .sharpen(0.3f);  // Coming v1.1\n\n" +
            "Result: " + time + "ms (native SIMD)"
        );
        infoArea.setText("Portrait: Subtle brightness + contrast boost (Sharpen coming in v1.1)");
    }
    
    private void resetImage() {
        resultLabel.setIcon(null);
        resultLabel.setText("Click a filter chain");
        codeArea.setText(
            "// FastImage Chain API\n" +
            "FastImage img = FastImage\n" +
            "    .fromBufferedImage(source)\n" +
            "    .resize(400, 300)\n" +
            "    .blur(2.5f)\n" +
            "    .grayscale()\n" +
            "    .brightness(1.2f);\n\n" +
            "// Alles in einem Durchlauf!"
        );
        infoArea.setText("Select a filter chain above to see the Fluent API in action.");
    }
    
    private BufferedImage generateColorfulImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Rainbow gradient
        for (int x = 0; x < w; x++) {
            float hue = (float) x / w;
            g.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
            g.fillRect(x, 0, 1, h);
        }
        
        // Geometric shapes
        g.setColor(Color.WHITE);
        for (int i = 0; i < 8; i++) {
            int x = (int) (Math.random() * w * 0.8) + w/10;
            int y = (int) (Math.random() * h * 0.8) + h/10;
            int size = 30 + (int)(Math.random() * 50);
            g.fillOval(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, size, size);
            g.setColor(Color.WHITE);
        }
        
        g.dispose();
        return img;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChainDemo().setVisible(true);
        });
    }
}
