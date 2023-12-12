package com.xitricon.workflowservice.util;

import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.Question;
import com.xitricon.workflowservice.model.WorkflowSubmission;

public class SupplierQuestionnaireDeterminator {

	private SupplierQuestionnaireDeterminator() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean determineSupplierQuestionnaire(WorkflowSubmission workflowSubmission) {

		// THIS IS REPLACED BY CONCEPTUAL QUESTIONNAIRE AND WILL REMOVE
		Page filteredPagesBySupplier = workflowSubmission.getPages().stream()
				.filter(page -> page.getTitle().equals("Supplier Involvement")).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		Question question = filteredPagesBySupplier.getQuestions().stream()
				.filter(q -> q.getLabel().equals("Supplier Involvement")).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

		return question.getResponse().get(0).equals("supplier");

	}

}