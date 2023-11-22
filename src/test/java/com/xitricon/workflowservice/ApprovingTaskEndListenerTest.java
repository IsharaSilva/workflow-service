package com.xitricon.workflowservice;

import java.time.LocalDateTime;
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
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskEndListener;
import com.xitricon.workflowservice.model.Comment;
import com.xitricon.workflowservice.model.WorkflowSubmission;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(MockitoExtension.class)
class ApprovingTaskEndListenerTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private DelegateExecution execution;

	@InjectMocks
	private ApprovingTaskEndListener listener;

	private static final int PORT = 8081;
	private static final String API_PATH = "/api/supplier-onboarding-request";

	@BeforeEach
	public void setUp() {
		RestAssured.port = PORT;
	}

	@Test
	void testNotify() {
		LocalDateTime commentedAt1 = LocalDateTime.now();
		LocalDateTime commentedAt2 = LocalDateTime.now().minusHours(1);

		WorkflowSubmission workflowSubmission = WorkflowSubmission.builder().workflowId("123")
				.comments(List.of(new Comment("0", commentedAt1, "user1", "Good job"),
						new Comment("1", commentedAt2, "user2", "Another comment")))
				.build();

		ObjectMapper objectMapper = new ObjectMapper();
		String interimStateJson;
		try {
			interimStateJson = objectMapper.writeValueAsString(workflowSubmission);
		} catch (JsonProcessingException e) {
			return;
		}

		given().contentType(ContentType.JSON).body(interimStateJson).when().post(API_PATH).then()
				.statusCode(HttpStatus.CREATED.value()).body("workflowId", notNullValue())
				.body("comments[0].refId", equalTo("0")).body("comments[0].commentedBy", equalTo("user1"))
				.body("comments[0].commentText", equalTo("Good job"))
				.body("comments[0].commentedAt", equalTo(commentedAt1.toString()))
				.body("comments[1].refId", equalTo("1")).body("comments[1].commentedBy", equalTo("user2"))
				.body("comments[1].commentText", equalTo("Another comment"))
				.body("comments[1].commentedAt", equalTo(commentedAt2.toString()))

				.log().all();
	}

}