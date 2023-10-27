package com.xitricon.workflowservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xitricon.workflowservice.dto.TaskOutputDTO;
import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;
import com.xitricon.workflowservice.service.WorkflowService;

@RestController
@RequestMapping("/api/workflow")
@Validated
public class WorkflowServiceController {

	private final WorkflowService workflowService;

	public WorkflowServiceController(final WorkflowService workflowService) {
		super();
		this.workflowService = workflowService;
	}

	@GetMapping("/init")
	public ResponseEntity<UserFormResponseOutputDTO> getRequestQuestionnaire() {
		return ResponseEntity.ok(workflowService.getRequestQuestionnaire());
	}

	@PostMapping("/submission")
	public ResponseEntity<UserFormResponseOutputDTO> postRequestQuestionnaireUpdate(
			@RequestBody(required = false) UserFormRequestInputDTO inputDto, @RequestParam(required = true) boolean isComplete) {
		return ResponseEntity.ok(workflowService.handleQuestionnaireSubmission(inputDto));
	}

	@GetMapping("/list")
	public ResponseEntity<TaskOutputDTO> getWorkflowInstancesList() {
		return ResponseEntity.ok(workflowService.getListOfWorkflows());
	}

}