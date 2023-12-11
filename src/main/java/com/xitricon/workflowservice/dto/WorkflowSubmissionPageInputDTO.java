package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WorkflowSubmissionPageInputDTO {
	private final int index;
	private final String id;
	private final String title;
	private final List<WorkflowSubmissionQuestionInputDTO> questions;
	private final boolean completed;

	@JsonCreator
	public WorkflowSubmissionPageInputDTO(@JsonProperty("index") int index, @JsonProperty("id") String id, @JsonProperty("title") String title,
			@JsonProperty("questions") List<WorkflowSubmissionQuestionInputDTO> questions, @JsonProperty("completed") boolean completed) {
		super();
		this.index = index;
		this.id = id;
		this.title = title;
		this.questions = questions;
		this.completed = completed;
	}

}
