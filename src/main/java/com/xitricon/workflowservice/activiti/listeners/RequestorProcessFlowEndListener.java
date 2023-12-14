package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
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
		WorkflowSubmissionUtil workflowSubmissionUtil = new WorkflowSubmissionUtil(new ObjectMapper());
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.STATUS,
				WorkFlowStatus.PENDING_REVIEW.name());
		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.ACTIVITY_TYPE, ActivitiType.REVIEWING.name());
		log.info("Process instance : {} Completed sub process : {}", execution.getProcessInstanceId(),
				execution.getCurrentFlowElement().getName());
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);
	}

}
