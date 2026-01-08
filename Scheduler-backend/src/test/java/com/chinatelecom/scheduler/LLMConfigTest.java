package com.chinatelecom.scheduler;

import com.chinatelecom.scheduler.agent.agent.AgentContext;
import com.chinatelecom.scheduler.agent.dto.Message;
import com.chinatelecom.scheduler.agent.llm.Config;
import com.chinatelecom.scheduler.agent.llm.LLM;
import com.chinatelecom.scheduler.agent.llm.LLMSettings;
import com.chinatelecom.scheduler.agent.printer.LogPrinter;
import com.chinatelecom.scheduler.agent.tool.ToolCollection;
import com.chinatelecom.scheduler.model.req.AgentRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM 配置测试用例
 * 测试新配置后的 LLM 是否可以正常发送请求
 */
@Slf4j
@SpringBootTest
public class LLMConfigTest {

    /**
     * 测试开发环境配置
     */
    @Test
    public void testDevEnvironmentConfig() throws Exception {
        log.info("=== 测试开发环境配置 ===");
        
        // 设置环境变量为开发环境（如果未设置，默认就是dev）
        String originalEnv = System.getenv("APP_ENV");
        try {
            // 确保是开发环境
            if (originalEnv == null || !"dev".equals(originalEnv)) {
                log.info("当前环境变量 APP_ENV: {}", originalEnv);
            }
            
            // 获取配置
            LLMSettings config = Config.getLLMConfig("MindIE2_Qwen2.5-14B-Instruct");
            log.info("开发环境配置: baseUrl={}, model={}, appEnv={}, appId={}, appKey={}", 
                    config.getBaseUrl(), config.getModel(), config.getAppEnv(), 
                    config.getAppId(), config.getAppKey());
            
            // 验证配置
            assertNotNull(config.getBaseUrl(), "baseUrl 不应为空");
            assertNotNull(config.getModel(), "model 不应为空");
            assertEquals("dev", config.getAppEnv(), "appEnv 应该是 dev");
            assertNotNull(config.getAppId(), "开发环境 appId 不应为空");
            assertNotNull(config.getAppKey(), "开发环境 appKey 不应为空");
            
            // 测试发送请求
            testLLMRequest(config);
            
        } finally {
            // 恢复原始环境变量（如果需要）
            if (originalEnv != null) {
                // 注意：Java 中无法直接修改环境变量，这里只是记录
                log.info("原始环境变量 APP_ENV: {}", originalEnv);
            }
        }
    }

    /**
     * 测试生产环境配置
     */
    @Test
    public void testProductionEnvironmentConfig() throws Exception {
        log.info("=== 测试生产环境配置 ===");
        
        // 设置环境变量为生产环境
        String originalEnv = System.getenv("APP_ENV");
        try {
            // 注意：Java 中无法直接修改环境变量，这里通过注释说明
            // 实际测试时需要在运行前设置: export APP_ENV=production
            log.info("当前环境变量 APP_ENV: {}", originalEnv);
            log.info("提示：要测试生产环境，请在运行前设置环境变量: export APP_ENV=production");
            
            // 如果当前是生产环境，则测试
            if ("production".equalsIgnoreCase(originalEnv) || "prod".equalsIgnoreCase(originalEnv)) {
                // 获取配置
                LLMSettings config = Config.getLLMConfig("MindIE2_Qwen2.5-14B-Instruct");
                log.info("生产环境配置: baseUrl={}, model={}, appEnv={}", 
                        config.getBaseUrl(), config.getModel(), config.getAppEnv());
                
                // 验证配置
                assertNotNull(config.getBaseUrl(), "baseUrl 不应为空");
                assertNotNull(config.getModel(), "model 不应为空");
                assertEquals("production", config.getAppEnv(), "appEnv 应该是 production");
                
                // 测试发送请求
                testLLMRequest(config);
            } else {
                log.warn("当前不是生产环境，跳过生产环境测试");
            }
            
        } finally {
            log.info("原始环境变量 APP_ENV: {}", originalEnv);
        }
    }

    /**
     * 测试 LLM 请求
     */
    private void testLLMRequest(LLMSettings config) throws Exception {
        log.info("=== 开始测试 LLM 请求 ===");
        
        // 创建 LLM 实例
        LLM llm = new LLM("MindIE2_Qwen2.5-14B-Instruct", "");
        
        // 创建 AgentRequest（LogPrinter 需要）
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setRequestId("test-request-" + System.currentTimeMillis());
        
        // 创建 AgentContext
        AgentContext context = AgentContext.builder()
                .requestId("test-request-" + System.currentTimeMillis())
                .sessionId("test-session")
                .query("你好，请简单介绍一下你自己")
                .printer(new LogPrinter(agentRequest))
                .toolCollection(new ToolCollection())
                .dateInfo("2025-01-XX")
                .build();
        
        // 创建消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(Message.userMessage("你好，请简单介绍一下你自己", null));
        
        // 创建系统消息（可选）
        List<Message> systemMsgs = new ArrayList<>();
        systemMsgs.add(Message.systemMessage("你是一个友好的AI助手", null));
        
        // 发送请求（非流式）
        log.info("发送 LLM 请求...");
        CompletableFuture<String> future = llm.ask(context, messages, systemMsgs, false, null);
        
        // 等待响应（最多等待30秒）
        String response = future.get(30, TimeUnit.SECONDS);
        
        // 验证响应
        assertNotNull(response, "响应不应为空");
        assertFalse(response.trim().isEmpty(), "响应内容不应为空");
        
        log.info("LLM 响应成功: {}", response);
        log.info("响应长度: {} 字符", response.length());
    }

    /**
     * 测试配置读取
     */
    @Test
    public void testConfigReading() {
        log.info("=== 测试配置读取 ===");
        
        // 测试获取配置
        LLMSettings config = Config.getLLMConfig("MindIE2_Qwen2.5-14B-Instruct");
        
        // 验证配置不为空
        assertNotNull(config, "配置不应为空");
        
        // 打印配置信息
        log.info("配置详情:");
        log.info("  model: {}", config.getModel());
        log.info("  baseUrl: {}", config.getBaseUrl());
        log.info("  interfaceUrl: {}", config.getInterfaceUrl());
        log.info("  maxTokens: {}", config.getMaxTokens());
        log.info("  temperature: {}", config.getTemperature());
        log.info("  appEnv: {}", config.getAppEnv());
        log.info("  appId: {}", config.getAppId());
        log.info("  appKey: {}", config.getAppKey());
        log.info("  useFullUrl: {}", config.getUseFullUrl());
        
        // 验证基本字段
        assertNotNull(config.getModel(), "model 不应为空");
        assertNotNull(config.getBaseUrl(), "baseUrl 不应为空");
        assertTrue(config.getMaxTokens() > 0, "maxTokens 应该大于 0");
    }

    /**
     * 测试不同模型名称的配置
     */
    @Test
    public void testDifferentModelNames() {
        log.info("=== 测试不同模型名称的配置 ===");
        
        String[] modelNames = {"MindIE2_Qwen2.5-14B-Instruct", "gpt-4o-0806", "test-model"};
        
        for (String modelName : modelNames) {
            LLMSettings config = Config.getLLMConfig(modelName);
            log.info("模型 {} 的配置: baseUrl={}, model={}", 
                    modelName, config.getBaseUrl(), config.getModel());
            
            assertNotNull(config, "配置不应为空");
            assertNotNull(config.getBaseUrl(), "baseUrl 不应为空");
        }
    }
}

