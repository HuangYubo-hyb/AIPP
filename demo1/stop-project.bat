@echo off
echo 正在停止项目服务...

REM 查找并终止Spring Boot应用进程
for /f "tokens=1" %%i in ('jps -l ^| findstr demo1') do (
    echo 终止Spring Boot进程: %%i
    taskkill /f /pid %%i
)

REM 查找并终止Vite开发服务器进程
tasklist /fi "imagename eq node.exe" /fo csv 2>nul | findstr "vite" >nul
if %errorlevel% equ 0 (
    echo 终止Vite开发服务器进程
    taskkill /f /im node.exe
) else (
    echo 尝试终止所有Node.js进程
    taskkill /f /im node.exe 2>nul
)

echo.
echo 服务已停止
pause