package fastimage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ResizeBenchmark_Bing - SIMD vs Java2D Comparison.
 * Focus: High-throughput 4K to 1080p scaling.
 */
public class ResizeBenchmark_Bing extends JFrame {

    private final BufferedImage src4K;
    private final JLabel labelJava, labelFast;
    private final JProgressBar barJava, barFast;
    private final JLabel statsJava, statsFast;
    
    private final AtomicInteger opsJava = new AtomicInteger(0);
    private final AtomicInteger opsFast = new AtomicInteger(0);
    private boolean running = true;

    public ResizeBenchmark_Bing() {
        setTitle("FastImage vs BufferedImage - Resize Benchmark (Bing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(15, 15, 15));
        setLayout(new BorderLayout());

        // Generate a 4K Test Image (3840x2160)
        src4K = new BufferedImage(3840, 2160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = src4K.createGraphics();
        g.setPaint(new GradientPaint(0, 0, Color.BLUE, 3840, 2160, Color.RED));
        g.fillRect(0, 0, 3840, 2160);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 200));
        g.drawString("4K TEST SOURCE", 500, 1080);
        g.dispose();

        // UI Header
        JLabel header = new JLabel("4K -> 1080P RESIZE BENCHMARK", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Center Comparison
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 20, 0, 20));

        labelJava = createDisplayPanel("JAVA BUFFEREDIMAGE (BILINEAR)", center);
        labelFast = createDisplayPanel("FASTIMAGE (SIMD / AVX2)", center);
        
        barJava = createProgressBar(center);
        barFast = createProgressBar(center);
        
        statsJava = createStatsLabel(center);
        statsFast = createStatsLabel(center);

        add(center, BorderLayout.CENTER);

        // Footer / Start Button
        JButton btnStart = new JButton("🚀 START STRESS TEST");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnStart.setBackground(new Color(0, 150, 255));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setBorder(new EmptyBorder(15, 0, 15, 0));
        btnStart.addActionListener(e -> {
            btnStart.setEnabled(false);
            startBenchmark();
        });
        add(btnStart, BorderLayout.SOUTH);

        pack();
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private JLabel createDisplayPanel(String title, JPanel parent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(new Color(150, 150, 150));
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(t, BorderLayout.NORTH);
        
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40)));
        p.add(imgLabel, BorderLayout.CENTER);
        
        parent.add(p);
        return imgLabel;
    }

    private JProgressBar createProgressBar(JPanel parent) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setForeground(new Color(0, 150, 255));
        bar.setBackground(new Color(30, 30, 30));
        bar.setBorderPainted(false);
        // We add them in a specific way to the grid... this is just a quick hack for the demo layout
        return bar; 
    }
    
    private JLabel createStatsLabel(JPanel parent) {
        JLabel l = new JLabel("0 OPS/S", SwingConstants.CENTER);
        l.setForeground(Color.GREEN);
        l.setFont(new Font("Consolas", Font.BOLD, 20));
        return l;
    }

    private void startBenchmark() {
        // We need to re-layout slightly to show bars and stats
        getContentPane().removeAll();
        setLayout(new GridLayout(2, 2, 20, 20));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));
        
        add(createBenchmarkColumn("JAVA2D", labelJava, barJava, statsJava));
        add(createBenchmarkColumn("FASTIMAGE SIMD", labelFast, barFast, statsFast));
        
        revalidate();
        
        // Java Thread
        new Thread(() -> {
            while (running) {
                BufferedImage scaled = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(src4K, 0, 0, 1920, 1080, null);
                g.dispose();
                opsJava.incrementAndGet();
                if (opsJava.get() % 5 == 0) {
                    ImageIcon icon = new ImageIcon(scaled.getScaledInstance(450, 250, Image.SCALE_FAST));
                    SwingUtilities.invokeLater(() -> labelJava.setIcon(icon));
                }
            }
        }).start();

        // FastImage Thread
        new Thread(() -> {
            FastImage fiSrc = FastImage.fromBufferedImage(src4K);
            while (running) {
                FastImage fi = FastImage.fromNativeHandle(fiSrc.getNativeHandle(), fiSrc.getWidth(), fiSrc.getHeight());
                // Note: resize modifies the image, so we'd need a copy if we wanted to reuse src
                // But for benchmark, we just scale down
                fi.resize(1920, 1080);
                opsFast.incrementAndGet();
                if (opsFast.get() % 20 == 0) {
                    BufferedImage res = fi.toBufferedImage();
                    ImageIcon icon = new ImageIcon(res.getScaledInstance(450, 250, Image.SCALE_FAST));
                    SwingUtilities.invokeLater(() -> labelFast.setIcon(icon));
                }
                // Reset for next loop (this is a simplified bench)
                fi.resize(3840, 2160); 
            }
        }).start();

        // Stats Updater
        new Timer(1000, e -> {
            int j = opsJava.getAndSet(0);
            int f = opsFast.getAndSet(0);
            statsJava.setText(j + " OPS/S");
            statsFast.setText(f + " OPS/S");
            barJava.setValue(Math.min(100, j * 2)); // Scale for visibility
            barFast.setValue(Math.min(100, f / 2)); // FastImage is way faster
        }).start();
    }
    
    private JPanel createBenchmarkColumn(String name, JLabel img, JProgressBar bar, JLabel stats) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        JLabel title = new JLabel(name, SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        p.add(title, BorderLayout.NORTH);
        p.add(img, BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new GridLayout(2, 1, 5, 5));
        bottom.setOpaque(false);
        bottom.add(bar);
        bottom.add(stats);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ResizeBenchmark_Bing().setVisible(true));
    }
}
