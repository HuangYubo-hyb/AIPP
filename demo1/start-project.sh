#!/bin/bash

echo "正在启动项目..."

# 后台启动后端服务
echo "正在启动后端服务..."
cd "$(dirname "$0")"
./mvnw spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!

# 等待几秒钟让后端服务启动
sleep 10

# 后台启动前端服务
echo "正在启动前端服务..."
cd demo1-frontend
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo ""
echo "项目启动完成！"
echo "后端服务运行在: http://localhost:8082"
echo "前端服务运行在: http://localhost:3000 (如果3000被占用会自动选择其他端口)"
echo ""
echo "后端日志保存在: backend.log"
echo "前端日志保存在: frontend.log"
echo "要停止服务，请运行 stop-project.sh 脚本"

# 创建停止脚本
cat > stop-project.sh << 'EOF'
#!/bin/bash
echo "正在停止项目服务..."

# 查找并终止相关进程
pids=$(ps aux | grep -E "(spring-boot|vite)" | grep -v grep | awk '{print $2}')

if [ ! -z "$pids" ]; then
    echo "正在终止进程: $pids"
    kill $pids
    echo "服务已停止"
else
    echo "未找到相关进程"
fi
EOF

chmod +x stop-project.sh

echo "已创建停止脚本: stop-project.sh"