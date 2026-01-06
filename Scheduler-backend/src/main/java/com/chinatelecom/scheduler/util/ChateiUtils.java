package com.chinatelecom.scheduler.util;

import com.chinatelecom.scheduler.agent.enums.AutoBotsResultStatus;
import com.chinatelecom.scheduler.model.dto.AutoBotsResult;
import com.chinatelecom.scheduler.model.req.AgentRequest;
import com.chinatelecom.scheduler.model.req.GptQueryReq;
import org.apache.commons.lang3.StringUtils;

public class ChateiUtils {
    public static final String SOURCE_MOBILE = "mobile";
    public static final String SOURCE_PC = "pc";
    private static final String NO_ANSWER = "哎呀，超出我的知识领域了，换个问题试试吧";

    public static String getRequestId(GptQueryReq request) {
        return getRequestId(request.getUser(), request.getSessionId(), request.getRequestId());
    }

    public static String getRequestId(String erp, String traceId, String reqId) {
        erp = StringUtils.isNotEmpty(erp) ? erp.toLowerCase() : erp;
        if (ChineseCharacterCounter.hasChineseCharacters(erp)) {
            return traceId + ":" + reqId;
        } else {
            return erp + traceId + ":" + reqId;
        }
    }
    public static AutoBotsResult toAutoBotsResult(AgentRequest request, String status) {
        AutoBotsResult result = new AutoBotsResult();
        result.setTraceId(request.getRequestId());
        result.setReqId(request.getRequestId());
        result.setStatus(status);
        if (AutoBotsResultStatus.no.name().equals(status)) {
            result.setFinished(true);
            result.setResponse(NO_ANSWER);
            result.setResponseAll(NO_ANSWER);
        }
        return result;
    }
}
