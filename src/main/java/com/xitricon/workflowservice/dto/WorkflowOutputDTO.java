package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xitricon.workflowservice.model.enums.ActivitiType;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.Getter;

@Getter
public class WorkflowOutputDTO {

	private final String id;
	private final ActivitiType activitiType;
	private final String title;
	private final QuestionnaireOutputDTO questionnaire;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime createdAt;

	private final String createdBy;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime modifiedAt;

	private final String modifiedBy;

	private final String tenantId;

	@JsonCreator
	public WorkflowOutputDTO(@JsonProperty("id") String id, @JsonProperty("activitiType") ActivitiType activitiType,
			@JsonProperty("title") String title, @JsonProperty("questionnaire") QuestionnaireOutputDTO questionnaire,
			@JsonProperty("createdAt") LocalDateTime createdAt, @JsonProperty("createdBy") String createdBy,
			@JsonProperty("modifiedAt") LocalDateTime modifiedAt, @JsonProperty("modifiedBy") String modifiedBy,
			@JsonProperty("tenantId") String tenantId) {
		super();
		this.id = id;
		this.activitiType = activitiType;
		this.title = title;
		this.questionnaire = questionnaire;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
		this.tenantId = tenantId;
	}

}
