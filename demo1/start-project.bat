@echo off
echo 正在启动项目...

REM 启动后端服务
echo 正在启动后端服务...
start "后端服务" /D "C:\Users\H1314\Desktop\demo1" cmd /k "./mvnw spring-boot:run"

REM 等待几秒钟让后端服务启动
timeout /t 10 /nobreak >nul

REM 启动前端服务
echo 正在启动前端服务...
start "前端服务" /D "C:\Users\H1314\Desktop\demo1\demo1-frontend" cmd /k "npm run dev"

echo.
echo 项目启动完成！
echo 后端服务运行在: http://localhost:8082
echo 前端服务运行在: http://localhost:3002 (如果3000/3001被占用会自动选择其他端口)
echo.
echo 请稍等片刻直到两个服务完全启动后再访问。
echo 要停止服务，请运行 stop-project.bat
pause