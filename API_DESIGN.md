# FastImage API Design — BufferedImage Compatibility & Modern High‑Performance API

FastImage ist eine High‑Performance Image Processing Library für Java, die vollständig BufferedImage‑kompatibel ist und gleichzeitig eine moderne, fluente API bietet.

**Ziel:** „FastImage macht BufferedImage schneller" — ohne den bestehenden Java‑Code zu brechen.

FastImage kombiniert:
- Drop‑in Kompatibilität
- Off‑Heap Speicher
- SIMD‑Optimierungen (SSE/AVX)
- Fluent API für moderne Workflows
- Zero‑Copy Pipelines

Damit wird FastImage zur praktischen, schnellen und sicheren Alternative zu BufferedImage.

---

## 🎯 Ziel: „BufferedImage API + moderne Fluent API + native Geschwindigkeit"

FastImage soll:
- existierenden Java‑Code ohne Änderungen beschleunigen
- neue Projekte mit einer modernen, chainable API unterstützen
- Off‑Heap‑Speicher nutzen, um GC‑Pressure zu vermeiden
- Bildoperationen 10–20× schneller ausführen als BufferedImage

---

## 1. Level: Drop‑in Replacement (BufferedImage‑kompatibel)

FastImage unterstützt die wichtigsten BufferedImage‑Methoden:

- `getWidth()`
- `getHeight()`
- `getRGB(x, y)`
- `setRGB(x, y, rgb)`
- `getScaledInstance(width, height, hints)`

### Beispiel: Migration ohne API‑Änderung

**Vorher (BufferedImage):**
```java
BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

for (int y = 0; y < img.getHeight(); y++) {
    for (int x = 0; x < img.getWidth(); x++) {
        int rgb = img.getRGB(x, y);     // LANGSAM
        int gray = toGrayscale(rgb);
        img.setRGB(x, y, gray);         // LANGSAM
    }
}

BufferedImage scaled = img.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
```

**Nachher (FastImage):**
```java
FastImage img = FastImage.create(800, 600);

for (int y = 0; y < img.getHeight(); y++) {
    for (int x = 0; x < img.getWidth(); x++) {
        int rgb = img.getRGB(x, y);     // SCHNELL (direkter Off‑Heap Zugriff)
        int gray = toGrayscale(rgb);
        img.setRGB(x, y, gray);         // SCHNELL
    }
}

FastImage scaled = img.getScaledInstance(400, 300, FastImage.SCALE_BILINEAR);
```

### Unterschiede

- `TYPE_INT_ARGB` → implizit, FastImage nutzt immer ARGB
- `Image.SCALE_*` → ersetzt durch:
  - `SCALE_NEAREST`
  - `SCALE_BILINEAR`
  - `SCALE_BICUBIC`
  - `SCALE_LANCZOS`

---

## 2. Level: Moderne Fluent API (empfohlen für neue Projekte)

Die Fluent API ist chainable, minimalistisch und performant.

### Beispiel
```java
FastImage result = FastImage.fromFile("photo.jpg")
        .resize(1920, 1080, FastImage.LANCZOS)
        .brightness(1.2f)
        .contrast(1.1f)
        .blur(2.0f)
        .toBufferedImage();
```

---

## 3. Vorgeschlagene vollständige API

```java
public class FastImage {

    // === BufferedImage-kompatible Methoden ===

    public static FastImage create(int width, int height);
    public static FastImage create(int width, int height, int type); // type ignoriert

    public int getWidth();
    public int getHeight();
    public int getRGB(int x, int y);
    public void setRGB(int x, int y, int rgb);

    public FastImage getScaledInstance(int width, int height, int hints);

    // === Moderne High-Performance API ===

    // Geometrie
    public FastImage resize(int width, int height);
    public FastImage resize(int width, int height, int algorithm);
    public FastImage crop(int x, int y, int width, int height);
    public FastImage flipHorizontal();
    public FastImage flipVertical();
    public FastImage rotate(double angle);

    // Filter
    public FastImage blur(float radius);
    public FastImage sharpen(float amount);
    public FastImage edgeDetect();
    public FastImage emboss();

    // Farbe
    public FastImage grayscale();
    public FastImage sepia();
    public FastImage invert();
    public FastImage brightness(float factor);
    public FastImage contrast(float factor);
    public FastImage saturation(float factor);

    // I/O
    public static FastImage fromFile(String path);
    public static FastImage fromBufferedImage(BufferedImage img);
    public void saveToFile(String path, String format);
    public BufferedImage toBufferedImage();

    // Speicher
    public void dispose();
    public long getNativeMemoryUsage();

    // Konstanten
    public static final int SCALE_NEAREST  = 0;
    public static final int SCALE_BILINEAR = 1;
    public static final int SCALE_BICUBIC  = 2;
    public static final int SCALE_LANCZOS  = 3;
}
```

---

## 4. Beispiel: Migration von BufferedImage

**Vorher:**
```java
public BufferedImage processImage(BufferedImage input) {
    BufferedImage scaled = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = scaled.createGraphics();
    g.drawImage(input, 0, 0, 800, 600, null);
    g.dispose();

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

**Nachher:**
```java
public BufferedImage processImage(BufferedImage input) {
    FastImage img = FastImage.fromBufferedImage(input)
            .resize(800, 600)
            .grayscale()
            .brightness(1.1f);

    BufferedImage result = img.toBufferedImage();
    img.dispose();
    return result;
}
```

---

## 5. Design‑Entscheidungen

| Feature | Entscheidung | Grund |
|---------|--------------|-------|
| `getRGB` / `setRGB` | Ja, aber schnell | Drop‑in Kompatibilität |
| `TYPE_*` Konstanten | Nein | Immer ARGB, einfacher |
| `Graphics2D` | Nein | Nicht performant |
| `Raster` / `DataBuffer` | Nein | Direkter Off‑Heap Zugriff |
| `ColorModel` | Nein | Immer sRGB |
| Fluent API | Ja | Moderne Java‑Entwicklung |
| `dispose()` | Ja, wichtig | Off‑Heap muss freigegeben werden |

---

## 6. Empfehlung

**FastImage = BufferedImage API + moderne Fluent API + native Geschwindigkeit**

- einfache Migration
- moderne Workflows
- keine Performance‑Fallen
- ideal für FastGraphics, FastAI, FastVision
