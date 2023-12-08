package com.xitricon.workflowservice.service.impl;

import org.springframework.stereotype.Service;

import com.xitricon.workflowservice.common.exeption.ResourceNotFoundException;
import com.xitricon.workflowservice.model.WorkflowActiveStatus;
import com.xitricon.workflowservice.repository.WorkflowActiveStatusRepository;
import com.xitricon.workflowservice.service.WorkflowActiveStatusService;

import jakarta.transaction.Transactional;

@Service
public class WorkflowActiveStatusServiceImpl implements WorkflowActiveStatusService {

	private WorkflowActiveStatusRepository workflowActiveStatusRepository;

	public WorkflowActiveStatusServiceImpl(final WorkflowActiveStatusRepository workflowActiveStatusRepository) {
		this.workflowActiveStatusRepository = workflowActiveStatusRepository;
	}

	@Override
	public WorkflowActiveStatus findByActiveTrueAndTenantId(String tenantId) {
		return this.workflowActiveStatusRepository.findByActiveTrueAndTenantId(tenantId).orElseThrow(
				() -> new ResourceNotFoundException("An active process is not found for the given tenant"));
	}

	@Override
	@Transactional
	public void updateWorkflowActiveStatus(long id, boolean active) {
		this.workflowActiveStatusRepository.updateWorkflowActiveStatus(id, active);

	}

	@Override
	public WorkflowActiveStatus findByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String tenantId) {
		return this.workflowActiveStatusRepository.findByProcessDefinitionKeyAndTenantId(processDefinitionKey, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"A process is not found for the given tenant and process definition key"));

	}

}
