package com.xitricon.workflowservice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xitricon.workflowservice.model.enums.WorkFlowStatus;
import com.xitricon.workflowservice.util.CommonConstant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BasicWorkflowOutputDTO {

	private final String id;
	private final String title;
	private final String workflowType;
	private final WorkFlowStatus status;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime createdAt;

	private final String createdBy;

	@JsonFormat(pattern = CommonConstant.DATE_TIME_FORMAT)
	private final LocalDateTime modifiedAt;

	private final String modifiedBy;
}
