package com.xitricon.workflowservice.util;

import java.util.List;
import java.util.ArrayList;

import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
import com.xitricon.workflowservice.model.Comment;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.Question;

public class WorkflowSubmissionConverter {
	public static List<Page> convertWorkflowSubmissionInputDTOtoPages(WorkflowSubmissionInputDTO input,
			boolean completed) {
		return new ArrayList<>(input.getPages().stream().map(p -> {
			List<Question> questions = new ArrayList<>(p.getQuestions().stream()
					.map(q -> new Question(q.getId(), q.getIndex(), q.getResponse())).toList());

			return new Page(p.getIndex(), p.getId(), questions, completed);
		}).toList());
	}

	public static List<Comment> convertWorkflowSubmissionInputDTOtoComments(WorkflowSubmissionInputDTO input) {
		return new ArrayList<>(input.getComments().stream().map(c -> {
			return new Comment(c.getRefId(), c.getCommentedAt(), c.getCommentedBy(), c.getCommentText());
		}).toList());
	}
}
