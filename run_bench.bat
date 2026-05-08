@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "REPO=%USERPROFILE%\.m2\repository"
set "CP=target\classes"
set "CP=%CP%;%REPO%\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar"

"%JAVA_HOME%\bin\java.exe" "-Djava.library.path=build" -cp "%CP%" fastimage.FastImageBenchmark
