@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === Building FastImage with 6 Blur Algorithms ===
javac -d . -cp release\fastcore-1.0.0.jar src\main\java\fastimage\FastImage.java 2>&1

echo === Compiling all 6 Demos ===
javac -cp .;release\fastcore-1.0.0.jar DemoBlurBox.java DemoBlurGaussian.java DemoBlurStack.java DemoBlurKawase.java DemoBlurDualKawase.java DemoBlurMipmapped.java DemoUtils.java 2>&1

echo === Running All 6 Blur Demos ===
echo.
echo 1. blurBox       - Fastest, simple
echo 2. blurGaussian  - High quality smooth
echo 3. blurStack     - CSS style, balanced
echo 4. blurKawase    - Multi-pass, soft
echo 5. blurDualKawase - Premium 2-pass
echo 6. blurMipmapped - For huge radii
echo.

start "Demo 1: blurBox" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurBox"
timeout /t 1 >nul
start "Demo 2: blurGaussian" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurGaussian"
timeout /t 1 >nul
start "Demo 3: blurStack" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurStack"
timeout /t 1 >nul
start "Demo 4: blurKawase" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurKawase"
timeout /t 1 >nul
start "Demo 5: blurDualKawase" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurDualKawase"
timeout /t 1 >nul
start "Demo 6: blurMipmapped" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release DemoBlurMipmapped"

echo.
echo All 6 demos started! Close windows when done.
