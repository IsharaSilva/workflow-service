package com.xitricon.workflowservice.activiti;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.stereotype.Component;

@Component
public class BPMDeployer {

	public void deploy(ProcessEngine processEngine, BpmnModel model, String processId) {
		DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment().name(processId);
		deploymentBuilder = deploymentBuilder.addBpmnModel(processId + ".bpmn", model).key(processId);
		deploymentBuilder.deploy();
	}
}
