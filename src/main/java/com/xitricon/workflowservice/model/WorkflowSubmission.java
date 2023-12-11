package com.xitricon.workflowservice.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		pagesList.forEach(item -> {
			if (map.containsKey(item.getId())) {
				int pageIdx = pages.indexOf(map.get(item.getId()));
				if (pageIdx > -1) {
					Page pageToBeReplaced = Page.builder().id(item.getId()).index(item.getIndex())
							.questions(item.getQuestions()).completed(item.isCompleted()).build();
					pages.set(pageIdx, pageToBeReplaced);
				}
			} else {
				pages.add(item);
			}
		});
	}

	public void addComments(List<Comment> comments) {
		this.comments.addAll(comments);
	}
}