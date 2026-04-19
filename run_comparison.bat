@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === Building Fast Blur ===
javac -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar FastBlurAnimation.java

echo === Building Java Blur ===
javac -cp . JavaBlurComparison.java

echo === Starting both demos ===
echo FastImage (left) vs Java BufferedImage (right)
start "FastImage" cmd /c "java -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar -Djava.library.path=release FastBlurAnimation"
timeout /t 1 >nul
start "Java" cmd /c "java -cp . JavaBlurComparison"
