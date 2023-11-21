package com.xitricon.workflowservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertNotNull;
 import java.util.List;
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import org.springframework.web.client.RestTemplate;
 import static io.restassured.RestAssured.given;
 
 import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskEndListener;
 import com.xitricon.workflowservice.dto.CommentOutputDTO;
 import com.xitricon.workflowservice.dto.Page;
 
 import io.restassured.RestAssured;
 import io.restassured.http.ContentType;
 import io.restassured.http.Header;
 import io.restassured.http.Headers;
 import io.restassured.response.Response;
 
 @ExtendWith(MockitoExtension.class)
 public class ApprovingTaskEndListenerTest {
 
     @Mock
     private RestTemplate restTemplate;
 
     @Mock
     private DelegateExecution execution;
     
     @InjectMocks
     private ApprovingTaskEndListener listener;
     
 
     private static final int PORT = 8081;
     private static final int STATUS_CODE = 201;
     private static final String API_PATH = "/api/supplier-onboarding-request";
 
     private static final String SAMPLE_JSON = "{\"id\":\"123\",\"title\":\"Sample Title\",\"questionnaireId\":\"456\",\"initiator\":\"user1\",\"reviewer\":\"user2\",\"approver\":\"user3\",\"createdAt\":\"2023-01-01T12:00:00\",\"modifiedAt\":\"2023-01-02T12:00:00\",\"comments\":[],\"pages\":[]}";
     private static final String EXPECTED_TITLE = "Sample Title";
     private static final String EXPECTED_QUESTIONNAIRE_ID = "456";
     private static final String EXPECTED_INITIATOR = "user1";
     private static final String EXPECTED_REVIEWER = "user2";
     private static final String EXPECTED_APPROVER = "user3";
     private static final String EXPECTED_COMMENTED_BY = "user1";
     private static final String EXPECTED_COMMENT_TEXT = "Good job";
     private static final String EXPECTED_PAGE_ID = "1";
     private static final String EXPECTED_PAGE_TITLE = "Page title";
 
     @BeforeEach
     public void setUp() {
         RestAssured.port = PORT;
     }
 
     @Test
     public void testNotify() {
 
         ContentType contentType = ContentType.JSON;
 
         Headers headers = new Headers(new Header("Custom-Header", "header-value"));
         Response response = given()
                 .contentType(contentType)
                 .headers(headers)
                 .body(SAMPLE_JSON)
                 .when()
                 .post(API_PATH);
 
         assertEquals(STATUS_CODE, response.getStatusCode());
 
         assertEquals(EXPECTED_TITLE, response.jsonPath().getString("title"));
         assertEquals(EXPECTED_QUESTIONNAIRE_ID, response.jsonPath().getString("questionnaireId"));
         assertEquals(EXPECTED_INITIATOR, response.jsonPath().getString("initiator"));
         assertEquals(EXPECTED_REVIEWER, response.jsonPath().getString("reviewer"));
         assertEquals(EXPECTED_APPROVER, response.jsonPath().getString("approver"));
         assertNotNull(response.jsonPath().getString("createdAt"));
         assertNotNull(response.jsonPath().getString("modifiedAt"));
 
         List<CommentOutputDTO> comments = response.jsonPath().getList("comments", CommentOutputDTO.class);
         assertNotNull(comments);
 
         if (!comments.isEmpty()) {
             assertEquals(1, comments.size());
             assertEquals(EXPECTED_COMMENTED_BY, comments.get(0).getCommentedBy());
             assertEquals(EXPECTED_COMMENT_TEXT, comments.get(0).getCommentText());
         }
 
         List<Page> pages = response.jsonPath().getList("pages", Page.class);
         assertNotNull(pages);
 
         if (!pages.isEmpty()) {
             assertEquals(1, pages.size());
             assertEquals(EXPECTED_PAGE_ID, pages.get(0).getId());
             assertEquals(EXPECTED_PAGE_TITLE, pages.get(0).getTitle());
         }
     }
 }