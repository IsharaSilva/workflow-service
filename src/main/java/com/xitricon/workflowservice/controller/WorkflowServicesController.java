package com.xitricon.workflowservice.controller;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xitricon.workflowservice.dto.UserFormRequestInputDTO;
import com.xitricon.workflowservice.dto.UserFormResponseOutputDTO;
import com.xitricon.workflowservice.service.impl.WorkflowServiceImpl;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/workflow")
@Validated
public class WorkflowServicesController {
    private final String processEngineName = "supplierOnboarding";

    @Autowired
    private WorkflowServiceImpl supplierService;

    @Autowired
    Environment env;

    @PostConstruct
    private void init() {
        ProcessEngineConfiguration processEngineConfiguration 
        = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        ProcessEngine processEngine = processEngineConfiguration.setProcessEngineName(processEngineName).setDatabaseSchemaUpdate(ProcessEngineConfiguration
        .DB_SCHEMA_UPDATE_TRUE)
        .setJdbcUrl(env.getProperty("spring.activiti.datasource.url"))
        .setJdbcDriver(env.getProperty("spring.activiti.datasource.driver-class-name"))
        .setJdbcUsername(env.getProperty("spring.activiti.datasource.username"))
        .setJdbcPassword(env.getProperty("spring.activiti.datasource.password"))
          .buildProcessEngine();
    }

    @GetMapping("/init")
    public ResponseEntity<UserFormResponseOutputDTO> getRequestQuestionnaire() {
        return ResponseEntity.ok(supplierService.getRequestQuestionnaire());
    }

    @PostMapping("/submission")
    public ResponseEntity<UserFormResponseOutputDTO> postRequestQuestionnaireUpdate(@RequestBody(required = false) UserFormRequestInputDTO inputDto) {
        return ResponseEntity.ok(supplierService.updateRequestQuestionnaire(inputDto));
    }

}