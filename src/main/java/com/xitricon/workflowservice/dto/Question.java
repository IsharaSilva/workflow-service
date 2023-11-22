package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Question {
	private final String id;
	private final int index;
	private final String label;
	private final String type;
	private final String group;
	private final List<Validation> validations;
	private final boolean editable;
	private final List<String> response;
	private final Object optionsSource;
	private final List<Question> subQuestions;
	private final String tenantId;

	@JsonCreator
	public Question(@JsonProperty("id") String id, @JsonProperty("index") int index,
			@JsonProperty("label") String label, @JsonProperty("type") String type, @JsonProperty("group") String group,
			@JsonProperty("validations") List<Validation> validations, @JsonProperty("editable") boolean editable,
			@JsonProperty("response") List<String> response, @JsonProperty("optionsSource") Object optionsSource,
			@JsonProperty("subQuestions") List<Question> subQuestions, @JsonProperty("tenantId") String tenantId) {
		super();
		this.id = id;
		this.index = index;
		this.label = label;
		this.type = type;
		this.group = group;
		this.validations = validations;
		this.editable = editable;
		this.response = response;
		this.optionsSource = optionsSource;
		this.subQuestions = subQuestions;
		this.tenantId = tenantId;
	}

}
