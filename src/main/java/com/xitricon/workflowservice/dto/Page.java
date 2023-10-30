package com.xitricon.workflowservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Page {
	public final int index;
	public final String id;
	public final String title;
	public final List<Question> questions;

	@JsonCreator
	public Page(@JsonProperty("index") int index, @JsonProperty("id") String id, @JsonProperty("title") String title,
			@JsonProperty("questions") List<Question> questions) {
		super();
		this.index = index;
		this.id = id;
		this.title = title;
		this.questions = questions;
	}

}
