package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestorProcessFlowEndListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		WorkflowSubmissionUtil workflowSubmissionUtil = new WorkflowSubmissionUtil(objectMapper);
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), "status",
				WorkFlowStatus.PENDING_REVIEW.name());
		processEngine.getRuntimeService().setVariable(execution.getId(), "activityType", ActivitiType.REVIEWING.name());
		log.info("Process instance : {} Completed sub process : {}", execution.getProcessInstanceId(),
				execution.getCurrentFlowElement().getName());
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);
	}

}
