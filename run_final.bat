@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0
echo === Compiling FastImage Blur ===
javac -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar FastBlurFinal.java 2>&1
echo === Starting (close window to stop) ===
java -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar -Djava.library.path=release FastBlurFinal
