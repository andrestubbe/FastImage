package fastimage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * BlurGallery - Demonstration aller nativen Blur-Algorithmen.
 */
public class BlurGallery extends JFrame {
    
    private BufferedImage originalImage;
    private JLabel javaLabel, fastLabel, infoLabel;
    private JSlider radiusSlider;
    private JLabel radiusLabel;
    private JComboBox<String> blurType;
    
    public BlurGallery() {
        setTitle("FastImage Blur Gallery - SIMD Accelerated");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Generate test image
        originalImage = generateDetailedImage(800, 600);
        
        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        
        radiusLabel = new JLabel("Radius: 0px");
        radiusSlider = new JSlider(0, 30, 0);
        radiusSlider.addChangeListener(this::onRadiusChanged);
        
        blurType = new JComboBox<>(new String[]{"Gaussian (O(N))", "Stack (iOS Style)", "Box (Fast)"});
        blurType.addActionListener(e -> onRadiusChanged(null));

        controls.add(new JLabel("Type:"));
        controls.add(blurType);
        controls.add(radiusLabel);
        controls.add(radiusSlider);
        
        add(controls, BorderLayout.NORTH);
        
        // Image panel
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        javaLabel = new JLabel("Java2D", SwingConstants.CENTER);
        javaLabel.setBorder(BorderFactory.createTitledBorder("Standard Java (ConvolveOp)"));
        javaLabel.setIcon(new ImageIcon(originalImage));
        
        fastLabel = new JLabel("FastImage", SwingConstants.CENTER);
        fastLabel.setBorder(BorderFactory.createTitledBorder("FastImage (Native SIMD)"));
        fastLabel.setIcon(new ImageIcon(originalImage));
        
        imagePanel.add(javaLabel);
        imagePanel.add(fastLabel);
        
        add(imagePanel, BorderLayout.CENTER);
        
        infoLabel = new JLabel(" Adjust slider to see SIMD performance advantage");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        add(infoLabel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void onRadiusChanged(ChangeEvent e) {
        if (radiusSlider.getValueIsAdjusting()) return;
        
        int radius = radiusSlider.getValue();
        radiusLabel.setText("Radius: " + radius + "px");
        
        if (radius == 0) {
            javaLabel.setIcon(new ImageIcon(originalImage));
            fastLabel.setIcon(new ImageIcon(originalImage));
            return;
        }
        
        // Java blur (always box for comparison)
        long javaStart = System.currentTimeMillis();
        BufferedImage javaBlurred = blurJava(originalImage, radius);
        long javaTime = System.currentTimeMillis() - javaStart;
        
        // FastImage blur
        long fastStart = System.currentTimeMillis();
        FastImage fastImg = FastImage.fromBufferedImage(originalImage);
        
        String type = (String)blurType.getSelectedItem();
        if (type.contains("Gaussian")) fastImg.blurGaussian(radius);
        else if (type.contains("Stack")) fastImg.blurStack(radius);
        else fastImg.blurBox(radius);
        
        BufferedImage fastBlurred = fastImg.toBufferedImage();
        fastImg.dispose();
        long fastTime = System.currentTimeMillis() - fastStart;
        
        javaLabel.setIcon(new ImageIcon(javaBlurred));
        fastLabel.setIcon(new ImageIcon(fastBlurred));
        
        infoLabel.setText(String.format("  Java: %dms | FastImage: %dms | Speedup: %.1fx", 
            javaTime, fastTime, (double)javaTime / Math.max(1, fastTime)));
    }
    
    private BufferedImage blurJava(BufferedImage src, int radius) {
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        for (int i = 0; i < data.length; i++) data[i] = 1.0f / (size * size);
        return new ConvolveOp(new Kernel(size, size, data), ConvolveOp.EDGE_NO_OP, null).filter(src, null);
    }
    
    private BufferedImage generateDetailedImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < w; i += 20) g.drawLine(i, 0, i, h);
        for (int i = 0; i < h; i += 20) g.drawLine(0, i, w, i);
        g.setColor(Color.RED); g.fillOval(100, 100, 200, 200);
        g.setColor(Color.BLUE); g.fillRect(400, 150, 250, 200);
        g.setFont(new Font("Arial", Font.BOLD, 80));
        g.setColor(Color.BLACK); g.drawString("SIMD POWER", 150, 500);
        g.dispose();
        return img;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlurGallery().setVisible(true));
    }
}
