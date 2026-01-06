package com.chinatelecom.scheduler.agent.printer;

import com.chinatelecom.scheduler.agent.enums.AgentType;

public interface Printer {
    /**
     * 发送消息
     *
     * @param message 消息内容
     */

    void send(String messageId, String messageType, Object message, String digitalEmployee, Boolean isFinal);

    void send(String messageType, Object message);

    void send(String messageType, Object message, String digitalEmployee);

    void send(String messageId, String messageType, Object message, Boolean isFinal);

    void close();

    void updateAgentType(AgentType agentType);
}