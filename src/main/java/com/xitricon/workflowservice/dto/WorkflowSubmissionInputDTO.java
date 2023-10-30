package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WorkflowSubmissionInputDTO {
	private final String workflowId;
	private final List<WorkflowSubmissionPageInputDTO> pages;
	private final List<CommentInputDTO> comments;

	@JsonCreator
	public WorkflowSubmissionInputDTO(@JsonProperty("workflowId") String workflowId,
			@JsonProperty("pages") List<WorkflowSubmissionPageInputDTO> pages,
			@JsonProperty("comments") List<CommentInputDTO> comments) {
		super();
		this.workflowId = workflowId;
		this.pages = pages;
		this.comments = comments;
	}
}
