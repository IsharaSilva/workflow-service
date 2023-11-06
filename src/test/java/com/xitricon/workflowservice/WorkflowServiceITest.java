package com.xitricon.workflowservice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.xitricon.workflowservice.service.impl.WorkflowServiceImpl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class WorkflowServiceITest {

	private static final String WORKFLOW_SUBMISSION_ENDPOINT = "/api/workflows/submission";
	private static final String GET_WORKFLOWS_ENDPOINT = "/api/workflows";
	private static final String GET_WORKFLOWS_BY_ID = "/api/workflows/{id}";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String COMMENTED_BY = "kasuni.s@xitricon.com";
	private static final LocalDateTime NOW = LocalDateTime.now();
	private static final String COMMENT = "Comment";
	private static final String RESPONSE_ONE = "Response one";
	private static final String RESPONSE_TWO = "Response two";

	@LocalServerPort
	private int port;

	@Value("${external-api.questionnaire-service.find-by-id}")
	private String questionnaireServiceUrl;

	@Autowired
	WorkflowServiceImpl workflowServiceImpl;

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

		workflowOne = workflowServiceImpl.initiateWorkflow();

		commentInputDTO = new CommentInputDTO(COMMENTED_BY, NOW, COMMENT);

		questionInputDTO = new WorkflowSubmissionQuestionInputDTO(
				questionnaire.getPages().get(0).getQuestions().get(0).getId(),
				questionnaire.getPages().get(0).getQuestions().get(0).getIndex(), List.of(RESPONSE_ONE, RESPONSE_TWO));

		pageInputDTO = new WorkflowSubmissionPageInputDTO(questionnaire.getPages().get(0).getIndex(),
				questionnaire.getPages().get(0).getId(), List.of(questionInputDTO));

	}

	@Test
	public void testWorkflowSubmission() {

		WorkflowSubmissionInputDTO workflow = new WorkflowSubmissionInputDTO(workflowOne.getId(), List.of(pageInputDTO),
				List.of(commentInputDTO));

		RestAssured.given().contentType(ContentType.JSON).body(workflow).queryParam("completed", false)
				.post(WORKFLOW_SUBMISSION_ENDPOINT).then().statusCode(HttpStatus.SC_OK);

		RestAssured.given().contentType(ContentType.JSON).pathParam("id", workflowOne.getId()).get(GET_WORKFLOWS_BY_ID)
				.then().statusCode(HttpStatus.SC_OK).body("id", notNullValue())
				.body("title", equalTo(workflowOne.getTitle())).body("createdAt", notNullValue())
				.body("modifiedAt", notNullValue()).body("createdBy", equalTo(workflowOne.getCreatedBy()))
				.body("modifiedBy", notNullValue()).body("questionnaire", notNullValue())
				.body("questionnaire.id", equalTo(questionnaire.getId()))
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
				.body("questionnaire.pages[0].questions[0].response", equalTo(List.of(RESPONSE_ONE, RESPONSE_TWO)));

	}

	@Test
	public void testGetWorkflows() {
		RestAssured.given().contentType(ContentType.JSON).get(GET_WORKFLOWS_ENDPOINT).then()
				.statusCode(HttpStatus.SC_OK).body("size()", equalTo(1)).body("[0].id", notNullValue())
				.body("[0].title", equalTo(workflowOne.getTitle()))
				.body("[0].createdAt",
						equalTo(workflowOne.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
				.body("[0].modifiedAt", notNullValue()).body("[0].createdBy", equalTo(workflowOne.getCreatedBy()))
				.body("[0].modifiedBy", notNullValue()).body("[0].status", notNullValue());
	}

}
