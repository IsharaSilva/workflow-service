package com.xitricon.workflowservice.activiti.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.task.Task;

import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApprovingTaskOneEndListener implements ExecutionListener {

	ObjectMapper objectMapper = new ObjectMapper();
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), "status",
				WorkFlowStatus.PENDING_APPROVAL_STAGE2.name());
		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}", execution.getProcessInstanceId(), currentTask.getName());

		Object interimStateObj = execution.getVariable("interimState");
		if (interimStateObj instanceof String) {
			try {
				WorkflowSubmission workflowSubmission = objectMapper.readValue((String) interimStateObj, WorkflowSubmission.class);
				List<Page> pages = workflowSubmission.getPages().stream()
						.map(page -> {
							return new Page(page.getIndex(), page.getId(), page.getQuestions(), false);
						}).collect(Collectors.toList());
				WorkflowSubmission updatedWorkflowSubmission = WorkflowSubmission.builder().workflowId(workflowSubmission.getWorkflowId()).pages(pages).comments(workflowSubmission.getComments()).build();

				String updatedInterimState = objectMapper.writeValueAsString(updatedWorkflowSubmission);
				execution.setVariable("interimState", updatedInterimState);
			} catch (Exception e) {
				log.error("Error processing interimState data: {}", e.getMessage(), e);
			}
		} else {
			log.error("interimState is not a representation of WorkflowSubmission");
		}

	}

}
