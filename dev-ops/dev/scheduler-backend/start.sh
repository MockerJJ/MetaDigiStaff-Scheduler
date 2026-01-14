#!/bin/bash

echo "Starting backend service..."

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
    exit 1
fi

echo "Backend service is running with PID: ${BACKEND_PID}"

# 保持容器运行
tail -f /app/scheduler-backend/scheduler-backend_startup.log

