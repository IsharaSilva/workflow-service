package com.xitricon.workflowservice.dto;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Question {
	public final String id;
	public final int index;
	public final String label;
	public final String type;
	public final String group;
	public final List<Validation> validations;
	public final boolean editable;
	public final List<String> response;
	private final Object optionsSource;
	private final List<Question> subQuestions;
}
