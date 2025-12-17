# 一键启动项目脚本
# 同时启动前后端服务并显示端口信息

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       项目启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 端口配置
$backendPort = 8082
$frontendPort = 3000

Write-Host "正在启动项目..." -ForegroundColor Yellow
Write-Host ""

# 检查Java环境
Write-Host "检查Java环境..." -ForegroundColor Green
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java环境: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Java环境，请先安装Java 17+" -ForegroundColor Red
    Read-Host "按Enter键退出"
    exit 1
}

# 检查Node.js环境
Write-Host "检查Node.js环境..." -ForegroundColor Green
try {
    $nodeVersion = node --version
    Write-Host "Node.js版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Node.js环境，请先安装Node.js" -ForegroundColor Red
    Read-Host "按Enter键退出"
    exit 1
}

Write-Host ""

# 启动后端服务
Write-Host "正在启动后端服务..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    Set-Location $using:scriptDir
    & .\mvnw.cmd spring-boot:run
}

# 等待后端服务启动
Write-Host "等待后端服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 启动前端服务
Write-Host "正在启动前端服务..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    Set-Location "$using:scriptDir\demo1-frontend"
    npm run dev
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       项目启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "后端服务运行在: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:$backendPort" -ForegroundColor Green
Write-Host "前端服务运行在: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:$frontendPort" -ForegroundColor Green
Write-Host ""
Write-Host "注意: 如果前端端口 $frontendPort 被占用，Vite会自动选择其他可用端口" -ForegroundColor Yellow
Write-Host ""
Write-Host "要停止服务，请按 Ctrl+C 或运行 stop.ps1 脚本" -ForegroundColor Yellow
Write-Host ""

# 等待用户中断
try {
    Write-Host "服务正在运行中，按 Ctrl+C 停止服务..." -ForegroundColor Cyan
    while ($true) {
        Start-Sleep -Seconds 1
    }
} catch {
    Write-Host ""
    Write-Host "正在停止服务..." -ForegroundColor Yellow
    Stop-Job -Job $backendJob, $frontendJob -ErrorAction SilentlyContinue
    Remove-Job -Job $backendJob, $frontendJob -ErrorAction SilentlyContinue
    Write-Host "服务已停止" -ForegroundColor Green
}

