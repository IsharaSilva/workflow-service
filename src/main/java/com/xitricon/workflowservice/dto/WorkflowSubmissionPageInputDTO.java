package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WorkflowSubmissionPageInputDTO {
	public final int index;
	public final String id;
	public final List<WorkflowSubmissionQuestionInputDTO> questions;
	public final boolean completed;

	@JsonCreator
	public WorkflowSubmissionPageInputDTO(@JsonProperty("index") int index, @JsonProperty("id") String id,
			@JsonProperty("questions") List<WorkflowSubmissionQuestionInputDTO> questions, @JsonProperty("completed") boolean completed) {
		super();
		this.index = index;
		this.id = id;
		this.questions = questions;
		this.completed = completed;
	}

}
