package com.xitricon.workflowservice.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;

import lombok.extern.slf4j.Slf4j;

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
}
