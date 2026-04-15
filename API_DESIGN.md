# FastImage API Design - BufferedImage Kompatibilität

## Ziel: "FastImage macht BufferedImage schneller"

### Level 1: Drop-in Replacement (BufferedImage API)

```java
// Alt mit BufferedImage:
BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
for (int y = 0; y < img.getHeight(); y++) {
    for (int x = 0; x < img.getWidth(); x++) {
        int rgb = img.getRGB(x, y);        // LANGSAM!
        int gray = toGrayscale(rgb);
        img.setRGB(x, y, gray);            // LANGSAM!
    }
}
BufferedImage scaled = img.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
```

```java
// Neu mit FastImage (fast identisch, aber schnell):
FastImage img = FastImage.create(800, 600);
for (int y = 0; y < img.getHeight(); y++) {
    for (int x = 0; x < img.getWidth(); x++) {
        int rgb = img.getRGB(x, y);        // SCHNELL! (direkter Speicherzugriff)
        int gray = toGrayscale(rgb);
        img.setRGB(x, y, gray);            // SCHNELL!
    }
}
FastImage scaled = img.getScaledInstance(400, 300, FastImage.SCALE_BILINEAR);
```

**Unterschiede:**
- `TYPE_INT_ARGB` → implizit (immer ARGB)
- `Image.SCALE_*` → `FastImage.SCALE_BILINEAR`, `SCALE_BICUBIC`, `SCALE_NEAREST`

### Level 2: Moderne Fluent API (empfohlen für neue Projekte)

```java
// Fluent API für typische Workflows:
FastImage result = FastImage.fromFile("photo.jpg")
    .resize(1920, 1080, FastImage.LANCZOS)
    .adjust(brightness: 1.2f, contrast: 1.1f)
    .blur(2.0f)
    .sharpen(0.5f)
    .toBufferedImage();
```

### Vorgeschlagene komplette API:

```java
public class FastImage {
    
    // === BufferedImage-kompatible Methoden ===
    
    // Konstruktoren (ähnlich BufferedImage)
    public static FastImage create(int width, int height);
    public static FastImage create(int width, int height, int type);  // type ignoriert, immer ARGB
    
    // Basis-Getter (identisch zu BufferedImage)
    public int getWidth();
    public int getHeight();
    public int getRGB(int x, int y);           // ⚡ Schnell (direkter Zugriff)
    public void setRGB(int x, int y, int rgb);   // ⚡ Schnell
    
    // Skalierung (ähnlich getScaledInstance, aber besser)
    public FastImage getScaledInstance(int width, int height, int hints);
    
    // === Moderne High-Performance Methoden ===
    
    // Geometrie
    public FastImage resize(int width, int height);
    public FastImage resize(int width, int height, int algorithm);  // BILINEAR, BICUBIC, LANCZOS
    public FastImage crop(int x, int y, int width, int height);
    public FastImage flipHorizontal();
    public FastImage flipVertical();
    public FastImage rotate(double angle);  // Bilinear interpolation
    
    // Filter
    public FastImage blur(float radius);           // Gaussian blur
    public FastImage sharpen(float amount);        // Unsharp mask
    public FastImage edgeDetect();                  // Sobel operator
    public FastImage emboss();
    
    // Farbe
    public FastImage grayscale();
    public FastImage sepia();
    public FastImage invert();
    public FastImage brightness(float factor);       // 0.0-2.0
    public FastImage contrast(float factor);       // 0.0-2.0
    public FastImage saturation(float factor);     // 0.0-2.0
    
    // Convenience: Mehrere auf einmal
    public FastImage adjust(Adjustment... adjustments);
    
    // I/O
    public static FastImage fromFile(String path);
    public static FastImage fromBufferedImage(BufferedImage img);
    public void saveToFile(String path, String format);
    public BufferedImage toBufferedImage();
    
    // Speicher-Management
    public void dispose();  // Wichtig! Off-Heap-Speicher freigeben
    
    // Meta
    public long getNativeMemoryUsage();  // Für Debugging
    
    // === Konstanten ===
    public static final int SCALE_NEAREST = 0;
    public static final int SCALE_BILINEAR = 1;
    public static final int SCALE_BICUBIC = 2;
    public static final int SCALE_LANCZOS = 3;
}
```

### Beispiel: Migration von BufferedImage

```java
// Vorher (BufferedImage):
public BufferedImage processImage(BufferedImage input) {
    // Resize
    BufferedImage scaled = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = scaled.createGraphics();
    g.drawImage(input, 0, 0, 800, 600, null);
    g.dispose();
    
    // Grayscale (langsam in Java)
    for (int y = 0; y < scaled.getHeight(); y++) {
        for (int x = 0; x < scaled.getWidth(); x++) {
            int rgb = scaled.getRGB(x, y);
            int gray = ...;
            scaled.setRGB(x, y, gray);
        }
    }
    return scaled;
}
```

```java
// Nachher (FastImage) - fast identisch:
public BufferedImage processImage(BufferedImage input) {
    FastImage img = FastImage.fromBufferedImage(input)
        .resize(800, 600)
        .grayscale()
        .brightness(1.1f);
    
    BufferedImage result = img.toBufferedImage();
    img.dispose();  // WICHTIG!
    return result;
}
```

### Design-Entscheidungen:

| Feature | Entscheidung | Grund |
|---------|--------------|-------|
| `getRGB`/`setRGB` | **Ja, aber schnell** | Drop-in compatibility |
| `TYPE_*` Konstanten | **Nein** | Immer ARGB, einfacher |
| `Graphics2D` | **Nein** | Nicht schnell machbar |
| `Raster`/`DataBuffer` | **Nein** | Direkter Speicherzugriff stattdessen |
| `ColorModel` | **Nein** | Immer sRGB |
| Fluent API | **Ja** | Moderne Java-Stil |
| `dispose()` | **Ja, wichtig!** | Off-Heap muss freigegeben werden |

### Empfehlung:

**FastImage = "BufferedImage API + moderne Fluent API + Geschwindigkeit"**

- Einfache Migration bestehenden Codes (BufferedImage-Methoden)
- Neue Features über Fluent API (resize().blur().grayscale())
- Keine unerwarteten Performance-Fallen

Was denkst du? Soll ich die API in diese Richtung erweitern?
