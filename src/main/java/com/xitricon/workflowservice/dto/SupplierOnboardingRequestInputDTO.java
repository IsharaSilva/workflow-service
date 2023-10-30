package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class SupplierOnboardingRequestInputDTO {

	private final String title;
	private final String questionnaireId;
	private final List<CommentInputDTO> comments;
	private final List<Page> pages;
	private final String initiator;
	private final String reviewer;
	private final String approver;

	@JsonCreator
	public SupplierOnboardingRequestInputDTO(@JsonProperty("title") String title,
			@JsonProperty("workflowId") String questionnaireId,
			@JsonProperty("comments") List<CommentInputDTO> comments, @JsonProperty("pages") List<Page> pages,
			@JsonProperty("initiator") String initiator, @JsonProperty("reviewer") String reviewer,
			@JsonProperty("approver") String approver) {
		super();
		this.title = title;
		this.questionnaireId = questionnaireId;
		this.comments = comments;
		this.pages = pages;
		this.initiator = initiator;
		this.reviewer = reviewer;
		this.approver = approver;
	}

}
