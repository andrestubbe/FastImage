cd /d C:\Users\andre\Documents\FastJava\2026-04-15-Work-FastImage-v1.0

echo === FastImage Killer Demo ===
echo ============================================
echo.

echo Building native DLL...
call compile.bat
if errorlevel 1 (
    echo ERROR: DLL build failed!
    pause
    exit /b 1
)

REM Copy fresh DLLs if available
if exist "build\fastimage.dll" (
    echo Updating FastImage DLL...
    copy /Y build\fastimage.dll release\ >nul 2>&1
)
if exist "..\2026-04-09-Work-FastJava\FastTheme\release\fasttheme.dll" (
    echo Updating FastTheme DLL...
    copy /Y "..\2026-04-09-Work-FastJava\FastTheme\release\fasttheme.dll" release\ >nul 2>&1
)

echo Compiling...
javac -d . -cp release\fastcore-1.0.0.jar fastimage\FastImage.java 2>nul
javac -cp .;release\fastcore-1.0.0.jar;release\fasttheme-1.0.0.jar FastImageKillerDemo.java DemoUtils.java 2>nul

echo Starting Demo... 
echo.
echo CONSOLE OUTPUT - Tracking every 30 frames:
echo ============================================
echo.

java -cp .;release\fastcore-1.0.0.jar;release\fasttheme-1.0.0.jar -Djava.library.path=release FastImageKillerDemo 2>&1 | findstr /V "WARNING"

echo.
echo Demo finished.
pause
