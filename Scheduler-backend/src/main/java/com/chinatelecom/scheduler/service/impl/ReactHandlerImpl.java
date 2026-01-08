package com.chinatelecom.scheduler.service.impl;

import com.chinatelecom.scheduler.agent.agent.AgentContext;
import com.chinatelecom.scheduler.agent.agent.ReActAgent;
import com.chinatelecom.scheduler.agent.agent.ReactImplAgent;
import com.chinatelecom.scheduler.agent.agent.SummaryAgent;
import com.chinatelecom.scheduler.agent.dto.File;
import com.chinatelecom.scheduler.agent.dto.TaskSummaryResult;
import com.chinatelecom.scheduler.agent.enums.AgentType;
import com.chinatelecom.scheduler.config.SchedulerConfig;
import com.chinatelecom.scheduler.model.req.AgentRequest;
import com.chinatelecom.scheduler.service.AgentHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class ReactHandlerImpl implements AgentHandlerService {

    @Autowired
    private SchedulerConfig schedulerConfig;


    @Override
    public String handle(AgentContext agentContext, AgentRequest request) {

        ReActAgent executor = new ReactImplAgent(agentContext);
        SummaryAgent summary = new SummaryAgent(agentContext);
        summary.setSystemPrompt(summary.getSystemPrompt().replace("{{query}}", request.getQuery()));

        executor.run(request.getQuery());
        TaskSummaryResult result = summary.summaryTaskResult(executor.getMemory().getMessages(), request.getQuery());

        Map<String, Object> taskResult = new HashMap<>();
        taskResult.put("taskSummary", result.getTaskSummary());

        if (CollectionUtils.isEmpty(result.getFiles())) {
            if (!CollectionUtils.isEmpty(agentContext.getProductFiles())) {
                List<File> fileResponses = agentContext.getProductFiles();
                // 过滤中间搜索结果文件
                fileResponses.removeIf(file -> Objects.nonNull(file) && file.getIsInternalFile());
                Collections.reverse(fileResponses);
                taskResult.put("fileList", fileResponses);
            }
        } else {
            taskResult.put("fileList", result.getFiles());
        }

        agentContext.getPrinter().send("result", taskResult);

        return "";
    }

    @Override
    public Boolean support(AgentContext agentContext, AgentRequest request) {
        return AgentType.REACT.getValue().equals(request.getAgentType());
    }
}
