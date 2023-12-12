package com.xitricon.workflowservice;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowEndListener;
import com.xitricon.workflowservice.model.Comment;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.WorkflowSubmission;
import com.xitricon.workflowservice.util.CommonConstant;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@ExtendWith(MockitoExtension.class)
@Disabled
class ApprovingTaskEndListenerTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private DelegateExecution execution;

	@InjectMocks
	private ApprovingProcessFlowEndListener listener;

	@Value("{external-api.onboarding-service.base}")
	private String onboardingBaseUrl;

	private static final int PORT = 8081;
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
				.pages(List.of(Page.builder().id("pageId1").index(1).completed(false).title("Title1").build(),
						Page.builder().id("pageId2").index(2).completed(true).title("Title2").build()))
				.build();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		given().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(workflowSubmission)).when()
				.post(onboardingBaseUrl + API_PATH).then().statusCode(HttpStatus.CREATED.value())
				.body("id", notNullValue()).body("comments.size()", equalTo(2))
				.body("comments[0].commentedBy", equalTo("user1")).body("comments[0].commentText", equalTo("Good job"))
				.body("comments[0].commentedAt", equalTo(commentedAt1.format(formatter)))
				.body("comments[1].commentedBy", equalTo("user2"))
				.body("comments[1].commentText", equalTo("Another comment"))
				.body("comments[1].commentedAt", equalTo(commentedAt2.format(formatter)))
				.body("pages.size()", equalTo(2)).body("pages[0].id", equalTo("pageId1"))
				.body("pages[0].title", equalTo("Title1")).body("pages[1].id", equalTo("pageId2"));
	}
}