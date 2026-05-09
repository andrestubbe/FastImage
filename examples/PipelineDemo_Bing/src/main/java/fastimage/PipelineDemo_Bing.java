package fastimage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * PipelineDemo_Bing - Visualizing the processing chain.
 * Steps: Original -> Gaussian Blur -> Contrast -> Grayscale.
 */
public class PipelineDemo_Bing extends JFrame {

    private final BufferedImage original;
    private final JLabel[] previewLabels = new JLabel[4];
    private final JLabel[] timeLabels = new JLabel[4];
    private final String[] stepNames = {"ORIGINAL", "BLUR GAUSSIAN", "CONTRAST", "GRAYSCALE"};

    public PipelineDemo_Bing() {
        setTitle("FastImage Pipeline Showcase (Bing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(20, 20, 20));
        setLayout(new BorderLayout());

        original = generateTestImage(1200, 800);

        // Header
        JLabel header = new JLabel("MULTI-STAGE SIMD PIPELINE", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Grid for steps
        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 20, 0, 20));

        for (int i = 0; i < 4; i++) {
            grid.add(createStepPanel(i));
        }
        add(grid, BorderLayout.CENTER);

        // Start Button
        JButton btnRun = new JButton("▶ RUN PIPELINE");
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnRun.setBackground(new Color(50, 180, 80));
        btnRun.setForeground(Color.WHITE);
        btnRun.addActionListener(e -> {
            btnRun.setEnabled(false);
            runPipeline();
        });
        add(btnRun, BorderLayout.SOUTH);

        pack();
        setSize(1300, 500);
        setLocationRelativeTo(null);
        
        // Initial state
        previewLabels[0].setIcon(new ImageIcon(original.getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
    }

    private JPanel createStepPanel(int index) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        
        JLabel name = new JLabel(stepNames[index], SwingConstants.CENTER);
        name.setForeground(new Color(180, 180, 180));
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(name, BorderLayout.NORTH);

        previewLabels[index] = new JLabel();
        previewLabels[index].setHorizontalAlignment(SwingConstants.CENTER);
        previewLabels[index].setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
        previewLabels[index].setPreferredSize(new Dimension(300, 200));
        p.add(previewLabels[index], BorderLayout.CENTER);

        timeLabels[index] = new JLabel(index == 0 ? "-" : "WAITING...");
        timeLabels[index].setForeground(Color.GRAY);
        timeLabels[index].setHorizontalAlignment(SwingConstants.CENTER);
        timeLabels[index].setFont(new Font("Consolas", Font.PLAIN, 14));
        p.add(timeLabels[index], BorderLayout.SOUTH);

        return p;
    }

    private void runPipeline() {
        new Thread(() -> {
            try {
                // Step 0: Load (already done)
                
                // Step 1: Blur
                long t1 = System.nanoTime();
                FastImage fi = FastImage.fromBufferedImage(original);
                fi.blurGaussian(10.0f);
                long d1 = (System.nanoTime() - t1) / 1_000_000;
                updateStep(1, fi.toBufferedImage(), d1);
                Thread.sleep(500);

                // Step 2: Contrast
                long t2 = System.nanoTime();
                fi.adjustContrast(1.5f);
                long d2 = (System.nanoTime() - t2) / 1_000_000;
                updateStep(2, fi.toBufferedImage(), d2);
                Thread.sleep(500);

                // Step 3: Grayscale
                long t3 = System.nanoTime();
                fi.grayscale();
                long d3 = (System.nanoTime() - t3) / 1_000_000;
                updateStep(3, fi.toBufferedImage(), d3);
                
                fi.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void updateStep(int index, BufferedImage img, long ms) {
        SwingUtilities.invokeLater(() -> {
            previewLabels[index].setIcon(new ImageIcon(img.getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
            timeLabels[index].setText(ms + " ms");
            timeLabels[index].setForeground(new Color(0, 200, 255));
        });
    }

    private BufferedImage generateTestImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Complex background
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
            g.fillOval((int)(Math.random()*w), (int)(Math.random()*h), 100, 100);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Black", Font.PLAIN, 120));
        g.drawString("PIPELINE TEST", 150, h/2);
        g.dispose();
        return img;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PipelineDemo_Bing().setVisible(true));
    }
}
