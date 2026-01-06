package com.chinatelecom.scheduler.service;

import com.chinatelecom.scheduler.agent.agent.AgentContext;
import com.chinatelecom.scheduler.model.req.AgentRequest;

public interface AgentHandlerService {

    /**
     * 处理Agent请求
     */
    String handle(AgentContext context, AgentRequest request);

    /**
     * 进入handler条件
     */
    Boolean support(AgentContext context, AgentRequest request);

}