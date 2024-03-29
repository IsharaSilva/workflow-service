package com.xitricon.workflowservice.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		Map<String, Page> map = new HashMap<>();
		pages.forEach(item -> map.put(item.getId(), item));
		for (Page item : pagesList) {
			if (map.containsKey(item.getId())) {
				int pageIdx = pages.indexOf(map.get(item.getId()));
				if (pageIdx > -1) {
					pages.set(pageIdx, Page.builder().id(item.getId()).index(item.getIndex()).title(item.getTitle())
							.questions(item.getQuestions()).completed(item.isCompleted()).build());
				}
			} else {
				pages.add(item);
			}
		}
	}

	public void addComments(List<Comment> comments) {
		this.comments.addAll(comments);
	}
}