@echo off
chcp 65001 >nul
cd /d "%~dp0"
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "REPO=%USERPROFILE%\.m2\repository"
set "LIB_PATH=..\..\build"

:menu
cls
echo ==========================================
echo   FastJava Ecosystem - FastImage Demos
echo ==========================================
echo.
echo  --- Core Demos ---
echo  [1] Visual Editor (Showcase)
echo  [2] Blur Gallery (Algorithms)
echo  [3] Resize Demo (Bilinear/Bicubic)
echo  [4] Basic Usage (Minimal)
echo.
echo  --- Bing Showcase Demos ---
echo  [5] Resize Benchmark (4K vs 1080p) _Bing
echo  [6] Pipeline Demo (Multi-Step) _Bing
echo  [7] Batch Processing (100 Images) _Bing
echo.
echo  [Q] Quit
echo.
set /p choice="Select a demo to run [1-7, Q]: "

if /i "%choice%"=="1" (
    set "DEMO_DIR=VisualEditor"
    set "MAIN=fastimage.VisualEditor"
)
if /i "%choice%"=="2" (
    set "DEMO_DIR=BlurGallery"
    set "MAIN=fastimage.BlurGallery"
)
if /i "%choice%"=="3" (
    set "DEMO_DIR=ResizeDemo"
    set "MAIN=fastimage.ResizeDemo"
)
if /i "%choice%"=="4" (
    set "DEMO_DIR=BasicUsage"
    set "MAIN=fastimage.BasicUsage"
)
if /i "%choice%"=="5" (
    set "DEMO_DIR=ResizeBenchmark_Bing"
    set "MAIN=fastimage.ResizeBenchmark_Bing"
)
if /i "%choice%"=="6" (
    set "DEMO_DIR=PipelineDemo_Bing"
    set "MAIN=fastimage.PipelineDemo_Bing"
)
if /i "%choice%"=="7" (
    set "DEMO_DIR=BatchProcessing_Bing"
    set "MAIN=fastimage.BatchProcessing_Bing"
)
if /i "%choice%"=="Q" exit /b

if not defined DEMO_DIR goto menu

echo.
echo [+] Preparing %DEMO_DIR%...
cd examples\%DEMO_DIR%
call mvn compile -DskipTests

:: Build CP
set "CP=target\classes"
:: Include project classes if we are running from a sub-module
set "CP=%CP%;..\..\target\classes"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fastimage\0.1.0\fastimage-0.1.0.jar"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar"

echo [+] Launching %MAIN%...
:: Note: Using --enable-native-access for JDK 21+
"%JAVA_HOME%\bin\java.exe" "--enable-native-access=ALL-UNNAMED" "-Djava.library.path=%LIB_PATH%" -cp "%CP%" %MAIN%

cd ..\..
echo.
echo [!] Demo finished.
pause
goto menu
