package com.xitricon.workflowservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;

@Service
public interface WorkflowService {
	public WorkflowOutputDTO initiateWorkflow(String tenantId);

	public WorkflowOutputDTO handleWorkflowSubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput, String tenantId);

	public List<BasicWorkflowOutputDTO> getWorkflows(String tenantId);

	public void changeActiveWorkflow(String workfowId, String tenantId);

	public WorkflowOutputDTO getWorkflowById(String id, String tenantId);

}
