@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === Building DLL with Gaussian Blur ===
call build_simple.bat 2>&1

echo === Copying new DLL ===
copy build\fastimage.dll release\ /Y
copy build\fastimage.dll src\main\resources\ /Y 2>nul

echo === Compiling FastImage Gaussian ===
javac -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar FastImageGaussianBench.java 2>&1

echo === Compiling Java Gaussian ===
javac -cp . JavaGaussianBench.java 2>&1

echo === Starting Benchmark: FastImage vs Java (both Gaussian) ===
echo [FAST] and [JAVA] timings will appear in console
echo.
start "FastImage Gaussian" cmd /c "java -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar -Djava.library.path=release FastImageGaussianBench 2>&1 | findstr /C:[FAST]"
timeout /t 2 >nul
start "Java Gaussian" cmd /c "java -cp . JavaGaussianBench 2>&1 | findstr /C:[JAVA]"
