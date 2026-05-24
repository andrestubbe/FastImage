package fastimage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FastImageTest - Quality and Stability Suite.
 */
public class FastImageTest {

    @Test
    @DisplayName("Test Basic Lifecycle (Create, Dispose)")
    void testLifecycle() {
        FastImage img = FastImage.create(100, 100);
        assertEquals(100, img.getWidth());
        assertEquals(100, img.getHeight());
        
        img.dispose();
        
        // Should throw exception after dispose
        assertThrows(IllegalStateException.class, img::grayscale);
    }

    @Test
    @DisplayName("Test Parameter Validation (Bounds Checks)")
    void testValidation() {
        FastImage img = FastImage.create(100, 100);
        
        // Invalid Radius
        assertThrows(FastImageException.class, () -> img.blurGaussian(-1.0f));
        
        // Invalid Resize
        assertThrows(FastImageException.class, () -> img.resize(0, 100));
        
        // Invalid Brightness
        assertThrows(FastImageException.class, () -> img.adjustBrightness(-0.5f));
        
        img.dispose();
    }

    @Test
    @DisplayName("Test Grayscale Integrity (SIMD Correctness)")
    void testGrayscale() {
        BufferedImage buffered = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        // Fill with pure red: 0xFFFF0000
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) buffered.setRGB(x, y, 0xFFFF0000);
        }
        
        FastImage img = FastImage.fromBufferedImage(buffered);
        img.grayscale();
        
        int[] pixels = img.getPixels();
        // Red (255, 0, 0) grayscale (0.299 weighting) should be around 76-77
        int firstPixel = pixels[0];
        int r = (firstPixel >> 16) & 0xFF;
        int g = (firstPixel >> 8) & 0xFF;
        int b = firstPixel & 0xFF;
        
        assertEquals(r, g);
        assertEquals(g, b);
        assertTrue(r > 70 && r < 80, "Grayscale value for red should be ~77, but was " + r);
        
        img.dispose();
    }

    @Test
    @DisplayName("Test Memory Safety (Handle Checks)")
    void testNullHandle() {
        // This is a bit of a hack to test native handle safety
        // In a real scenario, this would come from a corrupted state
        assertThrows(FastImageException.class, () -> {
            FastImage.fromNativeHandle(0, 10, 10).grayscale();
        });
    }

    @Test
    @DisplayName("Test Stability Stress (Rapid Resize/Filter)")
    void testStability() {
        FastImage img = FastImage.create(100, 100);
        for (int i = 0; i < 50; i++) {
            img.resize(200, 200);
            img.adjustBrightness(1.1f);
            img.resize(100, 100);
        }
        img.dispose();
    }
}
