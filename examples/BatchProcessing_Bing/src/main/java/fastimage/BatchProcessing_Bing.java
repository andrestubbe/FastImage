package fastimage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BatchProcessing_Bing - Parallel High-Throughput Demo.
 * Goal: Process 100 images as fast as possible.
 */
public class BatchProcessing_Bing extends JFrame {

    private final BufferedImage sample;
    private final JProgressBar progressBar;
    private final JLabel statsLabel;
    private final AtomicInteger completedCount = new AtomicInteger(0);

    public BatchProcessing_Bing() {
        setTitle("FastImage Batch Processor (Bing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 35));
        setLayout(new BorderLayout());

        sample = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sample.createGraphics();
        g.setPaint(new GradientPaint(0, 0, Color.CYAN, 1024, 1024, Color.MAGENTA));
        g.fillRect(0, 0, 1024, 1024);
        g.dispose();

        // Header
        JLabel header = new JLabel("PARALLEL BATCH PROCESSING (100 IMAGES)", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setBorder(new EmptyBorder(30, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        // Center Panel
        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 40, 0, 40));

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(0, 40));
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 255, 150));
        progressBar.setBackground(new Color(50, 50, 60));
        center.add(progressBar);

        statsLabel = new JLabel("READY TO PROCESS", SwingConstants.CENTER);
        statsLabel.setForeground(Color.LIGHT_GRAY);
        statsLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        center.add(statsLabel);

        add(center, BorderLayout.CENTER);

        // Run Button
        JButton btnRun = new JButton("🚀 START BATCH PROCESS");
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnRun.setBackground(new Color(0, 120, 215));
        btnRun.setForeground(Color.WHITE);
        btnRun.addActionListener(e -> {
            btnRun.setEnabled(false);
            runBatch();
        });
        add(btnRun, BorderLayout.SOUTH);

        pack();
        setSize(800, 400);
        setLocationRelativeTo(null);
    }

    private void runBatch() {
        completedCount.set(0);
        progressBar.setValue(0);
        statsLabel.setText("PROCESSING...");

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            int threadCount = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < 100; i++) {
                executor.submit(() -> {
                    try {
                        FastImage img = FastImage.fromBufferedImage(sample);
                        img.resize(512, 512);
                        img.blurStack(5.0f);
                        img.dispose();
                        
                        int done = completedCount.incrementAndGet();
                        SwingUtilities.invokeLater(() -> progressBar.setValue(done));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long duration = System.currentTimeMillis() - startTime;
            double imgPerSec = 100.0 / (duration / 1000.0);

            SwingUtilities.invokeLater(() -> {
                statsLabel.setText(String.format("FINISHED: %d ms (%.1f images/sec)", duration, imgPerSec));
                statsLabel.setForeground(Color.GREEN);
            });
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BatchProcessing_Bing().setVisible(true));
    }
}
