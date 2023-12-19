package com.xitricon.workflowservice.activiti.listeners;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.task.Task;

import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierDetailsTaskEndListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		boolean resubmission = execution.getVariable("resubmission", Boolean.class);

		WorkFlowStatus status = resubmission ? WorkFlowStatus.CORRECTION_INPROGRESS
				: WorkFlowStatus.SUBMISSION_IN_PROGRESS;

		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.STATUS, status.name());
		processEngine.getRuntimeService().setVariable(execution.getId(), "resubmission", false);

		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.ACTIVITY_TYPE,
				ActivitiType.FORM_FILLING.name());

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}", execution.getProcessInstanceId(), currentTask.getName());
	}

}
