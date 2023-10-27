package com.xitricon.workflowservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.service.WorkflowService;

@RestController
@RequestMapping("/api/workflows")
@Validated
public class WorkflowServiceController {

	private final WorkflowService workflowService;

	public WorkflowServiceController(final WorkflowService workflowService) {
		super();
		this.workflowService = workflowService;
	}

	@GetMapping("/init")
	public ResponseEntity<WorkflowOutputDTO> intiateWorkflow() {
		return ResponseEntity.ok(workflowService.initiateWorkflow());
	}

	@PostMapping("/submission")
	public ResponseEntity<WorkflowOutputDTO> postRequestQuestionnaireUpdate(
			@RequestBody(required = false) UserFormRequestInputDTO inputDto,
			@RequestParam(required = true) boolean isComplete) {
		return ResponseEntity.ok(workflowService.handleQuestionnaireSubmission(inputDto));
	}

	@GetMapping
	public ResponseEntity<List<BasicWorkflowOutputDTO>> getWorkflows() {
		return ResponseEntity.ok(workflowService.getWorkflows());
	}

}