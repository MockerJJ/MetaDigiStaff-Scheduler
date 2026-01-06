package com.chinatelecom.scheduler.handler;

import com.chinatelecom.scheduler.model.multi.EventResult;
import com.chinatelecom.scheduler.model.req.AgentRequest;
import com.chinatelecom.scheduler.model.response.AgentResponse;
import com.chinatelecom.scheduler.model.response.GptProcessResult;

import java.util.List;

public interface AgentResponseHandler {
    GptProcessResult handle(AgentRequest request,
                AgentResponse response,
                List<AgentResponse> agentRespList,
                EventResult eventResult);
}
