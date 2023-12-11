package com.xitricon.workflowservice.util;


import org.activiti.engine.delegate.DelegateExecution;


import com.xitricon.workflowservice.dto.Question;
import com.xitricon.workflowservice.model.Page;
import com.xitricon.workflowservice.model.WorkflowSubmission;

import java.util.List;

public class SupplierQuestionnaireDeterminator {
    
    public static boolean determineSupplierQuestionnaire(WorkflowSubmission workflowSubmission, DelegateExecution execution) {


        //THIS IS REPLACED BY CONCEPTUAL QUESTIONNAIRE AND WILL REMOVE
        Page filteredPagesBySupplier = workflowSubmission.getPages().stream()//todo
                .filter(page -> page.getTitle().equals("Supplier Involvement"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

        Question question = filteredPagesBySupplier.getQuestions().stream() //todo
              .filter(q -> q.getLable().equals("Supplier Involvement")).findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Invalid workflow Input"));

        
        return question.getResponse().get(0).equals("supplier");
 
    

    }

}