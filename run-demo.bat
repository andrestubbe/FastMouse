@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building FastMouse...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

powershell -NoProfile -Command "Unblock-File -Path '%USERPROFILE%\.fastcore\native\fastmouse\*', '%~dp0src\main\resources\native\*', '%~dp0build\*' -ErrorAction SilentlyContinue" >nul 2>&1

echo 🛠  Compiling Demo...
cd examples\Demo
call mvn compile dependency:build-classpath -Dmdep.outputFile=cp.txt -DincludeScope=runtime -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Demo...
set /p CP=<cp.txt
java --enable-native-access=ALL-UNNAMED "-Djava.library.path=%~dp0src\main\resources\native;%~dp0build" -cp "target\classes;%CP%" fastmouse.Demo

cd ..\..
pause
