package com.xitricon.workflowservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestInputDTO;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.dto.TaskOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowTaskDTO;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.ActivitiTypes;
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
	public UserFormResponseOutputDTO getRequestQuestionnaire() {
		QuestionnaireOutputDTO emptyRequest = createEmptyOnboardingRequestDTO();

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		bpmDeployer.deploy(processEngine, SupplierOnboardingProcessBuilder.build(), CommonConstant.PROCESS_ENGINE_NAME);

		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		log.info("Started process ID : " + processInstance.getId());
		log.info("Number of currently running process instances = "
				+ processEngine.getRuntimeService().createProcessInstanceQuery().count());

        Map<String, Object> processVariables = processInstance.getProcessVariables();
        processVariables.put("status", "Submission In-progress");

		return new UserFormResponseOutputDTO(processInstance.getId(), ActivitiTypes.FORMFILLING, emptyRequest);
	}

	@Override
	public UserFormResponseOutputDTO handleQuestionnaireSubmission(UserFormRequestInputDTO inputDTO) {
        SupplierOnboardingRequestInputDTO onboardingRequestInputDTO = new SupplierOnboardingRequestInputDTO("dummyTitle", inputDTO.getWorkflowId(), inputDTO.getComments(), inputDTO.getPages(), "initiator_name", "reviewer-Name", "approver-name");

		return null;
	}

	private QuestionnaireOutputDTO createEmptyOnboardingRequestDTO() {
		return restTemplate.getForObject(questionnaireServiceUrl, QuestionnaireOutputDTO.class);
	}

    private SupplierOnboardingRequestOutputDTO createOnboardingRequestDTO(SupplierOnboardingRequestInputDTO onboardingRequestInputDTO) {
		return restTemplate.postForObject(onboardingServiceUrl, onboardingRequestInputDTO, SupplierOnboardingRequestOutputDTO.class);
	}

    @Override
    public TaskOutputDTO getListOfWorkflows() {
        ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
        List<ProcessInstance> listOfProcessInstances = processEngine.getRuntimeService().createProcessInstanceQuery().list();
        ArrayList<WorkflowTaskDTO> tasks = new ArrayList<WorkflowTaskDTO>();

        for (ProcessInstance instance : listOfProcessInstances) {
            Map<String, Object> processVariables = instance.getProcessVariables();
            tasks.add(new WorkflowTaskDTO(instance.getId(), "TODO: Supplier Name", (String) processVariables.get("status"), instance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "TODO: start user", (LocalDateTime) processVariables.get("modifiedAt"), (String) processVariables.get("modifiedBy")));
        }

        return new TaskOutputDTO(tasks);
    }
}
