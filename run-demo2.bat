@echo off
cd /d "%~dp0"

echo ============================================
echo FastMouse Demo2 - Undecorated JFrame
echo ============================================
echo.

call mvn package -DskipTests -q
if errorlevel 1 (
    echo [WARNING] Build had issues, continuing...
)
echo     OK
echo.

echo [2/2] Building and running Demo2...
cd examples\Demo2
call mvn compile exec:java -q
cd ..\..

echo.
echo Demo2 finished.
pause
