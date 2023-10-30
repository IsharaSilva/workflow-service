package com.xitricon.workflowservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Validation {
	public final boolean required;

	@JsonCreator
	public Validation(@JsonProperty("required") boolean required) {
		super();
		this.required = required;
	}

}
