package com.xitricon.workflowservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.activiti.BPMDeployer;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessWorkflow1Builder;
import com.xitricon.workflowservice.activiti.SupplierOnboardingProcessWorkflow2Builder;
import com.xitricon.workflowservice.config.QuestionnaireServiceProperties;
import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.CommentOutputDTO;
import com.xitricon.workflowservice.dto.Page;
import com.xitricon.workflowservice.dto.Question;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionQuestionInputDTO;
import com.xitricon.workflowservice.model.WorkflowActiveStatus;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.service.WorkflowActiveStatusService;
import com.xitricon.workflowservice.service.WorkflowService;
import com.xitricon.workflowservice.util.CommonConstant;
import com.xitricon.workflowservice.util.WorkflowSubmissionConverter;
import com.xitricon.workflowservice.util.WorkflowSubmissionUtil;
import com.xitricon.workflowservice.util.WorkflowUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	private final BPMDeployer bpmDeployer;
	private final QuestionnaireServiceProperties questionnaireServiceProperties;
	private final String onboardingServiceUrl;
	private final RestTemplate restTemplate;
	private final WorkflowSubmissionUtil workflowSubmissionUtil;
	private final WorkflowActiveStatusService workflowActiveStatusService;

	public WorkflowServiceImpl(final RestTemplateBuilder restTemplateBuilder, final BPMDeployer bpmDeployer,
			final QuestionnaireServiceProperties questionnaireServiceProperties,
			final WorkflowSubmissionUtil workflowSubmissionUtil,
			@Value("${external-api.onboarding-service.find-by-id}") final String onboardingServiceUrl,
			final WorkflowActiveStatusService workflowActiveStatusService) {
		super();
		this.bpmDeployer = bpmDeployer;
		this.questionnaireServiceProperties = questionnaireServiceProperties;
		this.restTemplate = restTemplateBuilder.build();
		this.workflowSubmissionUtil = workflowSubmissionUtil;
		this.onboardingServiceUrl = onboardingServiceUrl;
		this.workflowActiveStatusService = workflowActiveStatusService;
	}

	@Override
	public WorkflowOutputDTO initiateWorkflow(String tenantId) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		String processDefinitionKey = this.workflowActiveStatusService.findByActiveTrueAndTenantId(tenantId)
				.getProcessDefinitionKey();

		bpmDeployer.deploy(processEngine,
				processDefinitionKey.equals(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
						? SupplierOnboardingProcessWorkflow1Builder.build()
						: SupplierOnboardingProcessWorkflow2Builder.build(),
				CommonConstant.PROCESS_ENGINE_NAME);

		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

		String processId = processInstance.getId();

		log.info("Started process ID : " + processId);
		log.info("Number of currently running process instances = "
				+ runtimeService.createProcessInstanceQuery().count());

		Task currentTask = processEngine.getTaskService().createTaskQuery().processInstanceId(processId).active()
				.singleResult();

		String executionId = currentTask.getExecutionId();

		runtimeService.setVariable(executionId, CommonConstant.TITLE, "Supplier Onboarding");
		runtimeService.setVariable(executionId, CommonConstant.WORKFLOW_TYPE, processDefinitionKey);
		runtimeService.setVariable(executionId, CommonConstant.STATUS, WorkFlowStatus.INITIATED.name());
		runtimeService.setVariable(executionId, CommonConstant.ACTIVITY_TYPE, ActivitiType.FORM_FILLING.name());
		runtimeService.setVariable(executionId, "onboardingServiceUrl", onboardingServiceUrl);
		runtimeService.setVariable(executionId, CommonConstant.TENANT_ID_KEY, tenantId);
		runtimeService.setVariable(executionId, CommonConstant.DELETED, false);
		runtimeService.setVariable(executionId, "resubmission", false);
		runtimeService.setVariable(executionId, CommonConstant.CREATED_AT, LocalDateTime.now());
		runtimeService.setVariable(executionId, CommonConstant.MODIFIED_AT, LocalDateTime.now());

		QuestionnaireOutputDTO questionnaire = retriveQuestionnaire(tenantId);

		runtimeService.setVariable(executionId, "questionnaireId", questionnaire.getId());

		return new WorkflowOutputDTO(processId, ActivitiType.FORM_FILLING, "Supplier Onboarding", questionnaire,
				processInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), "",
				LocalDateTime.now(), "", tenantId, WorkFlowStatus.INITIATED);
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

		if (!WorkflowUtil
				.getRuntimeWorkflowStringVariable(runtimeService, executionId, CommonConstant.TENANT_ID_KEY, "")
				.equals(tenantId)) {
			log.error(CommonConstant.INVALID_TENANT_MSG + tenantId);
			throw new IllegalArgumentException(CommonConstant.INVALID_TENANT_MSG + tenantId);
		}

		AtomicBoolean isUpdate = new AtomicBoolean(false);
		WorkflowSubmission interimState = WorkflowUtil
				.getRuntimeWorkflowStringVariable(runtimeService, executionId, CommonConstant.INTERIM_STATE).map(s -> {
					WorkflowSubmission is = workflowSubmissionUtil.convertToWorkflowSubmission(s);
					if (!workflowSubmissionInput.getPages().isEmpty()) {
						String pageId = workflowSubmissionInput.getPages().get(0).getId();
						boolean isCompleted = is.getPages().stream().filter(i -> i.getId().equals(pageId))
								.map(com.xitricon.workflowservice.model.Page::isCompleted).findFirst().orElse(false);
						isUpdate.set(isCompleted);
						is.addPages(WorkflowSubmissionConverter
								.convertWorkflowSubmissionInputDTOtoPages(workflowSubmissionInput, true));
					}
					is.addComments(WorkflowSubmissionConverter
							.convertWorkflowSubmissionInputDTOtoComments(workflowSubmissionInput));

					return is;
				})
				.orElse(new WorkflowSubmission(workflowSubmissionInput.getWorkflowId(),
						WorkflowSubmissionConverter.convertWorkflowSubmissionInputDTOtoPages(workflowSubmissionInput,
								true),
						WorkflowSubmissionConverter
								.convertWorkflowSubmissionInputDTOtoComments(workflowSubmissionInput)));

		runtimeService.setVariable(executionId, CommonConstant.INTERIM_STATE,
				workflowSubmissionUtil.convertToString(interimState));
		runtimeService.setVariable(executionId, CommonConstant.MODIFIED_AT, LocalDateTime.now());
		TaskService taskService = processEngine.getTaskService();
		if (!isUpdate.get()) {
			taskService.complete(currentTask.getId());
		}

		return null;

	}

	@Override
	public WorkflowOutputDTO handleWorkflowResubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput, String tenantId) {

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		Task currentTask = Optional
				.ofNullable(processEngine.getTaskService().createTaskQuery()
						.processInstanceId(workflowSubmissionInput.getWorkflowId()).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException(
						"Invalid workflow ID. Workflow instance has already been completed."));

		RuntimeService runtimeService = processEngine.getRuntimeService();
		String executionId = currentTask.getExecutionId();

		runtimeService.setVariable(executionId, "resubmission", true);

		WorkflowSubmission interimState = WorkflowUtil
				.getRuntimeWorkflowStringVariable(runtimeService, executionId, "interimState").map(s -> {
					WorkflowSubmission is = workflowSubmissionUtil.convertToWorkflowSubmission(s);
					if (!workflowSubmissionInput.getPages().isEmpty()) {
						is.addPages(WorkflowSubmissionConverter
								.convertWorkflowSubmissionInputDTOtoPages(workflowSubmissionInput, true));
					}
					is.addComments(WorkflowSubmissionConverter
							.convertWorkflowSubmissionInputDTOtoComments(workflowSubmissionInput));
					return is;
				}).orElseThrow(() -> new IllegalArgumentException("Invalid workflow."));

		runtimeService.setVariable(executionId, "interimState", workflowSubmissionUtil.convertToString(interimState));
		runtimeService.setVariable(executionId, CommonConstant.MODIFIED_AT, LocalDateTime.now());

		TaskService taskService = processEngine.getTaskService();
		taskService.complete(currentTask.getId());

		return null;
	}

	private QuestionnaireOutputDTO retriveQuestionnaire(String tenantId) {
		if (!questionnaireServiceProperties.getFindById().containsKey(tenantId)) {
			throw new IllegalArgumentException(CommonConstant.INVALID_TENANT_MSG + tenantId);
		}

		return restTemplate.getForObject(questionnaireServiceProperties.getFindById().get(tenantId),
				QuestionnaireOutputDTO.class);
	}

	private BasicWorkflowOutputDTO createBasicWorkflowOutputDTO(String id, String title, String workflowType,
			String status, LocalDateTime startedTime, LocalDateTime modifiedTime, String tenantId) {
		return new BasicWorkflowOutputDTO(id, title, workflowType, WorkFlowStatus.valueOf(status), startedTime, "",
				modifiedTime, "", tenantId);
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
					.filter(ei -> WorkflowUtil
							.getRuntimeWorkflowStringVariable(runtimeService, ei, CommonConstant.TENANT_ID_KEY, "")
							.equals(tenantId))
					.filter(ei -> Optional.ofNullable((Boolean) runtimeService.getVariable(ei, CommonConstant.DELETED))
							.map(b -> !b).orElse(true))
					.map(ei -> createBasicWorkflowOutputDTO(pi.getId(),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, CommonConstant.TITLE, ""),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei,
									CommonConstant.WORKFLOW_TYPE, ""),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei, CommonConstant.STATUS,
									"SUBMISSION_IN_PROGRESS"),
							LocalDateTime.parse(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei,
									CommonConstant.CREATED_AT, LocalDateTime.now().toString())),
							LocalDateTime.parse(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei,
									CommonConstant.MODIFIED_AT, LocalDateTime.now().toString())),
							WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, ei,
									CommonConstant.TENANT_ID_KEY, "")))
					.orElse(null);
		}).filter(Objects::nonNull).toList());

		HistoryService historyService = processEngine.getHistoryService();
		List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
				.finished().list();

		workflowOutputs.addAll(historicProcessInstances.stream().map(pi -> {
			HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService
					.createHistoricVariableInstanceQuery().processInstanceId(pi.getId())
					.variableName(CommonConstant.DELETED);

			HistoricVariableInstance historicVariableInstance = historicVariableInstanceQuery.singleResult();
			Boolean deleted = historicVariableInstance != null ? (Boolean) historicVariableInstance.getValue() : null;

			return Optional.ofNullable(pi)
					.filter(hpi -> WorkflowUtil.getHistoricWorkflowStringVariable(historyService, hpi.getId(),
							CommonConstant.TENANT_ID_KEY, "").equals(tenantId))
					.filter(hpi -> deleted == null || !deleted)
					.map(hpi -> createBasicWorkflowOutputDTO(hpi.getId(),
							WorkflowUtil.getHistoricWorkflowStringVariable(historyService, hpi.getId(),
									CommonConstant.TITLE, ""),
							WorkflowUtil.getHistoricWorkflowStringVariable(historyService, hpi.getId(),
									CommonConstant.WORKFLOW_TYPE, ""),
							WorkflowUtil.getHistoricWorkflowStringVariable(historyService, hpi.getId(),
									CommonConstant.STATUS, "SUBMISSION_IN_PROGRESS"),
							LocalDateTime.parse(WorkflowUtil.getHistoricWorkflowStringVariable(historyService,
									hpi.getId(), CommonConstant.CREATED_AT, LocalDateTime.now().toString())),
							LocalDateTime.parse(WorkflowUtil.getHistoricWorkflowStringVariable(historyService,
									hpi.getId(), CommonConstant.MODIFIED_AT, LocalDateTime.now().toString())),
							WorkflowUtil.getHistoricWorkflowStringVariable(historyService, hpi.getId(),
									CommonConstant.TENANT_ID_KEY, "")))
					.orElse(null);
		}).filter(Objects::nonNull).toList());

		return workflowOutputs;
	}

	@Override
	public WorkflowOutputDTO getWorkflowById(String id, String tenantId) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);

		String executionId = processEngine.getTaskService().createTaskQuery().processInstanceId(id).list().stream()
				.findAny().map(Task::getExecutionId).orElse(null);

		RuntimeService runtimeService = processEngine.getRuntimeService();
		HistoryService historyService = processEngine.getHistoryService();

		boolean isDeleted = isWorkflowDeleted(id);
		if (isDeleted) {
			throw new IllegalStateException("Workflow instance " + id + " has been deleted or not found");
		}

		String workflowTenantId = Objects.nonNull(executionId)
				? WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
						CommonConstant.TENANT_ID_KEY, "")
				: WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, CommonConstant.TENANT_ID_KEY, "");

		if (!workflowTenantId.equals(tenantId)) {
			throw new IllegalStateException("Invalid Tenant ID provided !!!");
		}

		return Objects.nonNull(executionId)
				? new WorkflowOutputDTO(id,
						ActivitiType.valueOf(
								WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
										CommonConstant.ACTIVITY_TYPE, "FORM_FILLING")),
						WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId, CommonConstant.TITLE,
								""),
						mapWorkflowSubmissionInputToQuestionnaire(WorkflowUtil.getRuntimeWorkflowStringVariable(
								runtimeService, executionId, CommonConstant.INTERIM_STATE, "{}"), tenantId),
						LocalDateTime.parse(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
								CommonConstant.CREATED_AT, LocalDateTime.now().toString())),
						"",
						LocalDateTime.parse(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
								CommonConstant.MODIFIED_AT, LocalDateTime.now().toString())),
						"",
						WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
								CommonConstant.TENANT_ID_KEY, ""),
						WorkFlowStatus.valueOf(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService,
								executionId, "status", "INITIATED")))
				: new WorkflowOutputDTO(id,
						ActivitiType.valueOf(WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id,
								CommonConstant.ACTIVITY_TYPE, "FORM_FILLING")),
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, CommonConstant.TITLE, ""),
						mapWorkflowSubmissionInputToQuestionnaire(WorkflowUtil.getHistoricWorkflowStringVariable(
								historyService, id, CommonConstant.INTERIM_STATE, "{}"), tenantId),
						LocalDateTime
								.parse(WorkflowUtil.getHistoricWorkflowStringVariable(
										historyService, id, CommonConstant.CREATED_AT, LocalDateTime.now().toString())),
						"",
						LocalDateTime.parse(WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id,
								CommonConstant.MODIFIED_AT, LocalDateTime.now().toString())),
						"",
						WorkflowUtil.getHistoricWorkflowStringVariable(historyService, id, CommonConstant.TENANT_ID_KEY,
								""),
						WorkFlowStatus.valueOf(WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService,
								executionId, "status", "INITIATED")));

	}

	private boolean isWorkflowDeleted(String id) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		HistoryService historyService = processEngine.getHistoryService();

		// Check history to see if the workflow has been deleted
		HistoricVariableInstance deletedVariable = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(id).variableName("deleted").singleResult();

		return deletedVariable != null && (Boolean) deletedVariable.getValue();
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

		Map<String, Boolean> pageIdToCompletedMap = workflowSubmissionInput.getPages().stream()
				.collect(Collectors.toMap(WorkflowSubmissionPageInputDTO::getId,
						WorkflowSubmissionPageInputDTO::isCompleted, (r1, r2) -> r1));

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
				return new Page(p.getIndex(), p.getId(), p.getTitle(), qs,
						Optional.ofNullable(pageIdToCompletedMap.get(p.getId())).orElse(false));
			}).toList();

			questionnaire = new QuestionnaireOutputDTO(questionnaire.getId(), questionnaire.getTitle(),
					questionnaire.getCreatedBy(), questionnaire.getCreatedAt(), questionnaire.getModifiedBy(),
					questionnaire.getModifiedAt(), pages, comments, questionnaire.getTenantId());
		}

		return questionnaire;
	}

	@Override
	public void changeActiveWorkflow(String processDefinitionKey, String tenantId) {

		WorkflowActiveStatus currentWorkflowActiveStatus = workflowActiveStatusService
				.findByActiveTrueAndTenantId(tenantId);

		WorkflowActiveStatus workflowActiveStatusToUpdate = workflowActiveStatusService
				.findByProcessDefinitionKeyAndTenantId(processDefinitionKey, tenantId);

		workflowActiveStatusService.updateWorkflowActiveStatus(currentWorkflowActiveStatus.getId(), false);
		workflowActiveStatusService.updateWorkflowActiveStatus(workflowActiveStatusToUpdate.getId(), true);
	}

	@Override
	public void deleteWorkflowById(String id, String tenantId) {
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		Task currentTask = Optional
				.ofNullable(
						processEngine.getTaskService().createTaskQuery().processInstanceId(id).active().singleResult())
				.orElseThrow(() -> new IllegalArgumentException("Workflow instance not found for ID: " + id));

		String executionId = currentTask.getExecutionId();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		String taskTenantId = WorkflowUtil.getRuntimeWorkflowStringVariable(runtimeService, executionId,
				CommonConstant.TENANT_ID_KEY, "");

		if (!taskTenantId.equals(tenantId)) {
			throw new IllegalArgumentException("Invalid tenant or workflow instance not found for ID: " + id);
		}

		processEngine.getRuntimeService().setVariable(executionId, CommonConstant.DELETED, true);
		runtimeService.deleteProcessInstance(id, "Deleted by user");
	}
}
