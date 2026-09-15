@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building Main Project (FastMouse)...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Main build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Building Benchmark Uber-JAR...
cd examples\Benchmark
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark build failed. & cd ..\.. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Official JMH Benchmarks for FastMouse...
java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED -jar target\benchmarks.jar -f 1 -i 3 -wi 2 -w 1s -r 1s -jvmArgs "-Xmx4g"

cd ..\..
pause
