@echo off
echo ==========================================
echo   Nutrition Tracker API - Run Script
echo ==========================================

REM Set JAVA_HOME to the working JDK found
set "JAVA_HOME=C:\Program Files (x86)\Screaming Frog SEO Spider\jre"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Check if java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found. Please check JAVA_HOME.
    pause
    exit /b 1
)

echo [INFO] Using Java from: %JAVA_HOME%
java -version
echo.

echo [INFO] Starting Spring Boot Application...
echo.

REM Run the JAR file
if exist "target\nutrition-tracker-api-1.0.0-SNAPSHOT.jar" (
    java -jar target\nutrition-tracker-api-1.0.0-SNAPSHOT.jar
) else (
    echo [ERROR] JAR file not found!
    echo Please run build.bat first.
    pause
    exit /b 1
)

pause
