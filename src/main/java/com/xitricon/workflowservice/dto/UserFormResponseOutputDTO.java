package com.xitricon.workflowservice.dto;

import com.xitricon.workflowservice.util.ActivitiTypes;

import lombok.Getter;

@Getter
public class UserFormResponseOutputDTO {
    private final String id;
    private final ActivitiTypes activitiType;
    private final QuestionnaireOutputDTO questionnaire;

    public UserFormResponseOutputDTO(String id, ActivitiTypes activitiType, QuestionnaireOutputDTO questionnaire){
        super();
        this.id = id;
        this.activitiType = activitiType;
        this.questionnaire = questionnaire;
    }
}
