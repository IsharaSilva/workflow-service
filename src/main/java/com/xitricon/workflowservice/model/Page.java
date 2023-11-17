package com.xitricon.workflowservice.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class Page {
    private int index;
	private String id;
	private List<Question> questions;
    private boolean completed;

    public Page(int index, String id, List<Question> questions, boolean completed) {
        this.index = index;
        this.id = id;
        this.questions = questions;
        this.completed = completed;
    }
}
