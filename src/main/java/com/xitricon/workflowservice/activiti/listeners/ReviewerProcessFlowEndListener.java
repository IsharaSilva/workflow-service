package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

@Slf4j
public class ReviewerProcessFlowEndListener implements ExecutionListener {
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		// TODO this needs to be updated to use object mapper form JsonConfig
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		WorkflowSubmissionUtil workflowSubmissionUtil = new WorkflowSubmissionUtil(objectMapper);
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		boolean resubmission = execution.getVariable("resubmission", Boolean.class);

		WorkFlowStatus status = resubmission ? WorkFlowStatus.PENDING_CORRECTION
				: WorkFlowStatus.PENDING_APPROVAL_STAGE1;

		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.STATUS, status.name());

		log.info("Process instance : {} Completed sub process : {}, resubmission = {}", execution.getProcessInstanceId(),
				execution.getCurrentFlowElement().getName(), execution.getVariable("resubmission"));
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);

	}

}
