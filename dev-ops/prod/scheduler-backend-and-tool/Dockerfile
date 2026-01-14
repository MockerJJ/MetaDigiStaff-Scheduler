# =====================
# Backend + Tool Manager + MCP Client Image
# - Builds Java backend (Spring Boot) with Maven
# - Installs Python tool manager (FastAPI) with uv
# - Installs Python MCP client (FastAPI) with uv
# - Runs all services in one container:
#   * Backend on 8080
#   * Tool Manager on 1601
#   * MCP Client on 8188
# =====================

# ---------- Backend build stage (Java) ----------
FROM docker.m.daocloud.io/library/maven:3.8-openjdk-17 AS backend-builder
WORKDIR /app
COPY scheduler-backend/pom.xml .
COPY scheduler-backend/src ./src
COPY scheduler-backend/build.sh scheduler-backend/start.sh ./
RUN sed -i 's/\r$//' build.sh start.sh && \
    chmod +x build.sh start.sh && \
    sh build.sh

# ---------- Python base stage (for tool services) ----------
FROM docker.m.daocloud.io/library/python:3.13-bookworm AS python-base
WORKDIR /app

# 配置 Debian 镜像源
RUN rm /etc/apt/sources.list.d/* && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm main contrib non-free non-free-firmware' > /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian-security bookworm-security main contrib non-free non-free-firmware' >> /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm-updates main contrib non-free non-free-firmware' >> /etc/apt/sources.list

RUN apt-get clean && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    netcat-openbsd \
    procps \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip uv

# ---------- Final runtime image ----------
FROM docker.m.daocloud.io/library/python:3.13-bookworm
WORKDIR /app
ENV TZ=Asia/Shanghai

# 配置 Debian 镜像源
RUN rm /etc/apt/sources.list.d/* && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm main contrib non-free non-free-firmware' > /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian-security bookworm-security main contrib non-free non-free-firmware' >> /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm-updates main contrib non-free non-free-firmware' >> /etc/apt/sources.list

# 安装系统依赖
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    netcat-openbsd \
    procps \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 复制 Python 运行时
COPY --from=python-base /usr/local/lib/python3.13 /usr/local/lib/python3.13
COPY --from=python-base /usr/local/bin/python3.13 /usr/local/bin/python3.13
COPY --from=python-base /usr/local/bin/pip3.13 /usr/local/bin/pip3.13
COPY --from=python-base /usr/local/bin/uv /usr/local/bin/uv

# 复制后端构建产物
COPY --from=backend-builder /app/target /app/scheduler-backend/target
COPY scheduler-backend/start.sh /app/scheduler-backend/
RUN chmod +x /app/scheduler-backend/start.sh

# 安装 scheduler-tool-manager
WORKDIR /app/scheduler-tool-manager
COPY scheduler-tool-manager/pyproject.toml scheduler-tool-manager/uv.lock ./
COPY scheduler-tool-manager/tools ./tools
COPY scheduler-tool-manager/server.py scheduler-tool-manager/start.sh scheduler-tool-manager/.env_template ./
RUN sed -i 's/\r$//' start.sh || true && \
    chmod +x start.sh && \
    uv venv .venv && \
    UV_DEFAULT_INDEX=${UV_DEFAULT_INDEX:-https://pypi.tuna.tsinghua.edu.cn/simple} uv sync --python .venv/bin/python && \
    mkdir -p /data/scheduler-tool-manager && \
    cp .env_template .env && \
    .venv/bin/python -m tools.db.db_engine

# 安装 scheduler-mcp-client
WORKDIR /app/scheduler-mcp-client
COPY scheduler-mcp-client/pyproject.toml scheduler-mcp-client/uv.lock ./
COPY scheduler-mcp-client/app ./app
COPY scheduler-mcp-client/main.py scheduler-mcp-client/server.py scheduler-mcp-client/start.sh ./
RUN sed -i 's/\r$//' start.sh || true && \
    chmod +x start.sh && \
    uv venv .venv && \
    UV_DEFAULT_INDEX=${UV_DEFAULT_INDEX:-https://pypi.tuna.tsinghua.edu.cn/simple} uv sync --python .venv/bin/python

# 复制启动脚本
WORKDIR /app
COPY dev-ops/prod/scheduler-backend-and-tool/start_backend_tool.sh .
RUN chmod +x start_backend_tool.sh

# 暴露端口
EXPOSE 8080 1601 8188

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=3 \
    CMD curl -f --max-time 5 http://localhost:8080/web/health || exit 1

# 启动所有服务
CMD ["/bin/bash", "-c", "./start_backend_tool.sh"]
