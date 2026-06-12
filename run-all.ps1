Write-Host "============================================" -ForegroundColor Cyan
Write-Host " TikiTikiPhonk - Starting Backend + Frontend" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Start Backend (Spring Boot)
Write-Host "[1/2] Starting Backend on http://localhost:8080 ..." -ForegroundColor Yellow
$backendJob = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$rootDir\backend`" && .\mvnw.cmd spring-boot:run" -WindowStyle Normal -PassThru -NoNewWindow:$false

Write-Host "     Backend PID: $($backendJob.Id)" -ForegroundColor DarkGray

# Wait for backend to start initializing
Start-Sleep -Seconds 5

# Start Frontend (JavaFX)
Write-Host "[2/2] Starting Frontend ..." -ForegroundColor Yellow
$frontendJob = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$rootDir\frontend`" && ..\backend\mvnw.cmd javafx:run" -WindowStyle Normal -PassThru -NoNewWindow:$false

Write-Host "     Frontend PID: $($frontendJob.Id)" -ForegroundColor DarkGray
Write-Host ""
Write-Host "Both applications are starting in separate windows." -ForegroundColor Green
Write-Host "Close each window to stop the corresponding process." -ForegroundColor Green
Write-Host ""
Write-Host "Press any key to exit this launcher (processes will continue running)..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
