package com.chinatelecom.scheduler.handler;

import com.chinatelecom.scheduler.model.multi.EventResult;
import com.chinatelecom.scheduler.model.req.AgentRequest;
import com.chinatelecom.scheduler.model.response.AgentResponse;
import com.chinatelecom.scheduler.model.response.GptProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
public class ReactAgentResponseHandler  extends BaseAgentResponseHandler implements AgentResponseHandler {

    @Override
    public GptProcessResult handle(AgentRequest request, AgentResponse response, List<AgentResponse> agentRespList, EventResult eventResult) {
        try {
            return buildIncrResult(request, eventResult, response);
        } catch (Exception e) {
            log.error("{} ReactAgentResponseHandler handle error", request.getRequestId(), e);
            return null;
        }
    }
}
