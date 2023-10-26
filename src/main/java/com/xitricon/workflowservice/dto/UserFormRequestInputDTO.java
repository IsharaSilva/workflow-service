package com.xitricon.workflowservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;


@Getter
public class UserFormRequestInputDTO {
    private final String questionnaireId;

    @JsonCreator
    public UserFormRequestInputDTO(@JsonProperty("questionnaireId") String questionnaireId) {
        super();
        this.questionnaireId = questionnaireId;
    }
}
