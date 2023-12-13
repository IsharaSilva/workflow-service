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
    private String title;
	private List<Question> questions;
    private boolean completed;

}
