# 一键启动「若依 + Redis + 若依前端（Vue3）」
# 前置：MySQL 8 已启动（本机服务名 MySQL84），且 ry-vue 库已导入 ruoyi/backend/sql 下的脚本
# 用法：右键「使用 PowerShell 运行」，或在终端执行 .\start-ruoyi.ps1
$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host '==============================' -ForegroundColor Cyan
Write-Host ' 1/3 启动 Redis (6379)' -ForegroundColor Cyan
Write-Host '==============================' -ForegroundColor Cyan
$redisDir = Join-Path $root 'tools\redis\Redis-8.10.3-Windows-x64-msys2'
$redisExe = Join-Path $redisDir 'redis-server.exe'
$redisCli = Join-Path $redisDir 'redis-cli.exe'
$running = $false
try { $running = (Test-NetConnection -ComputerName 127.0.0.1 -Port 6379 -InformationLevel Quiet -WarningAction SilentlyContinue) } catch {}
if ($running) {
    Write-Host 'Redis 已在运行' -ForegroundColor Green
} elseif (Test-Path $redisExe) {
    Start-Process -FilePath $redisExe -WorkingDirectory $redisDir -ArgumentList 'redis.conf' -WindowStyle Minimized
    Start-Sleep -Seconds 3
    if (Test-Path $redisCli) { & $redisCli -p 6379 ping }
} else {
    Write-Host '未找到 redis-server.exe，请先安装 Redis' -ForegroundColor Red
}

Write-Host ''
Write-Host '==============================' -ForegroundColor Cyan
Write-Host ' 2/3 启动若依后端 (8080)' -ForegroundColor Cyan
Write-Host '==============================' -ForegroundColor Cyan
$beDir = Join-Path $root 'ruoyi\backend'
$jar = Join-Path $beDir 'ruoyi-admin\target\ruoyi-admin.jar'
$port8080 = $false
try { $port8080 = (Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue) } catch {}
if ($port8080) {
    Write-Host '8080 已被占用（若依后端可能已在运行）' -ForegroundColor Yellow
} else {
    if (-not (Test-Path $jar)) {
        Write-Host '未找到 jar，正在用 Maven 打包（首次较慢）...' -ForegroundColor Yellow
        Push-Location $beDir
        & mvn -B -DskipTests package
        Pop-Location
    }
    Start-Process -FilePath 'java' -WorkingDirectory $beDir `
        -ArgumentList '-Dfile.encoding=UTF-8', '-jar', $jar -WindowStyle Minimized
    Write-Host '后端启动中，约 20 秒后可用：http://localhost:8080' -ForegroundColor Green
}

Write-Host ''
Write-Host '==============================' -ForegroundColor Cyan
Write-Host ' 3/3 启动若依前端 (1024)' -ForegroundColor Cyan
Write-Host '==============================' -ForegroundColor Cyan
$uiDir = Join-Path $root 'ruoyi\ui'
Push-Location $uiDir
if (-not (Test-Path 'node_modules')) {
    Write-Host '首次运行，安装前端依赖（约 1~3 分钟）...' -ForegroundColor Yellow
    npm install --no-audit --no-fund
}
Pop-Location

Write-Host ''
Write-Host '账号：admin / admin123' -ForegroundColor Yellow
Write-Host '若依管理端：http://localhost:1024' -ForegroundColor Green
Write-Host '成绩管理菜单位置：成绩管理 → 月考成绩' -ForegroundColor Green
Write-Host ''
Push-Location $uiDir
npm run dev -- --port 1024
Pop-Location
