package com.xitricon.workflowservice.activiti.listeners;

import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.task.Task;

import java.util.Optional;

@Slf4j
public class ApprovingProcessFlowOneStartListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.STATUS,
				WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.name());

		processEngine.getRuntimeService().setVariable(execution.getId(), CommonConstant.ACTIVITY_TYPE, ActivitiType.APPROVING.name());

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}, resubmission = {}", execution.getProcessInstanceId(), currentTask.getName(), execution.getVariable("resubmission"));
	}

}
