package com.xitricon.workflowservice.service.impl;

import java.util.ArrayList;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;
import com.xitricon.workflowservice.service.WorkflowService;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);
	private final String processId = "supplier-onboarding";
	private final String processEngineName = "supplierOnboarding";

	@Autowired
	private Environment env;

	@Override
	public UserFormResponseOutputDTO getRequestQuestionnaire() {
		UserFormResponseOutputDTO emptyRequest = createEmptyOnboardingRequestDTO();

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(processEngineName);
		DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment().name(processId);
		BpmnModel model = createSupplierOnboardingProcess();
		deploymentBuilder = deploymentBuilder.addBpmnModel(processId + ".bpmn", model).key(processId);
		deploymentBuilder.deploy();

		ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processId);
		logger.info("Started process ID : " + processInstance.getId());
		logger.info("Number of currently running process instances = "
				+ processEngine.getRuntimeService().createProcessInstanceQuery().count());
		// processInstance.
		UserFormResponseOutputDTO response = new UserFormResponseOutputDTO(processInstance.getId(),
				emptyRequest.getTitle(), emptyRequest.getCreatedBy(), emptyRequest.getCreatedAt(),
				emptyRequest.getModifiedBy(), emptyRequest.getModifiedAt(),
				/* emptyRequest.getActivitiType() */ emptyRequest.getPages());

		return response;
	}

	@Override
	public UserFormResponseOutputDTO updateRequestQuestionnaire(UserFormRequestInputDTO inputDto) {

		if (inputDto != null || inputDto.getQuestionnaireId() != null) {
			logger.info("Updating process ID : " + inputDto.getQuestionnaireId());
		}

		return null;
	}

	private UserFormResponseOutputDTO createEmptyOnboardingRequestDTO() {
		String url = env.getProperty("com.xitricon.questionnaireservice.url");
		RestTemplate restTemplate = new RestTemplate();
		/*
		 * List<HttpMessageConverter<?>> converters = new ArrayList<>();
		 * MappingJackson2HttpMessageConverter jsonConverter = new
		 * MappingJackson2HttpMessageConverter();
		 * jsonConverter.setObjectMapper(objectMapper); converters.add(jsonConverter);
		 * restTemplate.setMessageConverters(converters);
		 */
		// restTemplate.getMessageConverters().add(0,
		// mappingJacksonHttpMessageConverter);
		UserFormResponseOutputDTO result = restTemplate.getForObject(url, UserFormResponseOutputDTO.class);

		return result;
	}

	private BpmnModel createSupplierOnboardingProcess() {
		BpmnModel model = new BpmnModel();
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId(processId);
		process.setName("Supplier Onboarding");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		UserTask userTaskSupplierDetails = new UserTask();
		userTaskSupplierDetails.setName("Supplier Details");
		userTaskSupplierDetails.setId("sid-supplier-details");
		userTaskSupplierDetails.setAssignee("kermit");
		userTaskSupplierDetails.setFormKey("supplier-form");

		ExtensionElement ext = new ExtensionElement();
		FormProperty prop = new FormProperty();
		prop.setName("supplierName");
		prop.setType("string");
		FormValue formValue = new FormValue();
		ArrayList<FormValue> values = new ArrayList<FormValue>();
		values.add(formValue);
		prop.setFormValues(values);

		ext.addAttribute(new ExtensionAttribute("sample ABC"));
		userTaskSupplierDetails.addExtensionElement(ext);

		UserTask userTaskSupportingEvidence = new UserTask();
		userTaskSupportingEvidence.setName("Supporting Evidence");
		userTaskSupportingEvidence.setId("sid-supporting-evidence");
		userTaskSupportingEvidence.setAssignee("kermit");

		UserTask userTaskSupplierClassification = new UserTask();
		userTaskSupplierClassification.setName("Supplier Classification");
		userTaskSupplierClassification.setId("sid-supplier-classification");
		userTaskSupplierClassification.setAssignee("kermit");

		UserTask userTaskSupplierInvolvement = new UserTask();
		userTaskSupplierInvolvement.setName("Supplier Involvement");
		userTaskSupplierInvolvement.setId("sid-supplier-involvement");
		userTaskSupplierInvolvement.setAssignee("kermit");

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(userTaskSupplierDetails);
		process.addFlowElement(userTaskSupportingEvidence);
		process.addFlowElement(userTaskSupplierClassification);
		process.addFlowElement(userTaskSupplierInvolvement);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", "sid-supplier-details"));
		process.addFlowElement(new SequenceFlow("sid-supplier-details", "sid-supporting-evidence"));
		process.addFlowElement(new SequenceFlow("sid-supporting-evidence", "sid-supplier-classification"));
		process.addFlowElement(new SequenceFlow("sid-supplier-classification", "sid-supplier-involvement"));
		process.addFlowElement(new SequenceFlow("sid-supplier-involvement", "end"));

		model.addProcess(process);

		return model;
	}
}
