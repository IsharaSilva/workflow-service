package com.xitricon.workflowservice;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskEndListener;
import com.xitricon.workflowservice.model.Comment;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import com.xitricon.workflowservice.util.CommonConstant;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ExtendWith(MockitoExtension.class)
class ApprovingTaskEndListenerTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private DelegateExecution execution;

	@InjectMocks
	private ApprovingTaskEndListener listener;

	private static final int PORT = 8082;
	private static final String API_PATH = "/api/supplier-onboarding-request";
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstant.DATE_TIME_FORMAT);

	@BeforeEach
	public void setUp() {
		RestAssured.port = PORT;
	}

	@Test
	void testNotify() throws JsonProcessingException {
		LocalDateTime commentedAt1 = LocalDateTime.now();
		LocalDateTime commentedAt2 = LocalDateTime.now().minusHours(1);

		WorkflowSubmission workflowSubmission = WorkflowSubmission.builder().workflowId("123")
				.comments(List.of(new Comment("0", commentedAt1, "user1", "Good job"),
						new Comment("1", commentedAt2, "user2", "Another comment")))
				.pages(List.of(new Page(1, "pageId1", null, false), new Page(2, "pageId2", null, true))).build();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		String interimStateJson = objectMapper.writeValueAsString(workflowSubmission);

		given().contentType(ContentType.JSON).body(interimStateJson).when().post(API_PATH).then()
				.statusCode(HttpStatus.CREATED.value()).body("id", notNullValue()).body("comments.size()", equalTo(2))
				.body("comments[0].commentedBy", equalTo("user1")).body("comments[0].commentText", equalTo("Good job"))
				.body("comments[0].commentedAt", equalTo(commentedAt1.format(formatter)))
				.body("comments[1].commentedBy", equalTo("user2"))
				.body("comments[1].commentText", equalTo("Another comment"))
				.body("comments[1].commentedAt", equalTo(commentedAt2.format(formatter)))
				.body("pages.size()", equalTo(2)).body("pages[0].id", equalTo("pageId1"))
				.body("pages[1].id", equalTo("pageId2"));
	}
}