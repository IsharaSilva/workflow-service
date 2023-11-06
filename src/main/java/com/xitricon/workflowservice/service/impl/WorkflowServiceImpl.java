package com.xitricon.workflowservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.activiti.BPMDeployer;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessBuilder;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessBuilderWorkflow1;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessBuilderWorkflow2;
import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.Page;
import com.xitricon.workflowservice.dto.Question;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestInputDTO;
import com.xitricon.workflowservice.dto.SupplierOnboardingRequestOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionQuestionInputDTO;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	private static String 	processDefinitionKey = CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID;
	private final BPMDeployer bpmDeployer;
	private final String questionnaireServiceUrl;
	private final String onboardingServiceUrl;
	private final RestTemplate restTemplate;
	private final WorkflowSubmissionUtil workflowSubmissionUtil;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			@Value("${external-api.questionnaire-service.find-by-id}") final String questionnaireServiceUrl,
			@Value("${external-api.onboarding-service.find-by-id}") final String onboardingServiceUrl,
			final WorkflowSubmissionUtil workflowSubmissionUtil) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceUrl = questionnaireServiceUrl;
		this.onboardingServiceUrl = onboardingServiceUrl;
		this.restTemplate = restTemplateBuilder.build();
		this.workflowSubmissionUtil = workflowSubmissionUtil;
	}

	@Override
	public WorkflowOutputDTO initiateWorkflow() {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		bpmDeployer.deploy(processEngine, processDefinitionKey.equals(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID) ? SupplierOnboardingProcessBuilderWorkflow1.build() : SupplierOnboardingProcessBuilderWorkflow2.build(), CommonConstant.PROCESS_ENGINE_NAME);

		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		String processId = processInstance.getId();

		log.info("Started process ID : " + processId);
		log.info("Number of currently running process instances = "
				+ processEngine.getRuntimeService().createProcessInstanceQuery().count());

		Task currentTask = processEngine.getTaskService().createTaskQuery().processInstanceId(processId).active()
				.singleResult();

		String executionId = currentTask.getExecutionId();

		processEngine.getRuntimeService().setVariable(executionId, "title", "Supplier Onboarding");
		processEngine.getRuntimeService().setVariable(executionId, "status", WorkFlowStatus.INITIATED.name());
		processEngine.getRuntimeService().setVariable(executionId, "activityType", ActivitiType.FORM_FILLING.name());
		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire();

		processEngine.getRuntimeService().setVariable(executionId, "questionnaireId", questionnaire.getId());

		return new WorkflowOutputDTO(processId, ActivitiType.FORM_FILLING, "Supplier Onboarding", questionnaire,
				processInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
				LocalDateTime.now(), "");
	}

	@Override
	public WorkflowOutputDTO handleWorkflowSubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		Task currentTask = processEngine.getTaskService().createTaskQuery()
				.processInstanceId(workflowSubmissionInput.getWorkflowId()).active().singleResult();

		String workflowSubmissionInoutAsString = Optional
				.ofNullable(workflowSubmissionUtil.convertToString(workflowSubmissionInput))
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		processEngine.getRuntimeService().setVariable(currentTask.getExecutionId(), "interimState",
				workflowSubmissionInoutAsString);

		if (completed) {
			TaskService taskService = processEngine.getTaskService();
			taskService.complete(currentTask.getId());

			log.info("Completed task : " + currentTask.getName());
		}

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
			String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).list()
					.stream().findAny().map(Task::getExecutionId).orElse(null);

			return Optional.ofNullable(executionId).map(ei -> {
				String status = Optional.ofNullable(processEngine.getRuntimeService().getVariable(ei, "status"))
						.map(Object::toString).orElse("SUBMISSION_IN_PROGRESS");
				String title = Optional.ofNullable(processEngine.getRuntimeService().getVariable(ei, "title"))
						.map(Object::toString).orElse("");
				return new BasicWorkflowOutputDTO(pi.getId(), title, WorkFlowStatus.valueOf(status),
						pi.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
						LocalDateTime.now(), "");
			}).orElse(null);
		}).filter(Objects::nonNull).toList();
	}

	@Override
	public WorkflowOutputDTO getWorkflowById(String id) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(id).list().stream()
				.findAny().map(Task::getExecutionId).orElse(null);

		String activityType = Optional
				.ofNullable(processEngine.getRuntimeService().getVariable(executionId, "activityType"))
				.map(Object::toString).orElse("FORM_FILLING");

		String title = Optional.ofNullable(processEngine.getRuntimeService().getVariable(executionId, "title"))
				.map(Object::toString).orElse("");

		String interimState = Optional
				.ofNullable(processEngine.getRuntimeService().getVariable(executionId, "interimState"))
				.map(Object::toString).orElse("{}");

		WorkflowSubmissionInputDTO workflowSubmissionInput = Optional
				.ofNullable(workflowSubmissionUtil.convertToWorkflowSubmissionInputDTO(interimState))
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		List<WorkflowSubmissionQuestionInputDTO> questions = workflowSubmissionInput.getPages().stream()
				.map(WorkflowSubmissionPageInputDTO::getQuestions).flatMap(Collection::stream).toList();

		Map<String, List<String>> questionIdToResponseMap = questions.stream().collect(Collectors
				.toMap(WorkflowSubmissionQuestionInputDTO::getId, WorkflowSubmissionQuestionInputDTO::getResponse));

		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire();

		if (Objects.nonNull(questionnaire)) {
			List<Page> pages = questionnaire.getPages().stream().map(p -> {
				List<Question> qs = p.getQuestions().stream()
						.map(q -> new Question(q.getId(), q.getIndex(), q.getLabel(), q.getType(), q.getGroup(),
								q.getValidations(), q.isEditable(), questionIdToResponseMap.get(q.getId()),
								q.getOptionsSource(), q.getSubQuestions()))
						.toList();
				return new Page(p.getIndex(), p.getId(), p.getTitle(), qs);
			}).toList();
			questionnaire = new QuestionnaireOutputDTO(questionnaire.getId(), questionnaire.getTitle(),
					questionnaire.getCreatedBy(), questionnaire.getCreatedAt(), questionnaire.getModifiedBy(),
					questionnaire.getModifiedAt(), pages);
		}

		return new WorkflowOutputDTO(id, ActivitiType.valueOf(activityType), title, questionnaire, LocalDateTime.now(),
				"", LocalDateTime.now(), "");
	}

	public void handleSetWorkflow(String workfowId) {
		processDefinitionKey = workfowId;
	}
}
