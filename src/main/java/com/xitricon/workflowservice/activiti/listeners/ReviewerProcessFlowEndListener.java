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
		processEngine.getRuntimeService().setVariable(execution.getId(), "status",
				WorkFlowStatus.PENDING_APPROVAL_STAGE1.name());
		log.info("Process instance : {} Completed sub process : {}", execution.getProcessInstanceId(),
				execution.getCurrentFlowElement().getName());
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);

	}

}
