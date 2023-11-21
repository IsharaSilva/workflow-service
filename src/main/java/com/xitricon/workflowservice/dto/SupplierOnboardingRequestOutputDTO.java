package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SupplierOnboardingRequestOutputDTO {

	@JsonProperty("workflowId")
	private final String id;
	private final String title;
	private final String questionnaireId;
	private final List<CommentOutputDTO> comments;
	private final List<Page> pages;
	private final String initiator;
	private final String reviewer;
	private final String approver;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime createdAt;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime modifiedAt;

	public SupplierOnboardingRequestOutputDTO() {
		this.id = null;
		this.title = null;
		this.questionnaireId = null;
		this.comments = null;
		this.pages = null;
		this.initiator = null;
		this.reviewer = null;
		this.approver = null;
		this.createdAt = null;
		this.modifiedAt = null;
	}
}

