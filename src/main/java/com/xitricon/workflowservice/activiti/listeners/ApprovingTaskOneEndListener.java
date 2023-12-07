package com.xitricon.workflowservice.activiti.listeners;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		WorkflowSubmissionUtil wf=new WorkflowSubmissionUtil(new ObjectMapper());
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		processEngine.getRuntimeService().setVariable(execution.getId(), "status",
				WorkFlowStatus.PENDING_APPROVAL_STAGE2.name());
		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}", execution.getProcessInstanceId(), currentTask.getName());
		wf.setCompletedFalseWhenPartialSubmission(execution);

	}

}
