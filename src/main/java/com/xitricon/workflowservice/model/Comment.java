package com.xitricon.workflowservice.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Comment {
    private String commentedBy;
	private LocalDateTime commentedAt;
	private String comment;
}
