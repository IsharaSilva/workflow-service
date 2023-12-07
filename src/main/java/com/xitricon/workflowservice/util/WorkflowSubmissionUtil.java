package com.xitricon.workflowservice.util;

import com.xitricon.workflowservice.model.Page;
import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.model.WorkflowSubmission;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorkflowSubmissionUtil {

	private final ObjectMapper objectMapper;

	public WorkflowSubmissionUtil(@Qualifier("dateTimeAwareObjectMapper") final ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public String convertToString(WorkflowSubmissionInputDTO workflowSubmissionInput) {
		try {
			return objectMapper.writeValueAsString(workflowSubmissionInput);
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public String convertToString(WorkflowSubmission workflowSubmission) {
		try {
			return objectMapper.writeValueAsString(workflowSubmission);
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public WorkflowSubmission convertToWorkflowSubmission(String input) {
		try {
			return objectMapper.readValue(input, WorkflowSubmission.class);
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public WorkflowSubmissionInputDTO convertToWorkflowSubmissionInputDTO(String workflowSubmissionInput) {
		try {
			return objectMapper.readValue(workflowSubmissionInput, WorkflowSubmissionInputDTO.class);
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public  void setCompletedFalseWhenPartialSubmission(DelegateExecution execution){
		Object interimStateObj = execution.getVariable("interimState");
		if (interimStateObj instanceof String) {
			try {
				WorkflowSubmission workflowSubmission = convertToWorkflowSubmission((String)interimStateObj);
				List<Page> pages = workflowSubmission.getPages().stream()
						.map(page -> {
							return new Page(page.getIndex(), page.getId(), page.getQuestions(), false);
						}).collect(Collectors.toList());
				String updatedInterimState = convertToString(new WorkflowSubmission(workflowSubmission.getWorkflowId(),pages,workflowSubmission.getComments()));
				execution.setVariable("interimState", updatedInterimState);
			} catch (Exception e) {
				log.error("Error processing interimState data: {}", e.getMessage(), e);
			}
		} else {
			log.error("interimState is not a representation of WorkflowSubmission");
		}

	}

}
