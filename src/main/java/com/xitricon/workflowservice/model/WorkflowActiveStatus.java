package com.xitricon.workflowservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "workflowactivestatus")
@Audited
@Getter
@NoArgsConstructor
@SuperBuilder
public class WorkflowActiveStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String processDefinitionKey;
	private String tenantId;
	private boolean active;
}
