package com.chinatelecom.scheduler.agent.llm;

import com.chinatelecom.scheduler.agent.util.SpringContextHolder;
import com.chinatelecom.scheduler.config.SchedulerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.Objects;


/**
 * 配置工具类
 */
@Slf4j
public class Config {
    /**
     * 获取 LLM 配置
     * 根据环境变量 APP_ENV 自动选择开发环境或生产环境配置
     */
    public static LLMSettings getLLMConfig(String modelName) {
        ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
        SchedulerConfig schedulerConfig = applicationContext.getBean(SchedulerConfig.class);
        
        // 先从环境变量读取环境类型
        String appEnv = System.getenv("APP_ENV");
        if (StringUtils.isEmpty(appEnv)) {
            appEnv = "dev"; // 默认开发环境
        }
        
        // 判断是否为生产环境
        boolean isProduction = "production".equalsIgnoreCase(appEnv) || "prod".equalsIgnoreCase(appEnv);
        
        // 如果 llm.settings 中有特定模型的配置，优先使用
        if (Objects.nonNull(schedulerConfig.getLlmSettingsMap()) && schedulerConfig.getLlmSettingsMap().containsKey(modelName)) {
            LLMSettings config = schedulerConfig.getLlmSettingsMap().get(modelName);
            // 确保从 JSON 配置读取的配置也有正确的环境配置
            return mergeWithDefaults(config, schedulerConfig, isProduction);
        }
        
        // 根据环境选择对应的默认配置
        return getDefaultConfig(schedulerConfig, isProduction);
    }

    /**
     * 合并配置与默认值，确保所有字段都有值
     */
    private static LLMSettings mergeWithDefaults(LLMSettings config, SchedulerConfig schedulerConfig, boolean isProduction) {
        LLMSettings defaultConfig = getDefaultConfig(schedulerConfig, isProduction);
        
        // 判断 baseUrl 是否已经是完整 URL
        String baseUrl = StringUtils.isNotEmpty(config.getBaseUrl()) ? config.getBaseUrl() : defaultConfig.getBaseUrl();
        String interfaceUrl = StringUtils.isNotEmpty(config.getInterfaceUrl()) ? config.getInterfaceUrl() : defaultConfig.getInterfaceUrl();
        
        boolean useFullUrl = config.getUseFullUrl() != null ? config.getUseFullUrl() : 
                           (baseUrl.contains("/v1/chat/completions") || 
                            baseUrl.contains("/chat/completions") ||
                            baseUrl.endsWith("/v1") ||
                            baseUrl.endsWith("/chat"));

        // 如果是完整 URL，则不需要 interfaceUrl
        if (useFullUrl && StringUtils.isNotEmpty(baseUrl)) {
            interfaceUrl = "";
        }

        return LLMSettings.builder()
                .model(StringUtils.isNotEmpty(config.getModel()) ? config.getModel() : defaultConfig.getModel())
                .maxTokens(config.getMaxTokens() > 0 ? config.getMaxTokens() : defaultConfig.getMaxTokens())
                .temperature(config.getTemperature() != 0.0 ? config.getTemperature() : defaultConfig.getTemperature())
                .baseUrl(baseUrl)
                .interfaceUrl(interfaceUrl)
                .functionCallType(StringUtils.isNotEmpty(config.getFunctionCallType()) ? config.getFunctionCallType() : defaultConfig.getFunctionCallType())
                .apiKey(StringUtils.isNotEmpty(config.getApiKey()) ? config.getApiKey() : defaultConfig.getApiKey())
                .maxInputTokens(config.getMaxInputTokens() > 0 ? config.getMaxInputTokens() : defaultConfig.getMaxInputTokens())
                .appEnv(isProduction ? "production" : "dev")
                .appId(StringUtils.isNotEmpty(config.getAppId()) ? config.getAppId() : defaultConfig.getAppId())
                .appKey(StringUtils.isNotEmpty(config.getAppKey()) ? config.getAppKey() : defaultConfig.getAppKey())
                .useFullUrl(useFullUrl)
                .extParams(config.getExtParams() != null ? config.getExtParams() : defaultConfig.getExtParams())
                .build();
    }

    /**
     * 根据环境加载 LLM 配置（从 GenieConfig 读取）
     * @param schedulerConfig GenieConfig 实例
     * @param isProduction 是否为生产环境
     */
    private static LLMSettings getDefaultConfig(SchedulerConfig schedulerConfig, boolean isProduction) {
        String baseUrl;
        String model;
        Integer maxTokens;
        Double temperature;
        Integer maxInputTokens;
        String apiKey = "";
        String appId = "";
        String appKey = "";

        if (isProduction) {
            // 生产环境配置
            baseUrl = schedulerConfig.getLlmProductionBaseUrl();
            model = schedulerConfig.getLlmProductionModel();
            maxTokens = schedulerConfig.getLlmProductionMaxTokens();
            temperature = schedulerConfig.getLlmProductionTemperature();
            maxInputTokens = schedulerConfig.getLlmProductionMaxInputTokens();
            apiKey = schedulerConfig.getLlmProductionApikey();
        } else {
            // 开发环境配置
            baseUrl = schedulerConfig.getLlmDevBaseUrl();
            model = schedulerConfig.getLlmDevModel();
            maxTokens = schedulerConfig.getLlmDevMaxTokens();
            temperature = schedulerConfig.getLlmDevTemperature();
            maxInputTokens = schedulerConfig.getLlmDevMaxInputTokens();
            appId = schedulerConfig.getLlmDevAppId();
            appKey = schedulerConfig.getLlmDevAppKey();
        }

        // 判断 baseUrl 是否已经是完整 URL（包含 /v1/chat/completions 或类似路径）
        boolean useFullUrl = StringUtils.isNotEmpty(baseUrl) && (
                            baseUrl.contains("/v1/chat/completions") || 
                            baseUrl.contains("/chat/completions") ||
                            baseUrl.endsWith("/v1") ||
                            baseUrl.endsWith("/chat"));

        // 创建配置
        return LLMSettings.builder()
                .model(StringUtils.isNotEmpty(model) ? model : (isProduction ? "Qwen2.5-14B-Instruct" : "MindIE2_Qwen2.5-14B-Instruct"))
                .maxTokens(maxTokens != null && maxTokens > 0 ? maxTokens : 16384)
                .temperature(temperature != null ? temperature : 0.0)
                .baseUrl(StringUtils.isNotEmpty(baseUrl) ? baseUrl : "")
                .interfaceUrl("") // 使用完整 URL，不需要 interfaceUrl
                .functionCallType("function_call")
                .apiKey(StringUtils.isNotEmpty(apiKey) ? apiKey : "")
                .maxInputTokens(maxInputTokens != null && maxInputTokens > 0 ? maxInputTokens : 100000)
                .appEnv(isProduction ? "production" : "dev")
                .appId(StringUtils.isNotEmpty(appId) ? appId : "")
                .appKey(StringUtils.isNotEmpty(appKey) ? appKey : "")
                .useFullUrl(useFullUrl)
                .build();
    }
}