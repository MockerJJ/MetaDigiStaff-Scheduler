#!/bin/bash

# 添加调试信息
echo "当前工作目录: $(pwd)"
echo "目录列表:"
ls -la

# 开始启动前端服务
echo "尝试进入ui目录..."
if [ -d "ui" ]; then
    cd ui
    echo "当前目录: $(pwd)"
    echo "ui目录内容:"
    ls -la
    # UI目录没有start.sh，使用npm运行预览服务器
    if [ -f "package.json" ]; then
        echo "启动UI预览服务器"
        echo "修改为监听所有接口(0.0.0.0)并使用端口3000"
        # 设置 SERVICE_BASE_URL 环境变量（默认指向后端服务）
        export SERVICE_BASE_URL=${SERVICE_BASE_URL:-"http://localhost:8080"}
        echo "SERVICE_BASE_URL=${SERVICE_BASE_URL}"
        # 使用--host和--port参数明确指定监听地址和端口
        SERVICE_BASE_URL=${SERVICE_BASE_URL} pnpm preview --host 0.0.0.0 --port 3000 &
        # 等待服务启动
        sleep 3
        # 检查服务是否正在监听（使用更通用的方法，不依赖netstat）
        echo "检查服务监听状态:"
        if [ -n "$(ps aux | grep 'vite preview' | grep -v grep)" ]; then
            echo "Vite预览服务器已启动"
        else
            echo "警告: Vite预览服务器可能未正确启动"
        fi
    else
        echo "错误: ui/package.json文件不存在"
    fi
    cd ..
else
    echo "错误: ui目录不存在"
fi

# 开始启动后端服务
echo "尝试进入scheduler-backend目录..."
if [ -d "scheduler-backend" ]; then
    cd scheduler-backend
    echo "当前目录: $(pwd)"
    echo "scheduler-backend目录内容:"
    ls -la
    if [ -f "start.sh" ]; then
        echo "执行scheduler-backend/start.sh"
        sh start.sh
    else
        echo "错误: scheduler-backend/start.sh文件不存在，尝试直接启动jar文件"
        if [ -f "app.jar" ]; then
            echo "启动后端应用"
            java -jar app.jar &
        else
            echo "错误: scheduler-backend/app.jar文件不存在"
        fi
    fi
    cd ..
else
    echo "错误: scheduler-backend目录不存在"
fi

# 开始启动工具服务
echo "尝试进入scheduler-tool-manager目录..."
if [ -d "scheduler-tool-manager" ]; then
    cd scheduler-tool-manager
    echo "当前目录: $(pwd)"
    echo "scheduler-tool-manager目录内容:"
    ls -la
    
    # 处理环境变量
    echo "处理环境变量..."
    if [ -f ".env_template" ] && [ ! -f ".env" ]; then
        cp .env_template .env
        echo "已复制.env_template到.env"
    fi
    
    if [ -f ".env" ]; then
        echo "更新.env文件并导出环境变量..."
        
        # 如果环境变量已经设置，则更新.env文件
        if [ ! -z "$OPENAI_API_KEY" ]; then
            echo "使用环境变量OPENAI_API_KEY更新.env文件"
            sed -i "s|OPENAI_API_KEY=.*|OPENAI_API_KEY=${OPENAI_API_KEY}|g" .env
        fi
        
        if [ ! -z "$OPENAI_BASE_URL" ]; then
            echo "使用环境变量OPENAI_BASE_URL更新.env文件"
            sed -i "s|OPENAI_BASE_URL=.*|OPENAI_BASE_URL=${OPENAI_BASE_URL}|g" .env
        fi
        
        # 从.env文件导出所有环境变量到当前shell
        echo "从.env文件导出环境变量..."
        export $(grep -v '^#' .env | xargs)
        
        # 确保环境变量在全局范围内可用
        echo "将环境变量添加到/etc/environment以确保在所有shell会话中可用"
        echo "OPENAI_API_KEY=${OPENAI_API_KEY}" >> /etc/environment
        echo "OPENAI_BASE_URL=${OPENAI_BASE_URL}" >> /etc/environment
        echo "OPENAI_API_BASE=${OPENAI_BASE_URL}" >> /etc/environment
        
        echo "环境变量已导出: OPENAI_API_KEY=${OPENAI_API_KEY}, OPENAI_BASE_URL=${OPENAI_BASE_URL}, OPENAI_API_BASE=${OPENAI_BASE_URL}"
    else
        echo "警告: .env文件不存在，无法导出环境变量"
    fi
    
    # 初始化数据库
    echo "初始化数据库..."
    if [ -d ".venv" ]; then
        . .venv/bin/activate
        echo "执行数据库初始化..."
        python -m tools.db.db_engine
        echo "数据库初始化完成"
    else
        echo "错误: 虚拟环境不存在，无法初始化数据库"
    fi
    
    if [ -f "start.sh" ]; then
        echo "执行scheduler-tool-manager/start.sh"
        sh start.sh &
    else
        echo "错误: scheduler-tool-manager/start.sh文件不存在"
    fi
    cd ..
else
    echo "错误: scheduler-tool-manager目录不存在"
fi

# 开始启动MCP服务
echo "尝试进入scheduler-mcp-client目录..."
if [ -d "scheduler-mcp-client" ]; then
    cd scheduler-mcp-client
    echo "当前目录: $(pwd)"
    echo "scheduler-mcp-client目录内容:"
    ls -la
    if [ -f "start.sh" ]; then
        echo "执行scheduler-mcp-client/start.sh"
        sh start.sh &
    else
        echo "错误: scheduler-mcp-client/start.sh文件不存在"
    fi
    cd ..
else
    echo "错误: scheduler-mcp-client目录不存在"
fi

# 保持容器运行
echo "所有服务已启动，保持容器运行..."
tail -f /dev/null
