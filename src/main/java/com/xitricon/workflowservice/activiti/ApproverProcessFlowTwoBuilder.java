package com.xitricon.workflowservice.activiti;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowTwoStartListener;
import com.xitricon.workflowservice.util.CommonConstant;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import java.util.List;

public class ApproverProcessFlowTwoBuilder {

	private ApproverProcessFlowTwoBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static SubProcess build() {
		SubProcess subProcess = new SubProcess();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID);
		subProcess.setName("Supplier Onboarding Sub Process Three");

		StartEvent approverStartEvent = new StartEvent();
		approverStartEvent.setId("start-4");

		UserTask approverSupplierDetailsTask = new UserTask();
		approverSupplierDetailsTask.setName("Approver_2 Supplier Details");
		approverSupplierDetailsTask.setId("approver_2-supplier-details");
		approverSupplierDetailsTask.setAssignee("kermit");

		List<ActivitiListener> executionListeners = approverSupplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingProcessFlowTwoStartListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approverSupportingEvidenceTask = new UserTask();
		approverSupportingEvidenceTask.setName("Approver_2 Supporting Evidence");
		approverSupportingEvidenceTask.setId("approver_2-supporting-evidence");
		approverSupportingEvidenceTask.setAssignee("kermit");


		UserTask approverSupplierClassificationTask = new UserTask();
		approverSupplierClassificationTask.setName("Approver_2 Supplier Classification");
		approverSupplierClassificationTask.setId("approver_2-supplier-classification");
		approverSupplierClassificationTask.setAssignee("kermit");


		UserTask approverSupplierInvolvementTask = new UserTask();
		approverSupplierInvolvementTask.setName("Approver_2 Supplier Involvement");
		approverSupplierInvolvementTask.setId("approver_2-supplier-involvement");
		approverSupplierInvolvementTask.setAssignee("kermit");


		UserTask approverCommentTask = new UserTask();
		approverCommentTask.setName("Approver_2 Comments");
		approverCommentTask.setId("approver_2-comments");
		approverCommentTask.setAssignee("kermit");

		EndEvent approverEndEvent = new EndEvent();
		approverEndEvent.setId("end-4");

		subProcess.addFlowElement(approverStartEvent);
		subProcess.addFlowElement(approverSupplierDetailsTask);
		subProcess.addFlowElement(approverSupportingEvidenceTask);
		subProcess.addFlowElement(approverSupplierClassificationTask);
		subProcess.addFlowElement(approverSupplierInvolvementTask);
		subProcess.addFlowElement(approverCommentTask);
		subProcess.addFlowElement(approverEndEvent);

		subProcess.addFlowElement(new SequenceFlow("start-4", "approver_2-supplier-details"));
		subProcess.addFlowElement(new SequenceFlow("approver_2-supplier-details", "approver_2-supporting-evidence"));
		subProcess.addFlowElement(new SequenceFlow("approver_2-supporting-evidence", "approver_2-supplier-classification"));
		subProcess.addFlowElement(new SequenceFlow("approver_2-supplier-classification", "approver_2-supplier-involvement"));
		subProcess.addFlowElement(new SequenceFlow("approver_2-supplier-involvement", "approver_2-comments"));
		subProcess.addFlowElement(new SequenceFlow("approver_2-comments", "end-4"));

		return subProcess;
	}
}
