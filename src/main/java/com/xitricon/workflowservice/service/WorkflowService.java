package com.xitricon.workflowservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;

@Service
public interface WorkflowService {
	public WorkflowOutputDTO initiateWorkflow();

	public WorkflowOutputDTO handleQuestionnaireSubmission(UserFormRequestInputDTO inputDto);

	public List<BasicWorkflowOutputDTO> getWorkflows();
}
