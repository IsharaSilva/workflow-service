package com.xitricon.workflowservice.service;

import com.xitricon.workflowservice.model.WorkflowActiveStatus;

public interface WorkflowActiveStatusService {
	WorkflowActiveStatus findByActiveTrueAndTenantId(String tenantId);

	WorkflowActiveStatus findByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId);

	void updateWorkflowActiveStatus(long id, boolean active);
}
