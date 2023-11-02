package com.xitricon.workflowservice.util;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.springframework.stereotype.Component;

@Component
public class WorkflowVariableUtil {
    public Object getRuntimeWorkflowVariable(RuntimeService runtimeService, String executionID, String variableName) {
        return runtimeService.getVariable(executionID, "status");
	}

    public Object getHistoricWorkflowVariable(HistoryService historyService, String historicProcessInstanceId, String variableName) {
		return historyService.createHistoricVariableInstanceQuery().processInstanceId(historicProcessInstanceId).variableName("status").singleResult().getValue();
	}
}
