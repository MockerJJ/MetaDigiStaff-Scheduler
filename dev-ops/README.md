# Docker Compose 部署指南

## 快速开始

### 1. 构建并启动所有服务

在 `dev-ops` 目录下执行：

```bash
docker-compose up -d --build
```

这个命令会：
- 构建两个镜像（backend-tool 和 ui）
- 启动所有服务容器
- 在后台运行（-d 参数）

### 2. 查看服务状态

```bash
# 查看运行中的容器
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务的日志
docker-compose logs -f backend-tool
docker-compose logs -f ui
```

### 3. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（注意：会删除数据）
docker-compose down -v
```

## 服务说明

### Backend + Tool 服务
- **容器名**: `scheduler-backend-tool`
- **端口映射**:
  - Backend API: `8080:8080`
  - Tool Manager: `1601:1601`
  - MCP Client: `8188:8188`
- **数据卷**: `scheduler-data` (挂载到 `/data/scheduler-tool-manager`)

### UI 服务
- **容器名**: `scheduler-ui`
- **端口映射**: `3000:80`
- **访问地址**: http://localhost:3000

## 常用命令

```bash
# 重新构建并启动
docker-compose up -d --build

# 只构建不启动
docker-compose build

# 重启服务
docker-compose restart

# 查看资源使用情况
docker-compose top
```

## 故障排查

如果服务启动失败：

1. **查看日志**
   ```bash
   docker-compose logs backend-tool
   docker-compose logs ui
   ```

2. **检查容器状态**
   ```bash
   docker-compose ps
   ```

3. **进入容器调试**
   ```bash
   docker-compose exec backend-tool bash
   docker-compose exec ui sh
   ```

4. **重新构建**
   ```bash
   docker-compose build --no-cache
   docker-compose up -d
   ```

## 注意事项

- 首次启动可能需要几分钟时间构建镜像
- 确保端口 3000、8080、1601、8188 未被占用
- 数据会持久化在 Docker volume `scheduler-data` 中

