# Demo1 项目

这是一个基于Spring Boot和Vue.js的前后端分离项目。

## 项目结构

```
.
├── demo1-frontend      # 前端Vue项目
└── src                 # 后端Spring Boot项目
```

## 技术栈

### 后端
- Spring Boot 3.1.0
- Spring Security
- MyBatis Plus
- MySQL
- JWT

### 前端
- Vue 3
- Vite
- Element Plus (假设使用)

## 快速开始

### 手动启动

1. 启动后端服务:
   ```
   cd C:\Users\H1314\Desktop\demo1
   ./mvnw spring-boot:run
   ```

2. 启动前端服务:
   ```
   cd C:\Users\H1314\Desktop\demo1\demo1-frontend
   npm install
   npm run dev
   ```

### 一键启动

为了方便开发，提供了以下一键启动脚本:

#### Windows系统

双击运行以下脚本:

- [start-project.bat](file:///C:/Users/H1314/Desktop/demo1/start-project.bat) - 启动项目
- [stop-project.bat](file:///C:/Users/H1314/Desktop/demo1/stop-project.bat) - 停止项目

或者在命令行中执行:
```cmd
start-project.bat
```

#### Linux/Mac系统

在终端中执行:
```bash
chmod +x start-project.sh
./start-project.sh
```

要停止服务:
```bash
./stop-project.sh
```

## 访问应用

- 前端地址: http://localhost:3002 (如果3000/3001端口被占用会自动选择其他端口)
- 后端API: http://localhost:8082

## 注意事项

1. 确保已经安装Java 17+和Node.js
2. 确保MySQL服务正在运行
3. 数据库连接配置在[src/main/resources/application.properties](file:///C:/Users/H1314/Desktop/demo1/src/main/resources/application.properties)