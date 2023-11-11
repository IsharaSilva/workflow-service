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

    public Page(int index, String id, List<Question> questions) {
        this.index = index;
        this.id = id;
        this.questions = questions;
    }
}
