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
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import com.xitricon.workflowservice.util.WorkflowUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	private String processDefinitionKey = CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID;
	private final BPMDeployer bpmDeployer;
	private final String questionnaireServiceUrlTenantOne;
	private final String questionnaireServiceUrlTenantTwo;
	private final RestTemplate restTemplate;
	private final WorkflowSubmissionUtil workflowSubmissionUtil;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			@Value("${external-api.questionnaire-service.find-by-id-t1}") final String questionnaireServiceUrlTenantOne,
			@Value("${external-api.questionnaire-service.find-by-id-t2}") final String questionnaireServiceUrlTenantTwo,
			final WorkflowSubmissionUtil workflowSubmissionUtil) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceUrlTenantOne = questionnaireServiceUrlTenantOne;
		this.questionnaireServiceUrlTenantTwo = questionnaireServiceUrlTenantTwo;

		this.restTemplate = restTemplateBuilder.build();
		this.workflowSubmissionUtil = workflowSubmissionUtil;
	}

	@Override
	public WorkflowOutputDTO initiateWorkflow(String tenantId) {

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
		processEngine.getRuntimeService().setVariable(executionId, "tenantId", tenantId);

		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire(tenantId);

		processEngine.getRuntimeService().setVariable(executionId, "questionnaireId", questionnaire.getId());

		return new WorkflowOutputDTO(processId, ActivitiType.FORM_FILLING, "Supplier Onboarding", questionnaire,
				processInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
				LocalDateTime.now(), "", tenantId);
	}

	// TODO return actual object rather returning null
	@Override
	public WorkflowOutputDTO handleWorkflowSubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput, String tenantId) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(workflowSubmissionInput.getWorkflowId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid workflow ID. Workflow instance has already been completed."));

		RuntimeService runtimeService = processEngine.getRuntimeService();
		String executionId = currentTask.getExecutionId();

		if (!WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId, "tenantId", "")
				.equals(tenantId)) {
			log.error(CommonConstant.INVALID_TENANT_MSG + tenantId);
			throw new IllegalArgumentException(CommonConstant.INVALID_TENANT_MSG + tenantId);
		}

		String workflowSubmissionInoutAsString = Optional
				.ofNullable(workflowSubmissionUtil.convertToString(workflowSubmissionInput))
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		runtimeService.setVariable(executionId, "interimState", workflowSubmissionInoutAsString);

		if (completed) {
			TaskService taskService = processEngine.getTaskService();
			taskService.complete(currentTask.getId());

			log.info("Completed task : " + currentTask.getName());
			return null;
		}

		// TODO find proper way to handle initiated to submission in progress
		WorkFlowStatus status = WorkFlowStatus.valueOf(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService,
				executionId, "status", "SUBMISSION_IN_PROGRESS"));

		if (status.equals(WorkFlowStatus.INITIATED)) {
			runtimeService.setVariable(executionId, "status", "SUBMISSION_IN_PROGRESS");
		}

		return null;

	}

	private QuestionnaireOutputDTO retriveQuestionnaire(String tenantId) {

		return (tenantId.equals(CommonConstant.TENANT_ONE_KEY)
				? restTemplate.getForObject(questionnaireServiceUrlTenantOne, QuestionnaireOutputDTO.class)
				: restTemplate.getForObject(questionnaireServiceUrlTenantTwo, QuestionnaireOutputDTO.class));
	}

	private BasicWorkflowOutputDTO createBasicWorkflowOutputDTO(String id, String title, String workflowType,
			String status, Date startedTime, String tenantId) {
		return new BasicWorkflowOutputDTO(id, title, workflowType, WorkFlowStatus.valueOf(status),
				startedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "", LocalDateTime.now(), "",
				tenantId);
	}

	@Override
	public List<BasicWorkflowOutputDTO> getWorkflows(String tenantId) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		RuntimeService runtimeService = processEngine.getRuntimeService();
		List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();

		ArrayList<BasicWorkflowOutputDTO> workflowOutputs = new ArrayList<>();

		workflowOutputs.addAll(processInstances.stream().map(pi -> {
			String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).list()
					.stream().findAny().map(Task::getExecutionId).orElse(null);

			return Optional.ofNullable(executionId)
					.filter(ei -> WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "tenantId", "")
							.equals(tenantId))
					.map(ei -> createBasicWorkflowOutputDTO(pi.getId(),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "title", ""),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "workflowType", ""),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "status",
									"SUBMISSION_IN_PROGRESS"),
							pi.getStartTime(),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, "tenantId", "")))
					.orElse(null);
		}).filter(Objects::nonNull).toList());

		HistoryService historyService = processEngine.getHistoryService();
		List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
				.finished().list();

		workflowOutputs.addAll(historicProcessInstances.stream()
				.filter(pi -> WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "tenantId", "")
						.equals(tenantId))
				.map(pi -> createBasicWorkflowOutputDTO(pi.getId(),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "title", ""),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "workflowType", ""),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "status",
								"SUBMISSION_IN_PROGRESS"),
						pi.getStartTime(),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, pi.getId(), "tenantId", "")))
				.toList());

		return workflowOutputs;
	}

	@Override
	public WorkflowOutputDTO getWorkflowById(String id, String tenantId) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(id).list().stream()
				.findAny().map(Task::getExecutionId).orElse(null);

		RuntimeService runtimeService = processEngine.getRuntimeService();
		HistoryService historyService = processEngine.getHistoryService();

		if (Objects.nonNull(executionId) && WorkflowUtil
				.getRuntimeWorkflowStringVariable(runtimeService, executionId, "tenantId", "").equals(tenantId)) {
			return new WorkflowOutputDTO(id,
					ActivitiType.valueOf(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
							"activityType", "FORM_FILLING")),
					WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId, "title", ""),
					mapWorkflowSubmissionInputToQuestionnaire(WorkflowUtil.getRuntimeWorkflowStringVariable(
							runtimeService, executionId, "interimState", "{}"), tenantId),
					LocalDateTime.now(), "", LocalDateTime.now(), "",
					WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId, "tenantId", ""));

		}

		return WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "tenantId", "").equals(tenantId)
				? new WorkflowOutputDTO(id,
						ActivitiType.valueOf(WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id,
								"activityType", "FORM_FILLING")),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "title", ""),
						mapWorkflowSubmissionInputToQuestionnaire(WorkflowUtil
								.getHistoricWorkflowStringVariable(historyService, id, "interimState", "{}"), tenantId),
						LocalDateTime.now(), "", LocalDateTime.now(), "",
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, "tenantId", ""))
				: null;
	}

	private QuestionnaireOutputDTO mapWorkflowSubmissionInputToQuestionnaire(String workflowSubmissionInputJson,
			String tenantId) {

		WorkflowSubmissionInputDTO workflowSubmissionInput = Optional
				.ofNullable(workflowSubmissionUtil.convertToWorkflowSubmissionInputDTO(workflowSubmissionInputJson))
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		List<WorkflowSubmissionQuestionInputDTO> questions = workflowSubmissionInput.getPages().stream()
				.map(WorkflowSubmissionPageInputDTO::getQuestions).flatMap(Collection::stream).toList();

		Map<String, List<String>> questionIdToResponseMap = questions.stream()
				.collect(Collectors.toMap(WorkflowSubmissionQuestionInputDTO::getId,
						WorkflowSubmissionQuestionInputDTO::getResponse, (r1, r2) -> r1));

		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire(tenantId);

		List<CommentOutputDTO> comments = workflowSubmissionInput.getComments().stream()
				.map(c -> new CommentOutputDTO(c.getRefId(), c.getCommentedBy(), c.getCommentedAt(), c.getCommentText(),
						c.getRefId()))
				.toList();

		if (Objects.nonNull(questionnaire)) {
			List<Page> pages = questionnaire.getPages().stream().map(p -> {
				List<Question> qs = p.getQuestions().stream()
						.map(q -> new Question(q.getId(), q.getIndex(), q.getLabel(), q.getType(), q.getGroup(),
								q.getValidations(), q.isEditable(), questionIdToResponseMap.get(q.getId()),
								q.getOptionsSource(), q.getSubQuestions(), q.getTenantId()))
						.toList();
				return new Page(p.getIndex(), p.getId(), p.getTitle(), qs);
			}).toList();

			questionnaire = new QuestionnaireOutputDTO(questionnaire.getId(), questionnaire.getTitle(),
					questionnaire.getCreatedBy(), questionnaire.getCreatedAt(), questionnaire.getModifiedBy(),
					questionnaire.getModifiedAt(), pages, comments, questionnaire.getTenantId());
		}

		return questionnaire;
	}

	@Override
	public void changeActiveWorkflow(String workfowId, String tenantId) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery().processInstanceId(workfowId).active()
						.singleResult())
				.orElseThrow(() -> new IllegalArgumentException(String
						.format("Invalid workflow ID. Workflow instance %s has already been completed.", workfowId)));

		RuntimeService runtimeService = processEngine.getRuntimeService();

		if (!WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, currentTask.getExecutionId(), "tenantId", "")
				.equals(tenantId)) {
			throw new IllegalArgumentException(CommonConstant.INVALID_TENANT_MSG + tenantId);
		}
		processDefinitionKey = workfowId;
	}
}
