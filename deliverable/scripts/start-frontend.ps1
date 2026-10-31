# 启动前端（Vue 3 + Vite 开发服务器）
# 用法：右键“使用 PowerShell 运行”，或在终端执行 .\start-frontend.ps1
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $root 'grade-front')

if (-not (Test-Path 'node_modules')) {
    Write-Host '首次运行，正在安装依赖（约 20 秒）...' -ForegroundColor Cyan
    npm install --no-audit --no-fund
}

Write-Host ''
Write-Host '前端启动中：http://localhost:5173/' -ForegroundColor Green
Write-Host '登录账号：学生 20230101 / 123456，班主任 T001 / 123456' -ForegroundColor Yellow
Write-Host ''
npm run dev
