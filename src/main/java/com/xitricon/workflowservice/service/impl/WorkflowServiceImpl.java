package com.xitricon.workflowservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.activiti.BPMDeployer;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessWorkflow1Builder;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessWorkflow2Builder;
import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.CommentOutputDTO;
import com.xitricon.workflowservice.dto.Page;
import com.xitricon.workflowservice.dto.Question;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionQuestionInputDTO;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowSubmissionConverter;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import com.xitricon.workflowservice.util.WorkflowUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	private String processDefinitionKey = CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID;
	private final BPMDeployer bpmDeployer;
	private final String questionnaireServiceUrl;
	private final String onboardingServiceUrl;
	private final RestTemplate restTemplate;
	private final WorkflowSubmissionUtil workflowSubmissionUtil;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			@Value("${external-api.questionnaire-service.find-by-id}") final String questionnaireServiceUrl,
			final WorkflowSubmissionUtil workflowSubmissionUtil,
			@Value("${external-api.onboarding-service.find-by-id}") final String onboardingServiceUrl) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceUrl = questionnaireServiceUrl;
		this.restTemplate = restTemplateBuilder.build();
		this.workflowSubmissionUtil = workflowSubmissionUtil;
		this.onboardingServiceUrl=onboardingServiceUrl;
	}

	@Override
	public WorkflowOutputDTO initiateWorkflow() {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		bpmDeployer.deploy(processEngine,
				processDefinitionKey.equals(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
						? SupplierOnboardingProcessWorkflow1Builder.build()
						: SupplierOnboardingProcessWorkflow2Builder.build(),
				CommonConstant.PROCESS_ENGINE_NAME);

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
		processEngine.getRuntimeService().setVariable(executionId, "workflowType", processDefinitionKey);
		processEngine.getRuntimeService().setVariable(executionId, "status", WorkFlowStatus.INITIATED.name());
		processEngine.getRuntimeService().setVariable(executionId, "activityType", ActivitiType.FORM_FILLING.name());
		processEngine.getRuntimeService().setVariable(executionId, "onboardingServiceUrl", onboardingServiceUrl);
		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire();

		processEngine.getRuntimeService().setVariable(executionId, "questionnaireId", questionnaire.getId());

		return new WorkflowOutputDTO(processId, ActivitiType.FORM_FILLING, "Supplier Onboarding", questionnaire,
				processInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
				LocalDateTime.now(), "");
	}

	// TODO return actual object rather returning null
	@Override
	public WorkflowOutputDTO handleWorkflowSubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(workflowSubmissionInput.getWorkflowId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid workflow ID. Workflow instance has already been completed."));

		RuntimeService runtimeService = processEngine.getRuntimeService();
		String executionId = currentTask.getExecutionId();

		WorkflowSubmission interimState = WorkflowUtil
				.getRuntimeWorkflowStringVariable(runtimeService, executionId, "interimState").map(s -> {
					WorkflowSubmission is = workflowSubmissionUtil.convertToWorkflowSubmission(s);
					is.addPages(WorkflowSubmissionConverter.convertWorkflowSubmissionInputDTOtoPages(workflowSubmissionInput, true));
					is.addComments(WorkflowSubmissionConverter.convertWorkflowSubmissionInputDTOtoComments(workflowSubmissionInput));
					return is;
				}).orElse(new WorkflowSubmission(workflowSubmissionInput.getWorkflowId(), 
				WorkflowSubmissionConverter.convertWorkflowSubmissionInputDTOtoPages(workflowSubmissionInput, true),
				WorkflowSubmissionConverter.convertWorkflowSubmissionInputDTOtoComments(workflowSubmissionInput)));

		runtimeService.setVariable(executionId, "interimState", workflowSubmissionUtil.convertToString(interimState));

		TaskService taskService = processEngine.getTaskService();
		taskService.complete(currentTask.getId());

		return null;
	}

	private QuestionnaireOutputDTO retriveQuestionnaire() {
		return restTemplate.getForObject(questionnaireServiceUrl, QuestionnaireOutputDTO.class);
	}

	private BasicWorkflowOutputDTO createBasicWorkflowOutputDTO(String id, String title, String workflowType,
			String status, Date startedTime) {
		return new BasicWorkflowOutputDTO(id, title, workflowType, WorkFlowStatus.valueOf(status),
				startedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "", LocalDateTime.now(), "");
	}

	@Override
	public List<BasicWorkflowOutputDTO> getWorkflows() {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		RuntimeService runtimeService = processEngine.getRuntimeService();
		List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();

		ArrayList<BasicWorkflowOutputDTO> workflowOutputs = new ArrayList<>();

		workflowOutputs.addAll(processInstances.stream().map(pi -> {
			String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).list()
					.stream().findAny().map(Task::getExecutionId).orElse(null);

			return Optional.ofNullable(executionId).map(ei -> createBasicWorkflowOutputDTO(pi.getId(),
					WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "title", ""),
					WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "workflowType", ""), WorkflowUtil
							.getRuntimeWorkflowStringVariable(runtimeService, ei, "status", "SUBMISSION_IN_PROGRESS"),
					pi.getStartTime())).orElse(null);
		}).filter(wf -> Objects.nonNull(wf) && !wf.getStatus().equals(WorkFlowStatus.INITIATED)).toList());

		HistoryService historyService = processEngine.getHistoryService();
		List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
				.finished().list();

		workflowOutputs.addAll(historicProcessInstances.stream()
				.map(pi -> createBasicWorkflowOutputDTO(pi.getId(),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "title", ""),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "workflowType", ""),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "status",
								"SUBMISSION_IN_PROGRESS"),
						pi.getStartTime()))
				.toList());

		return workflowOutputs;
	}

	@Override
	public WorkflowOutputDTO getWorkflowById(String id) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(id).list().stream()
				.findAny().map(Task::getExecutionId).orElse(null);

		RuntimeService runtimeService = processEngine.getRuntimeService();
		HistoryService historyService = processEngine.getHistoryService();

		if (Objects.nonNull(executionId)) {
			return new WorkflowOutputDTO(id,
					ActivitiType.valueOf(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
							"activityType", "FORM_FILLING")),
					WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId, "title", ""),
					mapWorkflowSubmissionInputToQuestionnaire(WorkflowUtil
							.getRuntimeWorkflowStringVariable(runtimeService, executionId, "interimState", "{}")),
					LocalDateTime.now(), "", LocalDateTime.now(), "");

		}

		return new WorkflowOutputDTO(id,
				ActivitiType.valueOf(WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "activityType",
						"FORM_FILLING")),
				WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "title", ""),
				mapWorkflowSubmissionInputToQuestionnaire(
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "interimState", "{}")),
				LocalDateTime.now(), "", LocalDateTime.now(), "");
	}

	private QuestionnaireOutputDTO mapWorkflowSubmissionInputToQuestionnaire(String workflowSubmissionInputJson) {
		WorkflowSubmissionInputDTO workflowSubmissionInput = Optional
				.ofNullable(workflowSubmissionUtil.convertToWorkflowSubmissionInputDTO(workflowSubmissionInputJson))
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		List<WorkflowSubmissionQuestionInputDTO> questions = workflowSubmissionInput.getPages().stream()
				.map(WorkflowSubmissionPageInputDTO::getQuestions).flatMap(Collection::stream).toList();

		Map<String, List<String>> questionIdToResponseMap = questions.stream()
				.collect(Collectors.toMap(WorkflowSubmissionQuestionInputDTO::getId,
						WorkflowSubmissionQuestionInputDTO::getResponse, (r1, r2) -> r1));

		Map<String, Boolean> pageIdToCompletedMap = workflowSubmissionInput.getPages().stream()
						.collect(Collectors.toMap(WorkflowSubmissionPageInputDTO::getId,
						WorkflowSubmissionPageInputDTO::isCompleted, (r1, r2) -> r1));

		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire();

		List<CommentOutputDTO> comments = workflowSubmissionInput.getComments().stream()
				.map(c -> new CommentOutputDTO(c.getRefId(), c.getCommentedBy(), c.getCommentedAt(), c.getCommentText(),
						c.getRefId()))
				.toList();

		if (Objects.nonNull(questionnaire)) {
			List<Page> pages = questionnaire.getPages().stream().map(p -> {
				List<Question> qs = p.getQuestions().stream()
						.map(q -> new Question(q.getId(), q.getIndex(), q.getLabel(), q.getType(), q.getGroup(),
								q.getValidations(), q.isEditable(), questionIdToResponseMap.get(q.getId()),
								q.getOptionsSource(), q.getSubQuestions()))
						.toList();
				return new Page(p.getIndex(), p.getId(), p.getTitle(), qs, Optional
				.ofNullable(pageIdToCompletedMap.get(p.getId())).orElse(false));
			}).toList();

			questionnaire = new QuestionnaireOutputDTO(questionnaire.getId(), questionnaire.getTitle(),
					questionnaire.getCreatedBy(), questionnaire.getCreatedAt(), questionnaire.getModifiedBy(),
					questionnaire.getModifiedAt(), pages, comments);
		}

		return questionnaire;
	}

	@Override
	public void changeActiveWorkflow(String workfowId) {
		processDefinitionKey = workfowId;
	}
}
