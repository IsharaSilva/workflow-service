package com.xitricon.workflowservice.util;

import java.util.Optional;

import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;

public class WorkflowUtil {

	private WorkflowUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String getRuntimeWorkflowStringVariable(RuntimeService runtimeService, String executionID, String key,
			String defaultValue) {
		return Optional.ofNullable(runtimeService.getVariable(executionID, key)).map(Object::toString)
				.orElse(defaultValue);
	}

	public static Optional<String> getRuntimeWorkflowStringVariable(RuntimeService runtimeService, String executionID,
			String key) {
		return Optional.ofNullable(runtimeService.getVariable(executionID, key)).map(Object::toString);
	}

	public static String getHistoricWorkflowStringVariable(HistoryService historyService, String processId, String key,
			String defaultValue) {
		return Optional.ofNullable(historyService.createHistoricVariableInstanceQuery().processInstanceId(processId)
				.variableName(key).singleResult().getValue()).map(Object::toString).orElse(defaultValue);
	}

	public static void setWorkflowInstanceDisplayTitle(WorkflowSubmissionInputDTO workflowSubmissionInput,
			ProcessEngine processEngine, String executionId) {
		if (!workflowSubmissionInput.getPages().isEmpty()) {
			WorkflowSubmissionPageInputDTO inputPage = workflowSubmissionInput.getPages().get(0);
			if (inputPage.getTitle().equals("Supplier Details")) {
				String displayTitle = inputPage.getQuestions().get(0).getResponse().get(0) + " - "
						+ workflowSubmissionInput.getWorkflowId();
				processEngine.getRuntimeService().setVariable(executionId, CommonConstant.TITLE, displayTitle);
			}
		}
	}
}
