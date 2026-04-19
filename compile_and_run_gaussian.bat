@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === Compiling FastImage.java with Gaussian ===
javac -d . -cp release\fastcore-1.0.0.jar src\main\java\fastimage\FastImage.java 2>&1

echo === Compiling Benchmarks ===
javac -cp .;release\fastcore-1.0.0.jar FastImageGaussianBench.java JavaGaussianBench.java DemoUtils.java 2>&1

echo === Starting Benchmark ===
echo FastImage (LEFT) vs Java (RIGHT)
echo Both use Separable Gaussian Blur - fair comparison
echo.
start "FastImage Gaussian" cmd /c "java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release FastImageGaussianBench"
timeout /t 2 >nul
start "Java Gaussian" cmd /c "java -cp . JavaGaussianBench"
