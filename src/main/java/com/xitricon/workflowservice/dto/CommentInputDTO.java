package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.Getter;

@Getter
public class CommentInputDTO {
	private final String refId;
	private final String commentedBy;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime commentedAt;

	private final String commentText;

	@JsonCreator
	public CommentInputDTO(@JsonProperty("refId") String refId, @JsonProperty("commentedBy") String commentedBy,
			@JsonProperty("commentedAt") LocalDateTime commentedAt, @JsonProperty("commentText") String commentText) {
		super();
		this.refId = refId;
		this.commentedBy = commentedBy;
		this.commentedAt = commentedAt;
		this.commentText = commentText;
	}

}
