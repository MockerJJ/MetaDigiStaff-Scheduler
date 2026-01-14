#!/bin/bash

echo "Starting tool manager and mcp-client services..."

# Start tool manager (Python FastAPI)
echo "Launching tool manager on 1601..."
cd /app/scheduler-tool-manager
if [ -f "start.sh" ]; then
    sh start.sh > /app/tool.log 2>&1 &
    TOOL_PID=$!
    echo "Tool manager started with PID: ${TOOL_PID}"
else
    echo "Error: scheduler-tool-manager/start.sh not found"
    exit 1
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
    exit 1
fi
cd /app

echo "Tool Manager PID: ${TOOL_PID}, MCP Client PID: ${MCP_PID}"
echo "All services started. Keeping container running..."

# 保持容器运行
tail -f /app/tool.log /app/mcp-client.log

