import os

import dotenv
from openai import OpenAI
from tools.util.log_util import logger
from tools.util.llm_config import get_llm_config
dotenv.load_dotenv()


class LLMClient:
    """大模型客户端类
    
    使用统一的 LLM 配置，根据环境变量 APP_ENV 自动选择开发环境或生产环境配置
    """

    def __init__(self):
        # 获取 LLM 配置
        llm_config = get_llm_config()
        
        # 使用配置中的值
        self.api_key = llm_config.get_api_key()
        self.model_name = llm_config.get_model()
        self.model_base_url = llm_config.get_base_url()
        self.llm_config = llm_config
        
        # 获取 headers
        headers = llm_config.get_headers()
        
        # 创建 OpenAI 客户端，使用配置的 base_url 和 headers
        # OpenAI 客户端支持通过 default_headers 设置自定义 headers
        self.client = OpenAI(
            api_key=self.api_key or "dummy",  # OpenAI 客户端需要 api_key，即使不使用
            base_url=self.model_base_url,
            default_headers=headers
        )
        logger.info("init LLM client, base_url={}, model={}, env={}".format(
            self.model_base_url, self.model_name, llm_config.app_env))

    @staticmethod
    def convert_messages(prompt):
        return [{"role": "user", "content": prompt}]

    def completions(self, messages, max_tokens=8192, temperature=0, stream=False):
        logger.info(f"chat completion\n{self.model_name}, {messages}")
        
        # 使用默认的 max_tokens（如果未指定）
        if max_tokens == 8192:
            max_tokens = self.llm_config.max_tokens
        
        # 使用默认的 temperature（如果未指定）
        if temperature == 0:
            temperature = self.llm_config.temperature
        
        # 使用 OpenAI 客户端发送请求
        # default_headers 已经在初始化时设置
        completion = self.client.chat.completions.create(
            model=self.model_name,
            messages=messages,
            temperature=temperature,
            stream=stream,
            max_tokens=max_tokens,
            extra_body={
                "enable_thinking": False,
                "chat_template_kwargs": {
                    "enable_thinking": False
                }
            }
        )
        
        if stream:
            return completion
        return completion.choices[0].message.content

    def chat(self, prompt, image_url):
        messages = self.convert_messages(prompt)
        return self.completions(messages)


if __name__ == '__main__':
    os.environ.setdefault("API_KEY", "Bearer sk-8a885913ec6044d38e6f167e4b1f6380")
    os.environ.setdefault("LLM_MODEL_NAME", "qwen-plus")
    os.environ.setdefault("LLM_MODEL_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1")
    llm = LLMClient()
    print(llm.completions([{"role": "user", "content": "你好"}]))
