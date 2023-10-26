package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class CommentOutputDTO extends CommentInputDTO {
	private final String id;

	public CommentOutputDTO(String id, String commentedBy, LocalDateTime commentedAt, String comment) {
		super(commentedBy, commentedAt, comment);
		this.id = id;
	}

}
