package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WorkflowSubmissionQuestionInputDTO {
	public final String id;
	public final int index;
	public final List<String> response;

	@JsonCreator
	public WorkflowSubmissionQuestionInputDTO(@JsonProperty("id") String id, @JsonProperty("index") int index,
			@JsonProperty("response") List<String> response) {
		super();
		this.id = id;
		this.index = index;
		this.response = response;
	}

}
