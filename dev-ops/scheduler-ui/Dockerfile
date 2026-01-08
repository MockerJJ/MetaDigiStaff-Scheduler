# 前端构建阶段
FROM docker.m.daocloud.io/library/node:20-alpine as frontend-builder
WORKDIR /app
RUN npm install -g pnpm
COPY ui/package.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN pnpm install
COPY ui/ .
RUN pnpm build

# 最终运行阶段
FROM docker.m.daocloud.io/library/node:20-alpine

# 安装 pnpm 和 wget（用于健康检查）
RUN npm install -g pnpm && \
    apk add --no-cache wget

# 设置工作目录
WORKDIR /app

# 复制前端构建产物（严格按照原有 Dockerfile 的方式）
COPY --from=frontend-builder /app/dist /app/ui/dist
COPY --from=frontend-builder /app/package.json /app/ui/package.json
COPY --from=frontend-builder /app/node_modules /app/ui/node_modules

# 切换到 UI 目录
WORKDIR /app/ui

EXPOSE 3000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

# 启动
CMD ["sh", "-c", "export SERVICE_BASE_URL=${SERVICE_BASE_URL:-http://scheduler-backend-tool:8080} && pnpm preview --host 0.0.0.0 --port 3000"]

