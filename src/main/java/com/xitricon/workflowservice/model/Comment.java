package com.xitricon.workflowservice.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class Comment {
    private String refId;
    private String commentedBy;
    private LocalDateTime commentedAt;
    private String commentText;

    public Comment(String refId, LocalDateTime commentedAt, String commentedBy, String commentText) {
        this.refId = refId;
        this.commentedAt = commentedAt;
        this.commentedBy = commentedBy;
        this.commentText = commentText;
    }
}
