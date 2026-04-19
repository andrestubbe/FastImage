@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0
echo === Compiling ===
javac -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar FastBlurDebug.java 2>&1
echo === Running (press Ctrl+C to stop) ===
java -cp .;release\fastimage-1.0.1.jar;release\fastcore-1.0.0.jar -Djava.library.path=release FastBlurDebug
pause
