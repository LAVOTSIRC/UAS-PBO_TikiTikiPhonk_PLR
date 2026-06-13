@echo off
title TikiTikiPhonk Launcher
echo ============================================
echo  TikiTikiPhonk - Starting Backend + Frontend
echo ============================================
echo.

:: Start Backend (Spring Boot)
echo [1/2] Starting Backend on http://localhost:8080 ...
start "TikiTikiPhonk Backend" cmd /c "cd /d %~dp0backend && .\mvnw.cmd spring-boot:run"

:: Wait a moment for backend to init
ping 127.0.0.1 -n 6 >nul

:: Start Frontend (JavaFX)
echo [2/2] Starting Frontend ...
start "TikiTikiPhonk Frontend" cmd /c "cd /d %~dp0frontend && ..\backend\mvnw.cmd javafx:run"

echo.
echo Both applications are starting in separate windows.
echo Close each window to stop the corresponding process.
echo.
pause
