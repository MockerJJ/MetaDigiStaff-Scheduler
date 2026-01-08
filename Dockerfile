# 前端构建阶段
FROM docker.m.daocloud.io/library/node:20-alpine as frontend-builder
WORKDIR /app
RUN npm install -g pnpm
COPY ui/package.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN pnpm install
COPY ui/ .
RUN pnpm build

# 后端构建阶段
FROM docker.m.daocloud.io/library/maven:3.8-openjdk-17 as backend-builder
WORKDIR /app
COPY scheduler-backend/pom.xml .
COPY scheduler-backend/src ./src
COPY scheduler-backend/build.sh scheduler-backend/start.sh ./
RUN sed -i 's/\r$//' build.sh start.sh && \
    chmod +x build.sh start.sh
RUN sh build.sh

# Python 环境准备阶段
FROM docker.m.daocloud.io/library/python:3.13-bookworm as python-base
WORKDIR /app

RUN rm /etc/apt/sources.list.d/* && echo 'deb https://mirrors.aliyun.com/debian/ bookworm main contrib non-free non-free-firmware' \
      > /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian-security bookworm-security main contrib non-free non-free-firmware' \
      >> /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm-updates main contrib non-free non-free-firmware' \
      >> /etc/apt/sources.list

RUN apt-get clean && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    netcat-openbsd \
    procps \
    curl \
    && rm -rf /var/lib/apt/lists/*
RUN pip install --upgrade pip && pip install uv

# 最终运行阶段
FROM docker.m.daocloud.io/library/python:3.13-bookworm

# 安装系统依赖
RUN rm /etc/apt/sources.list.d/* && echo 'deb https://mirrors.aliyun.com/debian/ bookworm main contrib non-free non-free-firmware' \
      > /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian-security bookworm-security main contrib non-free non-free-firmware' \
      >> /etc/apt/sources.list && \
    echo 'deb https://mirrors.aliyun.com/debian/ bookworm-updates main contrib non-free non-free-firmware' \
      >> /etc/apt/sources.list
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    netcat-openbsd \
    procps \
    poppler-utils \
    curl \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/* \
    && npm install -g pnpm

# 设置工作目录
WORKDIR /app

# 复制前端构建产物
COPY --from=frontend-builder /app/dist /app/ui/dist
COPY --from=frontend-builder /app/package.json /app/ui/package.json
COPY --from=frontend-builder /app/node_modules /app/ui/node_modules

# 复制后端构建产物
COPY --from=backend-builder /app/target /app/scheduler-backend/target
COPY scheduler-backend/start.sh /app/scheduler-backend/
RUN chmod +x /app/scheduler-backend/start.sh

# 复制 Python 工具和依赖
COPY --from=python-base /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=python-base /usr/local/bin/uv /usr/local/bin/uv


# 复制 scheduler-mcp-client
WORKDIR /app/scheduler-mcp-client
COPY scheduler-mcp-client/pyproject.toml scheduler-mcp-client/uv.lock ./
COPY scheduler-mcp-client/app ./app
COPY scheduler-mcp-client/main.py scheduler-mcp-client/server.py scheduler-mcp-client/start.sh ./
RUN chmod +x start.sh && \
    uv venv .venv && \
    . .venv/bin/activate && \
    export UV_DEFAULT_INDEX="https://pypi.tuna.tsinghua.edu.cn/simple" && uv sync

# 复制 scheduler-tool-manager
WORKDIR /app/scheduler-tool-manager
COPY scheduler-tool-manager/pyproject.toml scheduler-tool-manager/uv.lock ./
COPY scheduler-tool-manager/tools ./tools
COPY scheduler-tool-manager/server.py scheduler-tool-manager/start.sh scheduler-tool-manager/.env_template ./

# 创建虚拟环境并安装依赖
RUN chmod +x start.sh && \
    uv venv .venv && \
    . .venv/bin/activate && \
    export UV_DEFAULT_INDEX="https://pypi.tuna.tsinghua.edu.cn/simple" && uv sync && \
    mkdir -p /data/scheduler-tool-manager && \
    cp .env_template .env && \
    .venv/bin/python -m tools.db.db_engine

# 设置数据卷
VOLUME ["/data/scheduler-tool"]

# 复制统一启动脚本
WORKDIR /app
COPY start_scheduler_dockerfile.sh .
RUN chmod +x start_scheduler_dockerfile.sh

EXPOSE 3000 8080 1601

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:3000 || exit 1

# 启动所有服务
CMD ["/bin/bash", "-c", "./start_scheduler_dockerfile.sh"]
