package com.xitricon.workflowservice.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class Question {
    private int index;
    private String id;
    private List<String> response;

    public Question(String id, int index, List<String> response) {
        this.id = id;
        this.index = index;
        this.response = response;
    }
}
