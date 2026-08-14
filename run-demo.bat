@echo off
echo [FastImage] Building Native Library...
call compile.bat
if %ERRORLEVEL% NEQ 0 exit /b 1

echo [FastImage] Building Core Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 exit /b 1

echo [FastImage] Running Basic Usage Demo...
cd examples\BasicUsage
call mvn compile exec:java -DskipTests
cd ..\..
pause
