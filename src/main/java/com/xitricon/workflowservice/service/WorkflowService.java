package com.xitricon.workflowservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;

@Service
public interface WorkflowService {
	public WorkflowOutputDTO initiateWorkflow();

	public WorkflowOutputDTO handleWorkflowSubmission(boolean completed,
			WorkflowSubmissionInputDTO workflowSubmissionInput);

	public List<BasicWorkflowOutputDTO> getWorkflows();

	public WorkflowOutputDTO getWorkflowById(String id);

	public void changeActiveWorkflow(String workfowId);
}
