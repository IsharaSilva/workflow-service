package com.xitricon.workflowservice.util;


import org.activiti.engine.delegate.DelegateExecution;

import com.xitricon.workflowservice.dto.Page;
import com.xitricon.workflowservice.dto.Question;
import com.xitricon.workflowservice.model.WorkflowSubmission;

import java.util.List;

public class SupplierQuestionnaireDeterminator {
    
    public static boolean determineSupplierQuestionnaire(WorkflowSubmission workflowSubmission, DelegateExecution execution) {

        List<Page> pages = workflowSubmission.getPages().stream()
                .map(page -> new Page(page.getIndex(), page.getId(), "Supplier Involvement", null, page.isCompleted()))
                .toList();

        Page filteredPagesBySupplier = pages.stream()
                .filter(page -> page.getTitle().equals("Supplier Involvement"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

        Question question = filteredPagesBySupplier.getQuestions().stream()
                .filter(q -> q.getLabel().equals("Questionnaire to be filled by"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

        
        return question.getResponse().get(0).equals("supplier");

    

    }

}