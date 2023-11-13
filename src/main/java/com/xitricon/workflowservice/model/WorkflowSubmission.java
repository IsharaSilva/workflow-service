package com.xitricon.workflowservice.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class WorkflowSubmission {
	private String workflowId;
	private List<Page> pages;
	private List<Comment> comments;

	public WorkflowSubmission(String workflowId, List<Page> pages, List<Comment> comments) {
		this.workflowId = workflowId;
		this.pages = pages;
		this.comments = comments;
	}

	public void addPages(List<Page> pagesList) {
		this.pages.addAll(pagesList);
	}

	public void addComments(List<Comment> comments) {
		this.comments.addAll(comments);
	}
}