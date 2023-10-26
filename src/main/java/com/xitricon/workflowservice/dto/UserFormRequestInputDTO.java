package com.xitricon.workflowservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


@Getter
public class UserFormRequestInputDTO {
    private final String questionnaireId;

    @JsonCreator
    public UserFormRequestInputDTO(@JsonProperty("questionnaireId") String questionnaireId) {
        super();
        this.questionnaireId = questionnaireId;
    }
}
