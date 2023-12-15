package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.task.Task;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApprovingTaskOneEndListener implements ExecutionListener {

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
				: WorkFlowStatus.PENDING_APPROVAL_STAGE2;

		processEngine.getRuntimeService().setVariable(execution.getId(), "status", status.name());

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}", execution.getProcessInstanceId(), currentTask.getName());
		workflowSubmissionUtil.setCompletedFalseWhenPartialSubmission(execution);

	}

}
