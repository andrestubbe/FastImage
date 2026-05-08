@echo off
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
echo  [1] Visual Editor (Showcase)
echo  [2] Blur Gallery (Algorithms)
echo  [3] Resize Demo (Bilinear/Bicubic)
echo  [4] Performance Benchmark
echo  [5] Basic Usage (Minimal)
echo  [Q] Quit
echo.
set /p choice="Select a demo to run [1-5, Q]: "

if /i "%choice%"=="1" set "DEMO_DIR=VisualEditor" & set "MAIN=fastimage.VisualEditor"
if /i "%choice%"=="2" set "DEMO_DIR=BlurGallery"   & set "MAIN=fastimage.BlurGallery"
if /i "%choice%"=="3" set "DEMO_DIR=ResizeDemo"    & set "MAIN=fastimage.ResizeDemo"
if /i "%choice%"=="4" set "DEMO_DIR=Benchmark"     & set "MAIN=fastimage.FastImageBenchmark"
if /i "%choice%"=="5" set "DEMO_DIR=BasicUsage"    & set "MAIN=fastimage.BasicUsage"
if /i "%choice%"=="Q" exit /b

if not defined DEMO_DIR goto menu

echo.
echo [+] Preparing %DEMO_DIR%...
cd examples\%DEMO_DIR%
call mvn compile -DskipTests

:: Build CP
set "CP=target\classes"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fastimage\0.1.0\fastimage-0.1.0.jar"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fasttheme\0.2.0\fasttheme-0.2.0.jar"

echo [+] Launching %MAIN%...
"%JAVA_HOME%\bin\java.exe" "-Djava.library.path=%LIB_PATH%" -cp "%CP%" %MAIN%

cd ..\..
echo.
echo [!] Demo finished.
pause
goto menu
