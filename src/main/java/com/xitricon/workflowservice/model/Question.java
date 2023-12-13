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
	private String label;
}
