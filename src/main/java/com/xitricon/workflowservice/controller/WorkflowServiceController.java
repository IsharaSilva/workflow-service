package com.xitricon.workflowservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xitricon.workflowservice.dto.BasicWorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowOutputDTO;
import com.xitricon.workflowservice.dto.WorkflowSubmissionInputDTO;
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
	public ResponseEntity<WorkflowOutputDTO> intiateWorkflow(@RequestParam String tenantId) {
		return ResponseEntity.ok(workflowService.initiateWorkflow(tenantId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<WorkflowOutputDTO> getWorkflow(@PathVariable String id, @RequestParam String tenantId) {
		return ResponseEntity.ok(workflowService.getWorkflowById(id, tenantId));
	}

	@PostMapping("/submission")
	public ResponseEntity<WorkflowOutputDTO> workflowSubmission(
			@RequestBody WorkflowSubmissionInputDTO workflowSubmissionInput, @RequestParam boolean completed,
			@RequestParam String tenantId) {
		return ResponseEntity
				.ok(workflowService.handleWorkflowSubmission(completed, workflowSubmissionInput, tenantId));
	}

	@GetMapping
	public ResponseEntity<List<BasicWorkflowOutputDTO>> getWorkflows(@RequestParam String tenantId) {
		return ResponseEntity.ok(workflowService.getWorkflows(tenantId));
	}

	@PutMapping("/active/{workflowId}")
	public ResponseEntity<Void> changeActiveWorkflow(@PathVariable String workflowId,
			@RequestParam String tenantId) {
		this.workflowService.changeActiveWorkflow(workflowId, tenantId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteWorkflow(@PathVariable String id, @RequestParam String tenantId) {
		this.workflowService.deleteWorkflowById(id, tenantId);
		return ResponseEntity.noContent().build();
	}

}