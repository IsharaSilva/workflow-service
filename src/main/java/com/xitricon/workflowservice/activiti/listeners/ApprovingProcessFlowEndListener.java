package com.xitricon.workflowservice.activiti.listeners;

import com.xitricon.workflowservice.util.SupplierOnboardingUtil;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.task.Task;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.dto.CommentOutputDTO;
import com.xitricon.workflowservice.dto.Page;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApprovingProcessFlowEndListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) {
		String interimStateObj = execution.getVariable("interimState").toString();

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		boolean resubmission = execution.getVariable("resubmission", Boolean.class);

		WorkFlowStatus status = resubmission ? WorkFlowStatus.PENDING_CORRECTION
				: WorkFlowStatus.APPROVED;

		processEngine.getRuntimeService().setVariable(execution.getId(), "status", status.name());

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Process instance : {} Completed task : {}, resubmission = {}", execution.getProcessInstanceId(), currentTask.getName(), execution.getVariable("resubmission"));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		WorkflowSubmissionUtil workFlowSubmission = new WorkflowSubmissionUtil(objectMapper);
		WorkflowSubmission workflowSubmission = workFlowSubmission.convertToWorkflowSubmission(interimStateObj);

		SupplierOnboardingRequestOutputDTO supplierOnboardingRequestOutputDTO = mapToSupplierOnboardingRequestOutputDTO(
				workflowSubmission, execution);
		SupplierOnboardingUtil supplierOnboarding = new SupplierOnboardingUtil(objectMapper);
		String jsonRequest = supplierOnboarding.convertToString(supplierOnboardingRequestOutputDTO);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

		submitToOnboardingService(requestEntity, execution);

	}

	private SupplierOnboardingRequestOutputDTO mapToSupplierOnboardingRequestOutputDTO(
			WorkflowSubmission workflowSubmission, DelegateExecution execution) {

		List<CommentOutputDTO> commentOutputDTOs = workflowSubmission.getComments().stream()
				.map(comment -> new CommentOutputDTO(null, comment.getCommentedBy(), comment.getCommentedAt(),
						comment.getCommentText(), comment.getRefId()))
				.toList();

		List<Page> pages = workflowSubmission.getPages().stream()
				.map(page -> new Page(page.getIndex(), page.getId(), null, null, page.isCompleted())).toList();

		String workflowId = Optional.ofNullable(execution.getVariable("workflowId")).map(Object::toString).orElse(null);
		String title = Optional.ofNullable(execution.getVariable("title")).map(Object::toString).orElse(null);
		String questionnaireId = Optional.ofNullable(execution.getVariable("questionnaireId")).map(Object::toString)
				.orElse(null);
		String initiator = Optional.ofNullable(execution.getVariable("initiator")).map(Object::toString).orElse(null);
		String reviewer = Optional.ofNullable(execution.getVariable("reviewer")).map(Object::toString).orElse(null);
		String approver = Optional.ofNullable(execution.getVariable("approver")).map(Object::toString).orElse(null);

		return new SupplierOnboardingRequestOutputDTO(workflowId, title, questionnaireId, commentOutputDTOs, pages,
				initiator, reviewer, approver, LocalDateTime.now(), LocalDateTime.now());
	}

	private void submitToOnboardingService(HttpEntity<String> requestEntity,
			DelegateExecution execution) {
		RestTemplate restTemplate = new RestTemplate();
		String onboardingServiceUrl = Optional.ofNullable(execution.getVariable("onboardingServiceUrl")).orElse("")
				.toString();
		try {
			URI onboardingServiceUri = UriComponentsBuilder.fromUriString(onboardingServiceUrl).build().toUri();
			restTemplate.postForEntity(onboardingServiceUri, requestEntity, String.class);

			execution.setVariable("status", WorkFlowStatus.APPROVED.name());
		} catch (Exception e) {
			log.error("Error submitting the request to Onboarding Service: {}", e.getMessage(), e);
		}
	}

}
