# Scheduler Tool Manager

`python >= 3.11`

## 项目结构

```
scheduler-tool-manager/
├── tools/                              # 工具包（原 genie_tool）
│   ├── api/                            # api 服务
│   ├── db/                             # 数据库相关
│   ├── model/                          # 协议和 DataClass
│   ├── prompt/                         # Prompt 仓库
│   ├── tool/                           # 工具执行逻辑
│   │   ├── mrag/                       # MRAG 相关工具
│   │   ├── analysis_component/         # 分析组件
│   │   ├── search_component/           # 搜索组件
│   │   └── table_rag/                  # 表 RAG
│   └── util/                           # 工具类
├── test/                               # 测试文件
├── .env_template                       # 环境变量模板
├── server.py                           # FastAPI 服务启动
├── start.sh                            # 启动脚本
├── pyproject.toml                      # 项目配置
└── uv.lock                             # 依赖锁定文件

```

## 项目启动

### Python 环境和依赖安装

```bash
# 安装 uv（如果还没有安装）
pip install uv

# 进入项目目录
cd scheduler-tool-manager

# 同步依赖
uv sync

# 激活虚拟环境
source .venv/bin/activate
```

```bash
# 安装poetry来管理toml
pip install poetry

# 进入项目目录
cd scheduler-tool-manager

# 管理依赖
poetry install
```

### 首次启动 - 初始化数据库

首次启动需要初始化数据库（后续不再需要）：

```bash
cd scheduler-tool-manager

# 激活虚拟环境后执行
python -m tools.db.db_engine
```

### 启动服务

```bash
cd scheduler-tool-manager

# 复制环境变量模板
cp .env_template .env
# 编辑 .env 文件，填写必要的环境变量

# 使用 uv 运行服务
uv run python server.py

# 或者激活虚拟环境后运行
source .venv/bin/activate
python server.py
```

### MRAG 知识库初始化

如果需要使用 MRAG 功能，需要初始化知识库：

```bash
cd scheduler-tool-manager

# 激活虚拟环境后执行
python -m tools.tool.mrag.init.init_db
```

## 环境变量配置

复制 `.env_template` 到 `.env` 并配置以下环境变量：

- `APP_ENV`: 应用环境（dev/production）
- `LLM_DEV_BASE_URL`: 开发环境 LLM 服务地址
- `LLM_DEV_APP_ID`: 开发环境应用 ID
- `LLM_DEV_APP_KEY`: 开发环境应用 Key
- `LLM_PRODUCTION_BASE_URL`: 生产环境 LLM 服务地址
- `LLM_PRODUCTION_API_KEY`: 生产环境 API Key

更多环境变量请参考 `.env_template` 文件。