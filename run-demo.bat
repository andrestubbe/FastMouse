@echo off
cd /d "%~dp0"

echo ============================================
echo FastMouse Demo - Raw Input Mouse API
echo ============================================
echo.

echo [1/3] Building main project...
call mvn package -DskipTests -q
if errorlevel 1 (
    echo.
    echo [WARNING] Build had issues, continuing with existing JAR...
)
echo     OK
echo.

echo [2/3] Copying native DLL...
if not exist "src\main\resources\native" mkdir "src\main\resources\native"
copy /Y "build\fastmouse.dll" "src\main\resources\native\" >nul
echo     OK
echo.

echo [3/3] Building and running demo...
cd examples\Demo
call mvn compile -q
call mvn exec:java -q
cd ..\..

echo.
echo Demo finished.
pause
