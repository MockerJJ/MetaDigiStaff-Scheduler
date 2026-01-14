#!/bin/bash

echo "Starting backend, tool manager and mcp-client services..."

# Start backend (Java)
echo "Launching backend on 8080..."
cd /app/scheduler-backend
if [ -f "start.sh" ]; then
    sh start.sh &
    BACKEND_PID=$!
    echo "Backend started with PID: ${BACKEND_PID}"
    
    # 等待后端服务启动（最多等待 120 秒）
    echo "Waiting for backend to be ready..."
    for i in {1..60}; do
        if curl -f --max-time 2 http://localhost:8080/web/health > /dev/null 2>&1; then
            echo "Backend is ready! (took ${i}*2 seconds)"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "Warning: Backend may not be fully ready after 120 seconds"
        fi
        sleep 2
    done
else
    echo "Error: scheduler-backend/start.sh not found"
fi
cd /app

# Start tool manager (Python FastAPI)
echo "Launching tool manager on 1601..."
cd /app/scheduler-tool-manager
if [ -f "start.sh" ]; then
    sh start.sh > /app/tool.log 2>&1 &
    TOOL_PID=$!
    echo "Tool manager started with PID: ${TOOL_PID}"
else
    echo "Error: scheduler-tool-manager/start.sh not found"
fi
cd /app

# Start mcp-client (Python FastAPI)
echo "Launching mcp-client on 8188..."
cd /app/scheduler-mcp-client
if [ -f "start.sh" ]; then
    sh start.sh > /app/mcp-client.log 2>&1 &
    MCP_PID=$!
    echo "MCP client started with PID: ${MCP_PID}"
else
    echo "Error: scheduler-mcp-client/start.sh not found"
fi
cd /app

echo "Backend PID: ${BACKEND_PID}, Tool PID: ${TOOL_PID}, MCP Client PID: ${MCP_PID}"
echo "All services started. Keeping container running..."

# 保持容器运行（不要使用 wait，因为 wait -n 会在任意进程退出时结束脚本）
tail -f /dev/null

