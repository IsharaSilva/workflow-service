package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class CommentOutputDTO extends CommentInputDTO {
	private final String id;

	public CommentOutputDTO(String id, String commentedBy, LocalDateTime commentedAt, String comment, String refId) {
		super(refId, commentedBy, commentedAt, comment);
		this.id = id;
	}

	public CommentOutputDTO() {
		super(null, null, null, null); 
		this.id = null;
	}

}
