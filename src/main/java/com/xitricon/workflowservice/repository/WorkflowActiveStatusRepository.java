package com.xitricon.workflowservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.xitricon.workflowservice.model.WorkflowActiveStatus;

public interface WorkflowActiveStatusRepository extends JpaRepository<WorkflowActiveStatus, Long> {

	Optional<WorkflowActiveStatus> findByActiveTrueAndTenantId(String tenantId);

	Optional<WorkflowActiveStatus> findByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId);

	@Modifying
	@Query(value = "UPDATE WorkflowActiveStatus w SET w.active = ?2 WHERE w.id = ?1")
	void updateWorkflowActiveStatus(long id, boolean active);
}
