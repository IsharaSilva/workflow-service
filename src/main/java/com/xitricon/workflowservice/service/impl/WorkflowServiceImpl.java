package com.xitricon.workflowservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.activiti.BPMDeployer;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessBuilder;
import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestInputDTO;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	private final BPMDeployer bpmDeployer;
	private final String questionnaireServiceUrl;
	private final String onboardingServiceUrl;
	private final RestTemplate restTemplate;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			@Value("${external-api.questionnaire-service.find-by-id}") final String questionnaireServiceUrl,
			@Value("${external-api.onboarding-service.find-by-id}") final String onboardingServiceUrl) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceUrl = questionnaireServiceUrl;
		this.onboardingServiceUrl = onboardingServiceUrl;
		this.restTemplate = restTemplateBuilder.build();
	}

	@Override
	public WorkflowOutputDTO initiateWorkflow() {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		bpmDeployer.deploy(processEngine, SupplierOnboardingProcessBuilder.build(), CommonConstant.PROCESS_ENGINE_NAME);

		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		log.info("Started process ID : " + processInstance.getId());
		log.info("Number of currently running process instances = "
				+ processEngine.getRuntimeService().createProcessInstanceQuery().count());

		processEngine.getRuntimeService().setVariable(processInstance.getId(), "title", "Supplier Onboarding");
		processEngine.getRuntimeService().setVariable(processInstance.getId(), "status",
				WorkFlowStatus.INITIATED.name());
		// Model bpmnModel =
		// processEngine.getRepositoryService().getModel(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire();

		processEngine.getRuntimeService().setVariable(processInstance.getId(), "questionnaireId",
				questionnaire.getId());

		return new WorkflowOutputDTO(processInstance.getId(), ActivitiType.FORM_FILLING, "Supplier Onboarding",
				questionnaire,
				processInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
				LocalDateTime.now(), "");
	}

	@Override
	public WorkflowOutputDTO handleQuestionnaireSubmission(UserFormRequestInputDTO inputDTO) {
		SupplierOnboardingRequestInputDTO onboardingRequestInputDTO = new SupplierOnboardingRequestInputDTO(
				"dummyTitle", inputDTO.getWorkflowId(), inputDTO.getComments(), inputDTO.getPages(), "initiator_name",
				"reviewer-Name", "approver-name");
		
		// input json string save process
		// if complete ---> next step --> move to next step else submission 

		return null;
	}

	private QuestionnaireOutputDTO retriveQuestionnaire() {
		return restTemplate.getForObject(questionnaireServiceUrl, QuestionnaireOutputDTO.class);
	}

	private SupplierOnboardingRequestOutputDTO createOnboardingRequestDTO(
			SupplierOnboardingRequestInputDTO onboardingRequestInputDTO) {
		return restTemplate.postForObject(onboardingServiceUrl, onboardingRequestInputDTO,
				SupplierOnboardingRequestOutputDTO.class);
	}

	@Override
	public List<BasicWorkflowOutputDTO> getWorkflows() {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		List<ProcessInstance> processInstances = processEngine.getRuntimeService().createProcessInstanceQuery().list();

		return processInstances.stream().map(pi -> {
			String status = Optional.ofNullable(processEngine.getRuntimeService().getVariable(pi.getId(), "status"))
					.map(Object::toString).orElse("SUBMISSION_IN_PROGRESS");
			String title = Optional.ofNullable(processEngine.getRuntimeService().getVariable(pi.getId(), "title"))
					.map(Object::toString).orElse("");
			return new BasicWorkflowOutputDTO(pi.getId(), title, WorkFlowStatus.valueOf(status),
					pi.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
					LocalDateTime.now(), "");
		}).toList();
	}
}
