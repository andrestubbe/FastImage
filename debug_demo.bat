@echo off
cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === STEP 1: Check DLL ===
if exist "release\fastimage.dll" (
    echo DLL found: release\fastimage.dll
) else (
    echo ERROR: DLL not found!
    copy build\fastimage.dll release\
)

echo.
echo === STEP 2: Compile FastImage ===
javac -d . -cp release\fastcore-1.0.0.jar src\main\java\fastimage\FastImage.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: FastImage compilation failed!
    pause
    exit /b 1
)
echo FastImage compiled OK

echo.
echo === STEP 3: Compile Demo ===
javac -cp .;release\fastcore-1.0.0.jar FastImageKillerDemo.java DemoUtils.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Demo compilation failed!
    pause
    exit /b 1
)
echo Demo compiled OK

echo.
echo === STEP 4: Check class files ===
dir FastImageKillerDemo.class /b
dir DemoUtils.class /b

echo.
echo === STEP 5: Start Demo ===
echo If no window appears, check for Java errors below:
echo.
java -cp .;release\fastcore-1.0.0.jar -Djava.library.path=release FastImageKillerDemo

echo.
echo === Demo exited ===
pause
