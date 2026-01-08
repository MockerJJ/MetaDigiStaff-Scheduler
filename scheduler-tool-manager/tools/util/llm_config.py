# -*- coding: utf-8 -*-
# =====================
# LLM 配置模块
# 根据环境变量 APP_ENV 自动选择开发环境或生产环境配置
# 与 scheduler-backend 的配置保持一致
# =====================
import os
from typing import Dict, Optional


class LLMConfig:
    """LLM 配置类，根据环境变量自动选择配置"""
    
    def __init__(self):
        # 从环境变量读取环境类型
        self.app_env = os.getenv("APP_ENV", "dev")
        self.is_production = self.app_env.lower() in ["production", "prod"]
        
        # 根据环境加载配置
        if self.is_production:
            self._load_production_config()
        else:
            self._load_dev_config()
    
    def _load_dev_config(self):
        """加载开发环境配置"""
        self.base_url = os.getenv(
            "LLM_DEV_BASE_URL",
            "http://10.128.86.64:8000/serviceAgent/rest/v1/chat/completions"
        )
        self.model = os.getenv(
            "LLM_DEV_MODEL",
            "MindIE2_Qwen2.5-14B-Instruct"
        )
        self.max_tokens = int(os.getenv("LLM_DEV_MAX_TOKENS", "16384"))
        self.temperature = float(os.getenv("LLM_DEV_TEMPERATURE", "0"))
        self.max_input_tokens = int(os.getenv("LLM_DEV_MAX_INPUT_TOKENS", "100000"))
        self.app_id = os.getenv(
            "LLM_DEV_APP_ID",
            "c8ca33e7a375f17361b04b839e940267"
        )
        self.app_key = os.getenv(
            "LLM_DEV_APP_KEY",
            "fa3a932d59e1a8a7ac010a58256e1cad"
        )
        self.api_key = os.getenv("LLM_DEV_API_KEY", "")
    
    def _load_production_config(self):
        """加载生产环境配置"""
        self.base_url = os.getenv(
            "LLM_PRODUCTION_BASE_URL",
            "http://10.214.8.16:1026/v1/chat/completions"
        )
        self.model = os.getenv(
            "LLM_PRODUCTION_MODEL",
            "Qwen2.5-14B-Instruct"
        )
        self.max_tokens = int(os.getenv("LLM_PRODUCTION_MAX_TOKENS", "16384"))
        self.temperature = float(os.getenv("LLM_PRODUCTION_TEMPERATURE", "0"))
        self.max_input_tokens = int(os.getenv("LLM_PRODUCTION_MAX_INPUT_TOKENS", "100000"))
        self.api_key = os.getenv("LLM_PRODUCTION_API_KEY", "")
        self.app_id = None
        self.app_key = None
    
    def get_headers(self) -> Dict[str, str]:
        """
        根据环境构建请求头
        完全按照参考文件 llm_embeding_infra.py 的逻辑
        """
        headers = {"Content-Type": "application/json"}
        
        if self.is_production:
            # 生产环境：只使用 Content-Type header
            pass
        else:
            # 开发环境：使用 X-APP-ID 和 X-APP-KEY
            if self.app_id:
                headers["X-APP-ID"] = self.app_id
            if self.app_key:
                headers["X-APP-KEY"] = self.app_key
        
        return headers
    
    def get_base_url(self) -> str:
        """
        获取基础 URL
        根据 curl 命令，完整 URL 是：http://10.128.86.64:8000/serviceAgent/rest/v1/chat/completions
        
        OpenAI 客户端会自动在 base_url 后追加 /chat/completions
        所以如果配置的 base_url 是完整 URL，需要提取基础部分，保留到 /v1
        
        例如：
        - 输入：http://10.128.86.64:8000/serviceAgent/rest/v1/chat/completions
        - 输出：http://10.128.86.64:8000/serviceAgent/rest/v1
        - OpenAI 客户端会追加：/chat/completions
        - 最终 URL：http://10.128.86.64:8000/serviceAgent/rest/v1/chat/completions（正确）
        """
        url = self.base_url.strip()
        
        # 检查是否包含完整路径 /v1/chat/completions
        if "/v1/chat/completions" in url:
            # 提取基础部分，保留到 /v1（OpenAI 客户端会自动追加 /chat/completions）
            # 例如：http://10.128.86.64:8000/serviceAgent/rest/v1/chat/completions
            # -> http://10.128.86.64:8000/serviceAgent/rest/v1
            base_part = url.split("/v1/chat/completions")[0]
            url = base_part + "/v1"
        elif url.endswith("/chat/completions"):
            # 如果以 /chat/completions 结尾，去掉它
            url = url.rsplit("/chat/completions", 1)[0]
            # 如果去掉后不是以 /v1 结尾，需要添加
            if not url.endswith("/v1"):
                # 检查是否包含 /v1
                if "/v1" in url:
                    # 提取到 /v1（保留 /v1 之前的部分）
                    url = url.split("/v1")[0] + "/v1"
                else:
                    # 如果没有 /v1，添加它
                    url = url.rstrip("/") + "/v1"
        elif "/chat/completions" in url:
            # 如果包含 /chat/completions（但不是以它结尾），提取基础部分
            url = url.split("/chat/completions")[0]
            # 确保以 /v1 结尾
            if not url.endswith("/v1"):
                if "/v1" in url:
                    url = url.split("/v1")[0] + "/v1"
                else:
                    url = url.rstrip("/") + "/v1"
        
        # 确保返回的 URL 不以 / 结尾（除了协议部分）
        result = url.rstrip("/")
        return result
    
    def get_model(self) -> str:
        """获取模型名称"""
        return self.model
    
    def get_api_key(self) -> Optional[str]:
        """获取 API Key（生产环境使用）"""
        return self.api_key if self.api_key else None


# 全局配置实例
_llm_config: Optional[LLMConfig] = None


def get_llm_config() -> LLMConfig:
    """获取 LLM 配置实例（单例模式）"""
    global _llm_config
    if _llm_config is None:
        _llm_config = LLMConfig()
    return _llm_config

