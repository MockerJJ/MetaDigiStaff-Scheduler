# =====================
# Backend Image (Java Spring Boot)
# - Builds Java backend with Maven
# - Runs on port 8080
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

# 复制后端构建产物
COPY --from=backend-builder /app/target /app/scheduler-backend/target
COPY scheduler-backend/start.sh /app/scheduler-backend/
RUN chmod +x /app/scheduler-backend/start.sh

# 复制启动脚本
WORKDIR /app
COPY dev-ops/dev/scheduler-backend/start.sh .
RUN chmod +x start.sh

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=3 \
    CMD curl -f --max-time 5 http://localhost:8080/web/health || exit 1

# 启动后端服务
CMD ["/bin/bash", "-c", "./start.sh"]

