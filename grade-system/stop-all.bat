@echo off
setlocal
echo Stopping services on ports 8080, 8081, 5173 ...

for %%P in (8080 8081 5173) do (
  for /f "tokens=5" %%I in ('netstat -ano ^| findstr ":%%P" ^| findstr "LISTENING"') do (
    echo Killing PID %%I on port %%P
    taskkill /PID %%I /T /F >nul 2>&1
  )
)

echo.
echo Done. MySQL 3306 and Redis 6379 are left running.
pause
