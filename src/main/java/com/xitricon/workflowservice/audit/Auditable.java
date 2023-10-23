package com.xitricon.workflowservice.audit;

import java.time.LocalDateTime;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class Auditable<U> {

	@CreatedBy
	protected U createdBy;

	@CreatedDate
	protected LocalDateTime createdAt;

	@LastModifiedBy
	protected U modifiedBy;

	@LastModifiedDate
	protected LocalDateTime modifiedAt;
}
