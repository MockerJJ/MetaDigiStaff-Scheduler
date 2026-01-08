# -*- coding: utf-8 -*-
# =====================
# 
# 
# Author: liumin.423
# Date:   2025/7/8
# =====================
import json
import os
from typing import List, Any, Optional

from litellm import acompletion

from tools.util.log_util import timer, AsyncTimer
from tools.util.sensitive_detection import SensitiveWordsReplace
from tools.util.llm_config import get_llm_config


@timer(key="enter")
async def ask_llm(
        messages: str | List[Any],
        model: str = None,
        temperature: float = None,
        top_p: float = None,
        stream: bool = False,

        # 自定义字段
        only_content: bool = False,     # 只返回内容

        extra_headers: Optional[dict] = None,
        **kwargs,
):
    if isinstance(messages, str):
        messages = [{"role": "user", "content": messages}]
    if os.getenv("SENSITIVE_WORD_REPLACE", "false") == "true":
        for message in messages:
            if isinstance(message.get("content"), str):
                message["content"] = SensitiveWordsReplace.replace(message["content"])
            else:
                message["content"] = json.loads(
                    SensitiveWordsReplace.replace(json.dumps(message["content"], ensure_ascii=False)))
    
    # 获取 LLM 配置
    llm_config = get_llm_config()
    
    # 使用配置中的模型名称（如果未指定）
    if model is None:
        model = llm_config.get_model()
    
    # 使用配置中的温度（如果未指定）
    if temperature is None:
        temperature = llm_config.temperature
    
    # 合并请求头
    headers = llm_config.get_headers()
    if extra_headers:
        headers.update(extra_headers)
    
    # 获取基础 URL（会自动处理完整 URL，提取基础部分）
    base_url = llm_config.get_base_url()
    
    # 构建 litellm 参数
    litellm_params = {
        "messages": messages,
        "model": model,
        "temperature": temperature,
        "top_p": top_p,
        "stream": stream,
        "api_base": base_url,
        "extra_headers": headers,
        **kwargs
    }
    
    # 如果有 API Key（生产环境），添加到参数中
    api_key = llm_config.get_api_key()
    if api_key:
        litellm_params["api_key"] = api_key
    
    response = await acompletion(**litellm_params)
    async with AsyncTimer(key=f"exec ask_llm"):
        if stream:
            async for chunk in response:
                if only_content:
                    if chunk.choices and chunk.choices[0] and chunk.choices[0].delta and chunk.choices[0].delta.content:
                        yield chunk.choices[0].delta.content
                else:
                    yield chunk
        else:
            yield response.choices[0].message.content if only_content else response


if __name__ == "__main__":
    pass
