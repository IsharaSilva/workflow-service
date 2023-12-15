package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		WorkflowSubmissionUtil workflowSubmissionUtil = new WorkflowSubmissionUtil(new ObjectMapper());
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		
		boolean resubmission = execution.getVariable("resubmission", Boolean.class);

		WorkFlowStatus status = resubmission ? WorkFlowStatus.PENDING_CORRECTION
				: WorkFlowStatus.PENDING_APPROVAL_STAGE1;

		processEngine.getRuntimeService().setVariable(execution.getId(), "status", status.name());

		log.info("Process instance : {} Completed sub process : {}, resubmission = {}", execution.getProcessInstanceId(),
				execution.getCurrentFlowElement().getName(), execution.getVariable("resubmission"));
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);

	}

}
