package com.xitricon.workflowservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowSubmissionUtil {
	private WorkflowSubmissionUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String convertToString(WorkflowSubmissionInputDTO workflowSubmissionInput) {
		try {
			return new ObjectMapper().writeValueAsString(workflowSubmissionInput);
		} catch (JsonProcessingException e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}
}
