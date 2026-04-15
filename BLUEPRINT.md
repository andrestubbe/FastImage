# FastJava Blueprint - Template Guide

> This file explains the design decisions behind this template structure.
> Read this when creating a new Fast* module from this blueprint.

---

## Core Philosophy

```
src/main/java/     = API/Library code ONLY (what users import)
examples/          = ALL runnable code (demos, tutorials, tests)
docs/              = Screenshots, GIFs, images only
```

**Golden Rule:** Never put `main()` methods in `src/main/java`. Ever.

---

## Folder Structure Explained

### `src/main/java/fastXXX/`
- **Purpose:** Library API
- **Contains:** Public classes users instantiate (FastClipboard, FastMath, etc.)
- **No:** Main methods, demos, examples, test code

### `src/test/java/fastXXX/` (Optional)
- **Purpose:** JUnit tests
- **When to add:** When you need automated testing, API stability checks, or CI/CD
- **Not needed for:** Early development, manual testing, native modules tested via examples
- **Maven:** Automatically recognized when folder exists

### `examples/`
- **Purpose:** Standalone, runnable mini-projects
- **Each subfolder:** Has its own `pom.xml`, can be copied as starter template
- **Naming:** `00-*`, `10-*`, `20-*` for sorting (00 = start here)

### `docs/`
- **Purpose:** Binary assets only
- **Contains:** Screenshots, GIFs, architecture diagrams
- **Not for:** Markdown docs (those go to root level)

### `native/`
- **Purpose:** C/C++ JNI code (optional)
- **Only for:** Modules using native acceleration
- **Structure:** Flat - `.cpp` and `.h` files directly in `native/`, no `src/` subfolder
- **Optional:** `native/kernels/` for OpenCL kernels

---

## Using This Blueprint

### For a new Fast* module:

1. **Copy this template**
   ```bash
   cp -r BLUEPRINT FastWindow
   ```

2. **Replace placeholders**
   - `fastXXX` → `fastwindow` (package name)
   - `FastXXX` → `FastWindow` (class names)
   - Update `groupId` in `pom.xml`

3. **Remove unused folders**
   - No JNI? Delete `native/`
   - No examples yet? Keep `examples/00-basic-usage/` as template

4. **Implement your API in `src/main/java/`**
   - Public classes only
   - Clean, documented API surface

5. **Move Demo to examples**
   - Create `examples/00-basic-usage/`
   - Copy Demo.java there
   - Update example's `pom.xml` mainClass

### File Checklist:

- [ ] `pom.xml` - Update artifactId, name, description
- [ ] `LICENSE` - Keep MIT (or change if needed)
- [ ] `README.md` - Replace XXX with your module name
- [ ] `src/main/java/fastXXX/FastXXX.java` - Core API class
- [ ] `examples/00-basic-usage/` - Working demo
- [ ] `.gitignore` - Usually no changes needed

---

## Design Decisions

### Why `examples/` on root level?

| Alternative | Problem | Our Solution |
|-------------|---------|--------------|
| `src/main/java/Demo.java` | Pollutes API, ships in JAR | `examples/` separate |
| `src/test/java/Example.java` | Tests ≠ tutorials, different classpath | `examples/` standalone projects |
| `demo/` or `samples/` | Not clear it's copy-paste templates | `examples/` with own pom.xml |

### Why `00-*` prefix?

- Sorts alphabetically first
- Signals "start here"
- Leaves room for `10-`, `20-`, `30-` (advanced, UI, native)

### Why no `mainClass` in main pom.xml?

Library JARs should not have a main class. The manifest should be clean. Runnables are in `examples/`.

---

## Optional Project-Specific Docs

Create these at **root level** as needed:

- `BENCHMARK.md` - Performance results (like FastMath)
- `TODO.md` - Development roadmap
- `DEPLOYMENT.md` - Maven Central release guide
- `PROMOTION.md` - Social media content

**Not in `docs/`** - that's for images only.

---

## Common Patterns

### Basic Module (minimal, no JNI):
```
fastwindow/
├── src/main/java/fastwindow/FastWindow.java
├── examples/00-basic-usage/
├── pom.xml
└── README.md
```

### Basic Module (with tests):
```
fastwindow/
├── src/main/java/fastwindow/FastWindow.java
├── src/test/java/fastwindow/FastWindowTest.java   # (Optional)
├── examples/00-basic-usage/
├── pom.xml
└── README.md
```

### JNI Module (native code):
```
fastmath/
├── src/main/java/fastmath/FastMath.java
├── native/
│   ├── fastmath.cpp           # Native implementation
│   ├── fastmath.h             # Header file
│   └── kernels/               # OpenCL kernels (optional)
├── compile.bat
├── examples/00-basic-usage/
├── pom.xml
└── README.md
```

### Complex Module (multiple examples):
```
fastmath/
├── examples/
│   ├── 00-basic-usage/        # Hello World
│   ├── 10-vectors/            # Vector math demo
│   ├── 20-noise/              # Procedural generation
│   └── 30-benchmark/          # Performance test
```

---

## Quick Start for New Module

```bash
# 1. Copy blueprint
cp -r BLUEPRINT my-new-module
cd my-new-module

# 2. Replace XXX with your name
find . -type f -exec sed -i 's/fastXXX/fastwindow/g' {} +
find . -type f -exec sed -i 's/FastXXX/FastWindow/g' {} +

# 3. Update pom.xml
# Edit artifactId, name, description, url

# 4. Implement your API
# Edit src/main/java/fastwindow/FastWindow.java

# 5. Test build
mvn clean test

# 6. Run example
cd examples/00-basic-usage
mvn compile exec:java
```

---

**This blueprint is part of the FastJava ecosystem:**
https://github.com/andrestubbe/BLUEPRINT
