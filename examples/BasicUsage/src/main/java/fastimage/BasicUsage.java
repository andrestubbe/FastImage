package fastimage;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * BasicUsage - Der einfachste Weg, FastImage zu nutzen.
 */
public class BasicUsage {
    public static void main(String[] args) {
        System.out.println("--- FastImage Basic Usage Example ---");

        try {
            // 1. Ein BufferedImage laden (Standard Java)
            // Wir generieren hier ein Testbild, falls kein Bild vorhanden ist
            BufferedImage input = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = input.createGraphics();
            g.setColor(java.awt.Color.RED);
            g.fillRect(50, 50, 400, 400);
            g.dispose();

            // 2. FastImage Objekt erstellen (Pixel wandern in nativen Speicher)
            FastImage img = FastImage.fromBufferedImage(input);

            // 3. Native SIMD-Operationen anwenden
            System.out.println("[+] Applying Grayscale...");
            img.grayscale();

            System.out.println("[+] Applying Brightness (1.5x)...");
            img.adjustBrightness(1.5f);

            System.out.println("[+] Applying Gaussian Blur (radius 10.0)...");
            img.blurGaussian(10.0f);

            // 4. Zurück in ein BufferedImage konvertieren
            BufferedImage result = img.toBufferedImage();

            // 5. Ressourcen freigeben! (WICHTIG bei nativem Speicher)
            img.dispose();

            System.out.println("✅ Processing complete.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
