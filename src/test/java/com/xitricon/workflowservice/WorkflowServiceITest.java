package com.xitricon.workflowservice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.xitricon.workflowservice.dto.CommentInputDTO;
import com.xitricon.workflowservice.dto.QuestionnaireOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionQuestionInputDTO;
import com.xitricon.workflowservice.model.WorkflowActiveStatus;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.repository.WorkflowActiveStatusRepository;
import com.xitricon.workflowservice.service.impl.WorkflowServiceImpl;
import com.xitricon.workflowservice.util.CommonConstant;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class WorkflowServiceITest {

	private static final String WORKFLOW_SUBMISSION_ENDPOINT = "/api/workflows/submission";
	private static final String WORKFLOW_INITIALIZATION_ENDPOINT = "/api/workflows/init";
	private static final String WORKFLOW_RESUBMISSION_ENDPOINT = "/api/workflows/resubmission";
	private static final String GET_WORKFLOWS_ENDPOINT = "/api/workflows";
	private static final String GET_WORKFLOWS_BY_ID = "/api/workflows/{id}";
	private static final String CHANGE_ACTIVE_STATUS_ENDPOINT = "/api/workflows/active/{workflowId}";
	private static final String DELETE_WORKFLOWS_BY_ID = "/api/workflows/{id}";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String COMMENTED_BY = "kasuni.s@xitricon.com";
	private static final LocalDateTime NOW = LocalDateTime.now();
	private static final String COMMENT = "Comment";
	private static final String RESPONSE_ONE = "Response one";
	private static final String RESPONSE_TWO = "Response two";
	private static final String TENENT_ID_ONE = "T_1";
	private static final String REF_ONE = "REF001";

	@LocalServerPort
	private int port;

	@Value("${external-api.questionnaire-service.find-by-id.T_1}")
	private String questionnaireServiceUrl;

	@Autowired
	WorkflowServiceImpl workflowServiceImpl;

	@Autowired
	WorkflowActiveStatusRepository workflowActiveStatusRepository;

	private WorkflowOutputDTO workflowOne;
	private CommentInputDTO commentInputDTO;
	private WorkflowSubmissionQuestionInputDTO questionInputDTO;
	private WorkflowSubmissionPageInputDTO pageInputDTO;
	private QuestionnaireOutputDTO questionnaire;

	@BeforeAll
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<QuestionnaireOutputDTO> responseEntity = restTemplate.getForEntity(questionnaireServiceUrl,
				QuestionnaireOutputDTO.class);

		questionnaire = responseEntity.getBody();

		commentInputDTO = new CommentInputDTO(REF_ONE, COMMENTED_BY, NOW, COMMENT);

		questionInputDTO = new WorkflowSubmissionQuestionInputDTO(
				questionnaire.getPages().get(0).getQuestions().get(0).getId(),
				questionnaire.getPages().get(0).getQuestions().get(0).getIndex(), List.of(RESPONSE_ONE, RESPONSE_TWO),
				questionnaire.getPages().get(0).getQuestions().get(0).getLabel());

		pageInputDTO = new WorkflowSubmissionPageInputDTO(questionnaire.getPages().get(0).getIndex(),
				questionnaire.getPages().get(0).getId(), questionnaire.getPages().get(0).getTitle(),
				List.of(questionInputDTO), false);

	}

	@Test
	void testWorkflowInitialization() {

		String response = RestAssured.given().contentType(ContentType.JSON)
				.queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE).get(WORKFLOW_INITIALIZATION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK).body("id", notNullValue()).body("title", equalTo(workflowOne.getTitle()))
				.body("createdAt", notNullValue()).body("modifiedAt", notNullValue())
				.body("createdBy", equalTo(workflowOne.getCreatedBy())).body("modifiedBy", notNullValue())
				.body("questionnaire", notNullValue()).body("questionnaire.id", equalTo(questionnaire.getId()))
				.body("questionnaire.title", equalTo(questionnaire.getTitle()))
				.body("questionnaire.createdAt",
						equalTo(questionnaire.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("questionnaire.modifiedAt",
						equalTo(questionnaire.getModifiedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("questionnaire.createdBy", equalTo(questionnaire.getCreatedBy()))
				.body("questionnaire.modifiedBy", equalTo(questionnaire.getModifiedBy()))
				.body("questionnaire.pages", notNullValue()).body("questionnaire.pages.size()", equalTo(5))
				.body("questionnaire.pages[0].id", equalTo(questionnaire.getPages().get(0).getId()))
				.body("questionnaire.pages[0].index", equalTo(questionnaire.getPages().get(0).getIndex()))
				.body("questionnaire.pages[0].title", equalTo(questionnaire.getPages().get(0).getTitle()))
				.body("questionnaire.pages[0].questions", notNullValue())
				.body("questionnaire.pages[0].questions.size()",
						equalTo(questionnaire.getPages().get(0).getQuestions().size()))
				.body("questionnaire.pages[0].questions[0].id",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getId()))
				.body("questionnaire.pages[0].questions[0].index",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getIndex()))
				.body("questionnaire.pages[0].questions[0].label",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getLabel()))
				.body("questionnaire.pages[0].questions[0].type",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getType()))
				.body("questionnaire.pages[0].questions[0].group",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getGroup()))
				.body("questionnaire.pages[0].questions[0].editable",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).isEditable()))
				.body("questionnaire.pages[0].questions[0].optionsSource",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getOptionsSource()))
				.body("questionnaire.pages[0].questions[0].subQuestions",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getSubQuestions()))
				.body("questionnaire.pages[0].questions[0].validations[0].required", equalTo(
						questionnaire.getPages().get(0).getQuestions().get(0).getValidations().get(0).isRequired()))
				.extract().asString();

		String workflowId = JsonPath.from(response).getString("id");

		deleteWorkflow(workflowId);
	}

	@Test
	void testWorkflowInitializationWithInvalidTenant() {

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, "T_3")
				.get(WORKFLOW_INITIALIZATION_ENDPOINT).then().statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	void testWorkflowSubmission() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).pathParam("id", workflowOne.getId())
				.queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE).get(GET_WORKFLOWS_BY_ID).then()
				.statusCode(HttpStatus.SC_OK).body("id", notNullValue()).body("title", notNullValue())
				.body("createdAt", notNullValue()).body("modifiedAt", notNullValue())
				.body("createdBy", equalTo(workflowOne.getCreatedBy())).body("modifiedBy", notNullValue())
				.body("questionnaire", notNullValue()).body("questionnaire.id", equalTo(questionnaire.getId()))
				.body("questionnaire.title", equalTo(questionnaire.getTitle()))
				.body("questionnaire.createdAt",
						equalTo(questionnaire.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("questionnaire.modifiedAt",
						equalTo(questionnaire.getModifiedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("questionnaire.createdBy", equalTo(questionnaire.getCreatedBy()))
				.body("questionnaire.modifiedBy", equalTo(questionnaire.getModifiedBy()))
				.body("questionnaire.pages", notNullValue()).body("questionnaire.pages.size()", equalTo(5))
				.body("questionnaire.pages[0].id", equalTo(questionnaire.getPages().get(0).getId()))
				.body("questionnaire.pages[0].index", equalTo(questionnaire.getPages().get(0).getIndex()))
				.body("questionnaire.pages[0].title", equalTo(questionnaire.getPages().get(0).getTitle()))
				.body("questionnaire.pages[0].questions", notNullValue())
				.body("questionnaire.pages[0].questions.size()",
						equalTo(questionnaire.getPages().get(0).getQuestions().size()))
				.body("questionnaire.pages[0].questions[0].id",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getId()))
				.body("questionnaire.pages[0].questions[0].index",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getIndex()))
				.body("questionnaire.pages[0].questions[0].label",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getLabel()))
				.body("questionnaire.pages[0].questions[0].type",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getType()))
				.body("questionnaire.pages[0].questions[0].group",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getGroup()))
				.body("questionnaire.pages[0].questions[0].editable",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).isEditable()))
				.body("questionnaire.pages[0].questions[0].optionsSource",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getOptionsSource()))
				.body("questionnaire.pages[0].questions[0].subQuestions",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getSubQuestions()))
				.body("questionnaire.pages[0].questions[0].validations[0].required",
						equalTo(questionnaire.getPages().get(0).getQuestions().get(0).getValidations().get(0)
								.isRequired()))
				.body("questionnaire.pages[0].questions[0].response", equalTo(List.of(RESPONSE_ONE)));

		deleteWorkflow(workflowOne.getId());
	}

	@Test
	void testWorkflowSubmissionWithInvalidTenant() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam(CommonConstant.TENANT_ID_KEY, CommonConstant.TENANT_TWO_KEY)
				.post(WORKFLOW_SUBMISSION_ENDPOINT).then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		deleteWorkflow(workflowOne.getId());

	}

	@Test
	void testGetWorkflows() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].title", equalTo(workflowOne.getTitle()))
				.body("[0].createdAt",
						equalTo(workflowOne.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("[0].modifiedAt", notNullValue()).body("[0].createdBy", equalTo(workflowOne.getCreatedBy()))
				.body("[0].modifiedBy", notNullValue()).body("[0].status", notNullValue());
		deleteWorkflow(workflowOne.getId());
	}

	@Test
	void testGetWorkflowsWithInvalidTenant() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON)
				.queryParam(CommonConstant.TENANT_ID_KEY, CommonConstant.TENANT_TWO_KEY).get(GET_WORKFLOWS_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(0));
		deleteWorkflow(workflowOne.getId());
	}

	@Test
	void testDeleteWorkflowById() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		String workflowIdToDelete = workflowOne.getId();
		RestAssured.given().contentType(ContentType.JSON).pathParam("id", workflowIdToDelete)
				.queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE).delete(DELETE_WORKFLOWS_BY_ID).then()
				.statusCode(HttpStatus.SC_NO_CONTENT);
		RestAssured.given().contentType(ContentType.JSON).pathParam("id", workflowIdToDelete)
				.queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE).get(GET_WORKFLOWS_BY_ID).then()
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	void testDeleteWorkflowByIdWithInvalidTenant() {
		initializeWorkflow();
		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		String workflowIdToDelete = workflowOne.getId();
		RestAssured.given().contentType(ContentType.JSON).pathParam("id", workflowIdToDelete)
				.queryParam(CommonConstant.TENANT_ID_KEY, "T_3").delete(DELETE_WORKFLOWS_BY_ID).then()
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

		deleteWorkflow(workflowOne.getId());

	}

	@Test
	void testWorkflowSubmissionWithReviewerAndApprover() {
		initializeWorkflow();
		// Requestor Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.SUBMISSION_IN_PROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEW.toString()));

		// Reviewer Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEW_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE1.toString()));

		// Approver Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.APPROVED.toString()));

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		HistoryService historyService = processEngine.getHistoryService();

		historyService.deleteHistoricProcessInstance(workflowOne.getId());

	}

	@Test
	void testWorkflowSubmissionWithReviewerAndTwoApprovers() {

		RestAssured.given().contentType(ContentType.JSON)
				.pathParam("workflowId", TestConstants.SUPPLIER_ONBOARDING_PROCESS_TWO_ID)
				.queryParam("tenantId", TENENT_ID_ONE).put(CHANGE_ACTIVE_STATUS_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		initializeWorkflow();
		// Requestor Flow
		// Submission One

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.SUBMISSION_IN_PROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEW.toString()));

		// Reviewer Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEW_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE1.toString()));

		// Approver Flow One

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE2.toString()));

		// Approver Flow Two

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE2.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.APPROVED.toString()));

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		HistoryService historyService = processEngine.getHistoryService();

		historyService.deleteHistoricProcessInstance(workflowOne.getId());

		RestAssured.given().contentType(ContentType.JSON)
				.pathParam("workflowId", TestConstants.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
				.queryParam("tenantId", TENENT_ID_ONE).put(CHANGE_ACTIVE_STATUS_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

	}

	@Test
	void testWorkflowSubmissionWithRequestorResubmission() {
		initializeWorkflow();
		// Requestor Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.SUBMISSION_IN_PROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEW.toString()));

		// Reviewer Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEW_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five - Resubmission Request
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE)
				.post(WORKFLOW_RESUBMISSION_ENDPOINT).then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_CORRECTION.toString()));

		// Requestor Flow - Resubmission

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.CORRECTION_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five - requestor comment
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEW.toString()));

		// Reviewer Flow - second round

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEW_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five - Resubmission Request
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE1.toString()));

		// Approver Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.APPROVED.toString()));

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		HistoryService historyService = processEngine.getHistoryService();

		historyService.deleteHistoricProcessInstance(workflowOne.getId());

	}

	@Test
	void testWorkflowSubmissionWithReviewerResubmission() {
		initializeWorkflow();
		// Requestor Flow
		// Submission One

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.SUBMISSION_IN_PROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEW.toString()));

		// Reviewer Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEW_INPROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE1.toString()));

		// Approver Flow

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five - Resubmission request
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE)
				.post(WORKFLOW_RESUBMISSION_ENDPOINT).then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_REVIEWER_CORRECTIONS.toString()));

		// Reviewer Flow - Resubmission

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.REVIEWER_CORRECTIONS_IN_PROGRESS.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.PENDING_APPROVAL_STAGE1.toString()));

		// Approver Flow - Second Round

		// Submission One
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(0))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue())
				.body("[0].status", equalTo(WorkFlowStatus.APPROVAL_INPROGRESS_STAGE1.toString()));

		// Submission two
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(1))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission three
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(2))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission four
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(3))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		// submission five
		RestAssured.given().contentType(ContentType.JSON).body(createWorkflowSubmissionInputDTO(4))
				.queryParam("completed", true).queryParam("tenantId", TENENT_ID_ONE).post(WORKFLOW_SUBMISSION_ENDPOINT)
				.then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).queryParam(CommonConstant.TENANT_ID_KEY, TENENT_ID_ONE)
				.get(GET_WORKFLOWS_ENDPOINT).then().statusCode(HttpStatus.SC_OK).body("size()", equalTo(1))
				.body("[0].id", notNullValue()).body("[0].status", equalTo(WorkFlowStatus.APPROVED.toString()));

		ProcessEngine processEngine = ProcessEngines.getProcessEngine(CommonConstant.PROCESS_ENGINE_NAME);
		HistoryService historyService = processEngine.getHistoryService();

		historyService.deleteHistoricProcessInstance(workflowOne.getId());

	}

	@Test
	void testChangeActiveWorkflow() {

		WorkflowActiveStatus workflowActiveStatus = this.workflowActiveStatusRepository
				.findByActiveTrueAndTenantId(TENENT_ID_ONE).get();

		assertEquals(TestConstants.SUPPLIER_ONBOARDING_PROCESS_ONE_ID, workflowActiveStatus.getProcessDefinitionKey());
		assertEquals(TENENT_ID_ONE, workflowActiveStatus.getTenantId());
		assertTrue(workflowActiveStatus.isActive());

		RestAssured.given().contentType(ContentType.JSON)
				.pathParam("workflowId", TestConstants.SUPPLIER_ONBOARDING_PROCESS_TWO_ID)
				.queryParam("tenantId", TENENT_ID_ONE).put(CHANGE_ACTIVE_STATUS_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		workflowActiveStatus = this.workflowActiveStatusRepository.findByActiveTrueAndTenantId(TENENT_ID_ONE).get();

		assertEquals(TestConstants.SUPPLIER_ONBOARDING_PROCESS_TWO_ID, workflowActiveStatus.getProcessDefinitionKey());
		assertEquals(TENENT_ID_ONE, workflowActiveStatus.getTenantId());
		assertTrue(workflowActiveStatus.isActive());

		RestAssured.given().contentType(ContentType.JSON)
				.pathParam("workflowId", TestConstants.SUPPLIER_ONBOARDING_PROCESS_ONE_ID)
				.queryParam("tenantId", TENENT_ID_ONE).put(CHANGE_ACTIVE_STATUS_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK);

		workflowActiveStatus = this.workflowActiveStatusRepository.findByActiveTrueAndTenantId(TENENT_ID_ONE).get();

		assertEquals(TestConstants.SUPPLIER_ONBOARDING_PROCESS_ONE_ID, workflowActiveStatus.getProcessDefinitionKey());
		assertEquals(TENENT_ID_ONE, workflowActiveStatus.getTenantId());
		assertTrue(workflowActiveStatus.isActive());
	}

	@Test
	void testChangeActiveWorkflowwithInvalidTenant() {

		RestAssured.given().contentType(ContentType.JSON)
				.pathParam("workflowId", TestConstants.SUPPLIER_ONBOARDING_PROCESS_ONE_ID).queryParam("tenantId", "T_3")
				.put(CHANGE_ACTIVE_STATUS_ENDPOINT).then().statusCode(HttpStatus.SC_NOT_FOUND);

	}

	private void initializeWorkflow() {
		workflowOne = workflowServiceImpl.initiateWorkflow(TENENT_ID_ONE);
	}

	private void deleteWorkflow(String workflowId) {

		workflowServiceImpl.deleteWorkflowById(workflowId, TENENT_ID_ONE);
	}

	private WorkflowSubmissionInputDTO createWorkflowSubmissionInputDTO(int pageIndex) {
		questionInputDTO = new WorkflowSubmissionQuestionInputDTO(
				workflowOne.getQuestionnaire().getPages().get(pageIndex).getQuestions().get(0).getId(),
				workflowOne.getQuestionnaire().getPages().get(pageIndex).getQuestions().get(0).getIndex(),
				List.of(RESPONSE_ONE),
				workflowOne.getQuestionnaire().getPages().get(pageIndex).getQuestions().get(0).getLabel());

		String pageId = (pageIndex == 4) ? UUID.randomUUID().toString()
				: workflowOne.getQuestionnaire().getPages().get(pageIndex).getId();

		pageInputDTO = new WorkflowSubmissionPageInputDTO(
				workflowOne.getQuestionnaire().getPages().get(pageIndex).getIndex(), pageId,
				workflowOne.getQuestionnaire().getPages().get(pageIndex).getTitle(), List.of(questionInputDTO), true);

		return new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO), List.of(commentInputDTO));

	}

}
