# 停止项目脚本

Write-Host "正在停止项目服务..." -ForegroundColor Yellow
Write-Host ""

# 停止Spring Boot后端服务
Write-Host "正在停止后端服务..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*demo1*" -or $_.CommandLine -like "*spring-boot*"
}

if ($javaProcesses) {
    foreach ($process in $javaProcesses) {
        Write-Host "终止进程: $($process.Id) - $($process.ProcessName)" -ForegroundColor Yellow
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
    Write-Host "后端服务已停止" -ForegroundColor Green
} else {
    # 尝试使用jps查找
    try {
        $jpsOutput = jps -l | Select-String "demo1"
        if ($jpsOutput) {
            $pid = ($jpsOutput -split '\s+')[0]
            Write-Host "终止Spring Boot进程: $pid" -ForegroundColor Yellow
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "后端服务已停止" -ForegroundColor Green
        } else {
            Write-Host "未找到后端服务进程" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "未找到后端服务进程" -ForegroundColor Yellow
    }
}

# 停止前端Vite服务
Write-Host "正在停止前端服务..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
    $_.Path -like "*demo1-frontend*" -or $_.CommandLine -like "*vite*"
}

if ($nodeProcesses) {
    foreach ($process in $nodeProcesses) {
        Write-Host "终止进程: $($process.Id) - $($process.ProcessName)" -ForegroundColor Yellow
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
    Write-Host "前端服务已停止" -ForegroundColor Green
} else {
    # 尝试查找所有node进程（谨慎使用）
    $allNodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
    if ($allNodeProcesses) {
        Write-Host "找到 $($allNodeProcesses.Count) 个Node.js进程，正在终止..." -ForegroundColor Yellow
        $allNodeProcesses | ForEach-Object {
            try {
                $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)").CommandLine
                if ($cmdLine -like "*vite*" -or $cmdLine -like "*demo1-frontend*") {
                    Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
                    Write-Host "已终止进程: $($_.Id)" -ForegroundColor Yellow
                }
            } catch {
                # 忽略错误
            }
        }
        Write-Host "前端服务已停止" -ForegroundColor Green
    } else {
        Write-Host "未找到前端服务进程" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "服务已全部停止" -ForegroundColor Green
Read-Host "按Enter键退出"

