package com.xitricon.workflowservice.service.impl;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.activiti.BPMDeployer;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessBuilder;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.ActivitiTypes;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

	private final BPMDeployer bpmDeployer;
	private final String questionnaireServiceUrl;
	private final RestTemplate restTemplate;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			@Value("${external-api.questionnaire-service.find-by-id}") final String questionnaireServiceUrl) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceUrl = questionnaireServiceUrl;
		this.restTemplate = restTemplateBuilder.build();
	}

	@Override
	public UserFormResponseOutputDTO getRequestQuestionnaire() {
		QuestionnaireOutputDTO emptyRequest = createEmptyOnboardingRequestDTO();

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		bpmDeployer.deploy(processEngine, SupplierOnboardingProcessBuilder.build(), CommonConstant.PROCESS_ENGINE_NAME);

		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		log.info("Started process ID : " + processInstance.getId());
		log.info("Number of currently running process instances = "
				+ processEngine.getRuntimeService().createProcessInstanceQuery().count());

		return new UserFormResponseOutputDTO(processInstance.getId(), ActivitiTypes.FORMFILLING, emptyRequest);
	}

	@Override
	public UserFormResponseOutputDTO updateRequestQuestionnaire(UserFormRequestInputDTO inputDto) {
		return null;
	}

	private QuestionnaireOutputDTO createEmptyOnboardingRequestDTO() {
		return restTemplate.getForObject(questionnaireServiceUrl, QuestionnaireOutputDTO.class);
	}
}
