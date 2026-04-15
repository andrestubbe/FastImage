package fastimage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * VisualEditor - Interaktiver Split-Screen Editor
 * 
 * Links: Original Bild
 * Rechts: Bearbeitetes Bild (FastImage)
 * 
 * Controls:
 * - Resize Slider
 * - Blur Radius
 * - Grayscale Toggle
 * - Brightness/Contrast
 */
public class VisualEditor extends JFrame {
    
    private BufferedImage original;
    private JLabel originalLabel, resultLabel, statusLabel;
    
    // Controls
    private JCheckBox cbResize, cbBlur, cbGrayscale;
    private JSlider sliderResize, sliderBlur, sliderBrightness, sliderContrast;
    private JButton btnReset, btnApply;
    
    public VisualEditor() {
        setTitle("FastImage Visual Editor - Live Preview");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Generate colorful test image
        original = generateTestImage(800, 600);
        
        // Left panel - image comparison
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        originalLabel = new JLabel("Original", SwingConstants.CENTER);
        originalLabel.setBorder(BorderFactory.createTitledBorder("Original (BufferedImage)"));
        originalLabel.setIcon(new ImageIcon(original));
        
        resultLabel = new JLabel("Result", SwingConstants.CENTER);
        resultLabel.setBorder(BorderFactory.createTitledBorder("FastImage Result (SIMD Accelerated)"));
        resultLabel.setIcon(new ImageIcon(original));
        
        imagePanel.add(originalLabel);
        imagePanel.add(resultLabel);
        
        add(imagePanel, BorderLayout.CENTER);
        
        // Right controls panel
        JPanel controls = new JPanel(new GridLayout(8, 1, 5, 5));
        controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controls.setPreferredSize(new Dimension(300, 0));
        
        // Resize
        JPanel resizePanel = new JPanel(new BorderLayout());
        cbResize = new JCheckBox("Resize (50-100%)");
        sliderResize = new JSlider(50, 100, 100);
        resizePanel.add(cbResize, BorderLayout.WEST);
        resizePanel.add(sliderResize, BorderLayout.CENTER);
        controls.add(resizePanel);
        
        // Blur
        JPanel blurPanel = new JPanel(new BorderLayout());
        cbBlur = new JCheckBox("Blur (0-10px)");
        sliderBlur = new JSlider(0, 10, 0);
        blurPanel.add(cbBlur, BorderLayout.WEST);
        blurPanel.add(sliderBlur, BorderLayout.CENTER);
        controls.add(blurPanel);
        
        // Grayscale
        cbGrayscale = new JCheckBox("Grayscale");
        controls.add(cbGrayscale);
        
        // Brightness
        JPanel brightPanel = new JPanel(new BorderLayout());
        brightPanel.add(new JLabel("Brightness: "), BorderLayout.WEST);
        sliderBrightness = new JSlider(50, 150, 100);
        brightPanel.add(sliderBrightness, BorderLayout.CENTER);
        controls.add(brightPanel);
        
        // Contrast
        JPanel contrastPanel = new JPanel(new BorderLayout());
        contrastPanel.add(new JLabel("Contrast: "), BorderLayout.WEST);
        sliderContrast = new JSlider(50, 150, 100);
        contrastPanel.add(sliderContrast, BorderLayout.CENTER);
        controls.add(contrastPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnReset = new JButton("🔄 Reset");
        btnApply = new JButton("⚡ Apply FastImage");
        btnReset.addActionListener(e -> resetControls());
        btnApply.addActionListener(e -> applyEffects());
        buttonPanel.add(btnReset);
        buttonPanel.add(btnApply);
        controls.add(buttonPanel);
        
        // Status
        statusLabel = new JLabel("Click 'Apply' to process with FastImage SIMD");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        controls.add(statusLabel);
        
        add(controls, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void applyEffects() {
        long start = System.nanoTime();
        
        FastImage img = FastImage.fromBufferedImage(original);
        
        // Apply enabled effects
        if (cbResize.isSelected()) {
            int percent = sliderResize.getValue();
            int w = original.getWidth() * percent / 100;
            int h = original.getHeight() * percent / 100;
            img.resize(w, h);
        }
        
        if (cbBlur.isSelected() && sliderBlur.getValue() > 0) {
            img.blur(sliderBlur.getValue());
        }
        
        if (cbGrayscale.isSelected()) {
            img.grayscale();
        }
        
        float brightness = sliderBrightness.getValue() / 100f;
        float contrast = sliderContrast.getValue() / 100f;
        
        if (brightness != 1.0f) {
            img.adjustBrightness(brightness);
        }
        
        if (contrast != 1.0f) {
            img.adjustContrast(contrast);
        }
        
        BufferedImage result = img.toBufferedImage();
        img.dispose();
        
        long time = (System.nanoTime() - start) / 1_000_000;
        
        resultLabel.setIcon(new ImageIcon(result));
        statusLabel.setText(String.format("✅ Processed in %d ms (SIMD accelerated)", time));
    }
    
    private void resetControls() {
        cbResize.setSelected(false);
        cbBlur.setSelected(false);
        cbGrayscale.setSelected(false);
        sliderResize.setValue(100);
        sliderBlur.setValue(0);
        sliderBrightness.setValue(100);
        sliderContrast.setValue(100);
        resultLabel.setIcon(new ImageIcon(original));
        statusLabel.setText("Reset - click 'Apply' to process");
    }
    
    private BufferedImage generateTestImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Rainbow gradient background
        for (int x = 0; x < w; x++) {
            float hue = (float) x / w;
            g.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
            g.fillRect(x, 0, 1, h);
        }
        
        // Sharp geometric shapes (test for blur/resize quality)
        g.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            int x = (i * w / 5) + 40;
            int y = h / 4;
            int size = 80;
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
            g.setColor(Color.WHITE);
        }
        
        // Text (sharpness test)
        g.setFont(new Font("Arial", Font.BOLD, 72));
        g.setColor(Color.YELLOW);
        g.drawString("FASTIMAGE", w/4, h/2);
        
        // Fine detail (noise pattern)
        g.setColor(new Color(255, 255, 255, 100));
        for (int i = 0; i < 200; i++) {
            int x = (int)(Math.random() * w);
            int y = (int)(Math.random() * h);
            g.fillRect(x, y, 2, 2);
        }
        
        g.dispose();
        return img;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VisualEditor().setVisible(true);
        });
    }
}
