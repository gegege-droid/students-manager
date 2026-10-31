@echo off
setlocal
cd /d "%~dp0user-front"
echo Starting grade system user UI on http://localhost:5173 ...
call npm run dev
pause
