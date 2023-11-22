package com.xitricon.workflowservice.activiti.listeners;

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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowUtil;

import java.net.URI;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApprovingTaskEndListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	RestTemplate restTemplate = new RestTemplate();
	ObjectMapper objectMapper = new ObjectMapper();

	public ApprovingTaskEndListener() {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Override
	public void notify(DelegateExecution execution) {
		Object interimStateObj = execution.getVariable("interimState");

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(execution.getProcessInstanceId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid current task for process instance : " + execution.getProcessInstanceId()));
		log.info("Received request: {}", interimStateObj);

		String onboardingServiceUrl = WorkflowUtil.getRuntimeWorkflowStringVariable(processEngine.getRuntimeService(),
				currentTask.getExecutionId(), "onboardingServiceUrl", "");

		try {
			SupplierOnboardingRequestOutputDTO dto = objectMapper.readValue(interimStateObj.toString(),
					SupplierOnboardingRequestOutputDTO.class);
			String jsonRequest = objectMapper.writeValueAsString(dto);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

			submitToOnboardingService(requestEntity, onboardingServiceUrl);

			execution.setVariable("status", WorkFlowStatus.APPROVED.name());

		} catch (Exception e) {
			log.error("Error processing interimState data while connecting to onboarding service: {}", e.getMessage(),
					e);
		}
	}

	private void submitToOnboardingService(HttpEntity<String> requestEntity, String onboardingServiceUrl) {
		try {
			URI onboardingServiceUri = UriComponentsBuilder.fromUriString(onboardingServiceUrl).build().toUri();
			restTemplate.postForObject(onboardingServiceUri, requestEntity, SupplierOnboardingRequestOutputDTO.class);
		} catch (Exception e) {
			log.error("Error submitting to Onboarding Service: {}", e.getMessage(), e);
		}
	}
}
