package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.SubProcess;

import com.xitricon.workflowservice.activiti.listeners.SupplierClassificationListener;
import com.xitricon.workflowservice.activiti.listeners.SupplierDetailsTaskEndListener;
import com.xitricon.workflowservice.activiti.listeners.SupplierInvolvementListener;
import com.xitricon.workflowservice.activiti.listeners.SupportingEvidenceListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class RequestorProcessFlowBuilder {

	private RequestorProcessFlowBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static SubProcess build() {
		SubProcess subProcess = new org.activiti.bpmn.model.SubProcess();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID);
		subProcess.setName("Supplier Onboarding Sub Process One");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		UserTask supplierDetailsTask = new UserTask();
		supplierDetailsTask.setName("Supplier Details");
		supplierDetailsTask.setId("supplier-details");
		supplierDetailsTask.setAssignee("kermit");

		List<ActivitiListener> executionListeners = supplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierDetailsTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supportingEvidenceTask = new UserTask();
		supportingEvidenceTask.setName("Supporting Evidence");
		supportingEvidenceTask.setId("supporting-evidence");
		supportingEvidenceTask.setAssignee("kermit");

		executionListeners = supportingEvidenceTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupportingEvidenceListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supplierClassificationTask = new UserTask();
		supplierClassificationTask.setName("Supplier Classification");
		supplierClassificationTask.setId("supplier-classification");
		supplierClassificationTask.setAssignee("kermit");

		executionListeners = supplierClassificationTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierClassificationListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supplierInvolvementTask = new UserTask();
		supplierInvolvementTask.setName("Supplier Involvement");
		supplierInvolvementTask.setId("supplier-involvement");
		supplierInvolvementTask.setAssignee("kermit");

		executionListeners = supplierInvolvementTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierInvolvementListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		subProcess.addFlowElement(startEvent);
		subProcess.addFlowElement(supplierDetailsTask);
		subProcess.addFlowElement(supportingEvidenceTask);
		subProcess.addFlowElement(supplierClassificationTask);
		subProcess.addFlowElement(supplierInvolvementTask);
		subProcess.addFlowElement(endEvent);

		subProcess.addFlowElement(new SequenceFlow("start", "supplier-details"));
		subProcess.addFlowElement(new SequenceFlow("supplier-details", "supporting-evidence"));
		subProcess.addFlowElement(new SequenceFlow("supporting-evidence", "supplier-classification"));
		subProcess.addFlowElement(new SequenceFlow("supplier-classification", "supplier-involvement"));
		subProcess.addFlowElement(new SequenceFlow("supplier-involvement", "end"));

		return subProcess;
	}
}
