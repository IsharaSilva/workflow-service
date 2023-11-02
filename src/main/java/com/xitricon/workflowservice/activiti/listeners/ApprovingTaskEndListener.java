package com.xitricon.workflowservice.activiti.listeners;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;

public class ApprovingTaskEndListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), "status", WorkFlowStatus.APPROVED.name());
	}

}
