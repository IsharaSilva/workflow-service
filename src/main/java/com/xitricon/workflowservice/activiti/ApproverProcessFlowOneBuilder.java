package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowOneStartListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class ApproverProcessFlowOneBuilder {

	private static final String APPROVER_COMMENTS_ID = "approver-comments";
	private static final String APPROVER_SUPPLIER_INVOLVEMENT_ID = "approver-supplier-involvement";
	private static final String DEFAULT_ASSIGNEE = "kermit";
	private static final String APPROVER_SUPPLIER_DETAILS_ID = "approver-supplier-details";
	private static final String APPROVER_SUPPLIER_CLASSIFICATION_ID = "approver-supplier-classification";
	private static final String APPROVER_SUPPORTING_EVIDENCE_ID = "approver-supporting-evidence";

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
		approverSupplierDetailsTask.setId(APPROVER_SUPPLIER_DETAILS_ID);
		approverSupplierDetailsTask.setAssignee(DEFAULT_ASSIGNEE);

		List<ActivitiListener> executionListeners = approverSupplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingProcessFlowOneStartListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approverSupportingEvidenceTask = new UserTask();
		approverSupportingEvidenceTask.setName("Approver Supporting Evidence");
		approverSupportingEvidenceTask.setId(APPROVER_SUPPORTING_EVIDENCE_ID);
		approverSupportingEvidenceTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask approverSupplierClassificationTask = new UserTask();
		approverSupplierClassificationTask.setName("Approver Supplier Classification");
		approverSupplierClassificationTask.setId(APPROVER_SUPPLIER_CLASSIFICATION_ID);
		approverSupplierClassificationTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask approverSupplierInvolvementTask = new UserTask();
		approverSupplierInvolvementTask.setName("Approver Supplier Involvement");
		approverSupplierInvolvementTask.setId(APPROVER_SUPPLIER_INVOLVEMENT_ID);
		approverSupplierInvolvementTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask approverCommentTask = new UserTask();
		approverCommentTask.setName("Approver Comments");
		approverCommentTask.setId(APPROVER_COMMENTS_ID);
		approverCommentTask.setAssignee(DEFAULT_ASSIGNEE);

		EndEvent approverEndEvent = new EndEvent();
		approverEndEvent.setId("end-3");

		subProcess.addFlowElement(approverStartEvent);
		subProcess.addFlowElement(approverSupplierDetailsTask);
		subProcess.addFlowElement(approverSupportingEvidenceTask);
		subProcess.addFlowElement(approverSupplierClassificationTask);
		subProcess.addFlowElement(approverSupplierInvolvementTask);
		subProcess.addFlowElement(approverCommentTask);
		subProcess.addFlowElement(approverEndEvent);

		subProcess.addFlowElement(new SequenceFlow("start-3", APPROVER_SUPPLIER_DETAILS_ID));
		subProcess.addFlowElement(new SequenceFlow(APPROVER_SUPPLIER_DETAILS_ID, APPROVER_SUPPORTING_EVIDENCE_ID));
		subProcess
				.addFlowElement(new SequenceFlow(APPROVER_SUPPORTING_EVIDENCE_ID, APPROVER_SUPPLIER_CLASSIFICATION_ID));
		subProcess.addFlowElement(
				new SequenceFlow(APPROVER_SUPPLIER_CLASSIFICATION_ID, APPROVER_SUPPLIER_INVOLVEMENT_ID));
		subProcess.addFlowElement(new SequenceFlow(APPROVER_SUPPLIER_INVOLVEMENT_ID, APPROVER_COMMENTS_ID));
		subProcess.addFlowElement(new SequenceFlow(APPROVER_COMMENTS_ID, "end-3"));

		return subProcess;
	}
}
