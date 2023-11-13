package com.xitricon.workflowservice.model;

import java.util.List;
import java.util.ArrayList;

import com.xitricon.workflowservice.dto.CommentInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionPageInputDTO;

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

	public WorkflowSubmission(WorkflowSubmissionInputDTO workflowSubmissionInput) {
		this.workflowId = workflowSubmissionInput.getWorkflowId();
		pages = new ArrayList<>(workflowSubmissionInput.getPages().stream().map(p -> {
			List<Question> questions = new ArrayList<>(p.getQuestions().stream()
					.map(q -> new Question(q.getId(), q.getIndex(), q.getResponse())).toList());

			return new Page(p.getIndex(), p.getId(), questions);
		}).toList());

		comments = new ArrayList<>(workflowSubmissionInput.getComments().stream().map(c -> {
			return new Comment(c.getRefId(), c.getCommentedAt(), c.getCommentedBy(), c.getCommentText());
		}).toList());

	}

	public void addPages(List<WorkflowSubmissionPageInputDTO> pagesList) {
		this.pages.addAll(pagesList.stream().map(p -> {
			List<Question> questions = new ArrayList<>(p.getQuestions().stream()
					.map(q -> new Question(q.getId(), q.getIndex(), q.getResponse())).toList());

			return new Page(p.getIndex(), p.getId(), questions);
		}).toList());
	}

	public void addComments(List<CommentInputDTO> comments) {
		this.comments.addAll(comments.stream().map(c -> {
			return new Comment(c.getRefId(), c.getCommentedAt(), c.getCommentedBy(), c.getCommentText());
		}).toList());
	}
}