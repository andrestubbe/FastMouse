@echo off
cd /d C:\Users\andre\Documents\2026-04-28-Work-FastJava\FastMouse

echo === Testing FastMouse ===
echo.
echo Step 1: Building main JAR...
call mvn package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    exit /b 1
)

echo.
echo Step 2: Building demo...
cd examples\Demo
call mvn compile -q
if %ERRORLEVEL% NEQ 0 (
    echo Demo compile failed!
    exit /b 1
)

echo.
echo Step 3: Running demo...
call mvn exec:java -q

cd ..\..
