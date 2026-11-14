@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo  Grade System - start all services
echo  Backend : http://localhost:8080
echo  Admin   : http://localhost:8081
echo  User    : http://localhost:5173
echo ============================================
echo.
echo Please make sure MySQL 3306 and Redis 6379 are running.
echo.

start "RuoYi Backend 8080" cmd /k "%~dp0start-backend.bat"
echo Waiting 15 seconds for backend ...
timeout /t 15 /nobreak >nul

start "RuoYi Admin 8081" cmd /k "%~dp0start-admin-ui.bat"
start "Grade User 5173" cmd /k "%~dp0start-user-front.bat"

echo.
echo All windows started.
echo Admin login: admin / admin123
echo Student    : 2021001 / 123456
echo Teacher    : T001 / 123456
echo.
pause
