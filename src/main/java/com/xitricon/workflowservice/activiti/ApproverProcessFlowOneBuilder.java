package com.xitricon.workflowservice.activiti;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowOneStartListener;
import com.xitricon.workflowservice.util.CommonConstant;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import java.util.List;

public class ApproverProcessFlowOneBuilder {

	private ApproverProcessFlowOneBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static SubProcess build() {
		SubProcess subProcess = new SubProcess();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID);
		subProcess.setName("Supplier Onboarding Sub Process Three");

		StartEvent approverStartEvent = new StartEvent();
		approverStartEvent.setId("start-3");

		UserTask approverSupplierDetailsTask = new UserTask();
		approverSupplierDetailsTask.setName("Approver Supplier Details");
		approverSupplierDetailsTask.setId("approver-supplier-details");
		approverSupplierDetailsTask.setAssignee("kermit");

		List<ActivitiListener> executionListeners = approverSupplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingProcessFlowOneStartListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approverSupportingEvidenceTask = new UserTask();
		approverSupportingEvidenceTask.setName("Approver Supporting Evidence");
		approverSupportingEvidenceTask.setId("approver-supporting-evidence");
		approverSupportingEvidenceTask.setAssignee("kermit");

		UserTask approverSupplierClassificationTask = new UserTask();
		approverSupplierClassificationTask.setName("Approver Supplier Classification");
		approverSupplierClassificationTask.setId("approver-supplier-classification");
		approverSupplierClassificationTask.setAssignee("kermit");

		UserTask approverSupplierInvolvementTask = new UserTask();
		approverSupplierInvolvementTask.setName("Approver Supplier Involvement");
		approverSupplierInvolvementTask.setId("approver-supplier-involvement");
		approverSupplierInvolvementTask.setAssignee("kermit");

		UserTask approverCommentTask = new UserTask();
		approverCommentTask.setName("Approver Comments");
		approverCommentTask.setId("approver-comments");
		approverCommentTask.setAssignee("kermit");

		EndEvent approverEndEvent = new EndEvent();
		approverEndEvent.setId("end-3");

		subProcess.addFlowElement(approverStartEvent);
		subProcess.addFlowElement(approverSupplierDetailsTask);
		subProcess.addFlowElement(approverSupportingEvidenceTask);
		subProcess.addFlowElement(approverSupplierClassificationTask);
		subProcess.addFlowElement(approverSupplierInvolvementTask);
		subProcess.addFlowElement(approverCommentTask);
		subProcess.addFlowElement(approverEndEvent);

		subProcess.addFlowElement(new SequenceFlow("start-3", "approver-supplier-details"));
		subProcess.addFlowElement(new SequenceFlow("approver-supplier-details", "approver-supporting-evidence"));
		subProcess.addFlowElement(new SequenceFlow("approver-supporting-evidence", "approver-supplier-classification"));
		subProcess
				.addFlowElement(new SequenceFlow("approver-supplier-classification", "approver-supplier-involvement"));
		subProcess.addFlowElement(new SequenceFlow("approver-supplier-involvement", "approver-comments"));
		subProcess.addFlowElement(new SequenceFlow("approver-comments", "end-3"));

		return subProcess;
	}
}
