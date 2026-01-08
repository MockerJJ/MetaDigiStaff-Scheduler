# -*- coding: utf-8 -*-
# =====================
# 运行 LLM 配置测试的简单脚本
# =====================
import os
import sys

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# 运行测试
if __name__ == "__main__":
    import pytest
    
    # 运行测试
    pytest.main([
        os.path.join(os.path.dirname(__file__), "test_llm_config.py"),
        "-v",  # 详细输出
        "-s",  # 显示 print 输出
        "--tb=short"  # 简短的错误追踪
    ])

