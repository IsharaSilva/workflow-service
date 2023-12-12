package com.xitricon.workflowservice.util;

import java.util.ArrayList;
import java.util.List;

import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.model.Comment;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.Question;

public class WorkflowSubmissionConverter {

	private WorkflowSubmissionConverter() {
		throw new IllegalStateException("Utility class");
	}

	public static List<Page> convertWorkflowSubmissionInputDTOtoPages(WorkflowSubmissionInputDTO input,
			boolean completed) {
		return new ArrayList<>(input.getPages().stream().map(p -> {
			List<Question> questions = new ArrayList<>(p.getQuestions().stream().map(q -> Question.builder()
					.id(q.getId()).index(q.getIndex()).response(q.getResponse()).label(q.getLabel()).build()).toList());

			return Page.builder().id(p.getId()).index(p.getIndex()).title(p.getTitle()).questions(questions)
					.completed(completed).build();
		}).toList());
	}

	public static List<Comment> convertWorkflowSubmissionInputDTOtoComments(WorkflowSubmissionInputDTO input) {
		return new ArrayList<>(input.getComments().stream()
				.map(c -> new Comment(c.getRefId(), c.getCommentedAt(), c.getCommentedBy(), c.getCommentText()))
				.toList());
	}
}
