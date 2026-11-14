@echo off
setlocal
cd /d "%~dp0backend\ruoyi-ui"
set "NODE_OPTIONS=--openssl-legacy-provider"
echo Starting RuoYi admin UI on http://localhost:8081 ...
call npm run dev
pause
