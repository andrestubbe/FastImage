package fastimage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

public class FastImageBenchmark {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int ITERATIONS = 100;

    public static void main(String[] args) {
        System.out.println("=== FastImage SIMD Benchmark ===");
        System.out.println("Image Size: " + WIDTH + "x" + HEIGHT);
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();

        BufferedImage testImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = testImg.createGraphics();
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(java.awt.Color.RED);
        g.fillOval(100, 100, 500, 500);
        g.dispose();

        FastImage fastImg = FastImage.fromBufferedImage(testImg);

        // 1. Grayscale Benchmark
        benchmarkGrayscale(testImg, fastImg);

        // 2. Brightness Benchmark
        benchmarkBrightness(testImg, fastImg);

        // 3. Blur Benchmark (Radius 10)
        benchmarkBlur(testImg, fastImg, 10.0f);

        fastImg.dispose();
    }

    private static void benchmarkGrayscale(BufferedImage javaImg, FastImage fastImg) {
        System.out.println("[1] GRAYSCALE");
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            // Java doesn't have a simple grayscale filter that's fast, usually ColorConvertOp
            // We use a simplified version for comparison
        }
        long javaTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            fastImg.grayscale();
        }
        long fastTime = System.currentTimeMillis() - start;

        System.out.printf("    FastImage (SSE): %d ms (%.2f ms/op)\n", fastTime, (double)fastTime / ITERATIONS);
        System.out.println();
    }

    private static void benchmarkBrightness(BufferedImage javaImg, FastImage fastImg) {
        System.out.println("[2] BRIGHTNESS (factor 1.2)");
        
        RescaleOp op = new RescaleOp(1.2f, 0, null);
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            op.filter(javaImg, null);
        }
        long javaTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            fastImg.adjustBrightness(1.2f);
        }
        long fastTime = System.currentTimeMillis() - start;

        System.out.printf("    Java (RescaleOp): %d ms (%.2f ms/op)\n", javaTime, (double)javaTime / ITERATIONS);
        System.out.printf("    FastImage (SSE):  %d ms (%.2f ms/op)\n", fastTime, (double)fastTime / ITERATIONS);
        System.out.printf("    Speedup:          %.1fx\n", (double)javaTime / fastTime);
        System.out.println();
    }

    private static void benchmarkBlur(BufferedImage javaImg, FastImage fastImg, float radius) {
        System.out.println("[3] GAUSSIAN BLUR (radius " + radius + ")");
        
        // Java ConvolveOp is VERY slow for large kernels
        int size = (int)radius * 2 + 1;
        float[] data = new float[size * size];
        for(int i=0; i<data.length; i++) data[i] = 1.0f / (size * size);
        ConvolveOp op = new ConvolveOp(new Kernel(size, size, data));
        
        long start = System.currentTimeMillis();
        // Only 5 iterations for Java because it's so slow
        for (int i = 0; i < 5; i++) {
            op.filter(javaImg, null);
        }
        long javaTime = (System.currentTimeMillis() - start) * (ITERATIONS / 5);

        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            fastImg.blurGaussian(radius);
        }
        long fastTime = System.currentTimeMillis() - start;

        System.out.printf("    Java (ConvolveOp): %d ms (estimated for %d ops)\n", javaTime, ITERATIONS);
        System.out.printf("    FastImage (SIMD):  %d ms (%.2f ms/op)\n", fastTime, (double)fastTime / ITERATIONS);
        System.out.printf("    Speedup:           %.1fx\n", (double)javaTime / fastTime);
        System.out.println();
    }
}
