@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0
echo === Building both ===
javac -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar FastBlurFinal.java 2>&1
javac -cp . JavaBlurComparison.java 2>&1
echo === Starting Side-by-Side Showcase ===
start "FastImage (Fast)" cmd /c "java -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar -Djava.library.path=release FastBlurFinal"
timeout /t 2 >nul
start "Java (Slow)" cmd /c "java -cp . JavaBlurComparison"
