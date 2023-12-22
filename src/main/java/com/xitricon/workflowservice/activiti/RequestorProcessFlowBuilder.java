package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.activiti.listeners.SupplierClassificationListener;
import com.xitricon.workflowservice.activiti.listeners.SupplierCommentListener;
import com.xitricon.workflowservice.activiti.listeners.SupplierDetailsTaskEndListener;
import com.xitricon.workflowservice.activiti.listeners.SupplierInvolvementListener;
import com.xitricon.workflowservice.activiti.listeners.SupportingEvidenceListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class RequestorProcessFlowBuilder {

	private static final String END_EVENT_ID = "end-1";
	private static final String SUPPLIER_COMMENT_EXCLUSIVE_GW_ID = "supplierCommentExclusiveGw";
	private static final String SUPPLIER_COMMENT_ID = "supplier-Comment";
	private static final String SUPPLIER_INVOLVEMENT_ID = "supplier-involvement";
	private static final String SUPPLIER_CLASSIFICATION_ID = "supplier-classification";
	private static final String SUPPORTING_EVIDENCE_ID = "supporting-evidence";
	private static final String DEFAULT_ASSIGNEE = "kermit";
	private static final String SUPPLIER_DETAILS_ID = "supplier-details";

	private RequestorProcessFlowBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static SubProcess build() {
		SubProcess subProcess = new org.activiti.bpmn.model.SubProcess();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID);
		subProcess.setName("Supplier Onboarding Sub Process One");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start-1");

		UserTask supplierDetailsTask = new UserTask();
		supplierDetailsTask.setName("Supplier Details");
		supplierDetailsTask.setId(SUPPLIER_DETAILS_ID);
		supplierDetailsTask.setAssignee(DEFAULT_ASSIGNEE);

		List<ActivitiListener> executionListeners = supplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierDetailsTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supportingEvidenceTask = new UserTask();
		supportingEvidenceTask.setName("Supporting Evidence");
		supportingEvidenceTask.setId(SUPPORTING_EVIDENCE_ID);
		supportingEvidenceTask.setAssignee(DEFAULT_ASSIGNEE);

		executionListeners = supportingEvidenceTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupportingEvidenceListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supplierClassificationTask = new UserTask();
		supplierClassificationTask.setName("Supplier Classification");
		supplierClassificationTask.setId(SUPPLIER_CLASSIFICATION_ID);
		supplierClassificationTask.setAssignee(DEFAULT_ASSIGNEE);

		executionListeners = supplierClassificationTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierClassificationListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supplierInvolvementTask = new UserTask();
		supplierInvolvementTask.setName("Supplier Involvement");
		supplierInvolvementTask.setId(SUPPLIER_INVOLVEMENT_ID);
		supplierInvolvementTask.setAssignee(DEFAULT_ASSIGNEE);

		executionListeners = supplierInvolvementTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierInvolvementListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask supplierCommentTask = new UserTask();
		supplierCommentTask.setName("Supplier Comment");
		supplierCommentTask.setId(SUPPLIER_COMMENT_ID);
		supplierCommentTask.setAssignee(DEFAULT_ASSIGNEE);

		executionListeners = supplierCommentTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(SupplierCommentListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		ExclusiveGateway supplierCommentExclusiveGw = new ExclusiveGateway();
		supplierCommentExclusiveGw.setName("Supplier Resubmission Comment Exclusive Gateway");
		supplierCommentExclusiveGw.setId(SUPPLIER_COMMENT_EXCLUSIVE_GW_ID);

		EndEvent endEvent = new EndEvent();
		endEvent.setId(END_EVENT_ID);

		subProcess.addFlowElement(startEvent);
		subProcess.addFlowElement(supplierCommentExclusiveGw);
		subProcess.addFlowElement(supplierDetailsTask);
		subProcess.addFlowElement(supportingEvidenceTask);
		subProcess.addFlowElement(supplierClassificationTask);
		subProcess.addFlowElement(supplierInvolvementTask);
		subProcess.addFlowElement(supplierCommentTask);
		subProcess.addFlowElement(endEvent);

		subProcess.addFlowElement(new SequenceFlow("start-1", SUPPLIER_DETAILS_ID));
		subProcess.addFlowElement(new SequenceFlow(SUPPLIER_DETAILS_ID, SUPPORTING_EVIDENCE_ID));
		subProcess.addFlowElement(new SequenceFlow(SUPPORTING_EVIDENCE_ID, SUPPLIER_CLASSIFICATION_ID));
		subProcess.addFlowElement(new SequenceFlow(SUPPLIER_CLASSIFICATION_ID, SUPPLIER_INVOLVEMENT_ID));
		subProcess.addFlowElement(new SequenceFlow(SUPPLIER_INVOLVEMENT_ID, SUPPLIER_COMMENT_EXCLUSIVE_GW_ID));

		SequenceFlow seqSupplierCommentSubmission = new SequenceFlow(SUPPLIER_COMMENT_EXCLUSIVE_GW_ID, END_EVENT_ID);
		seqSupplierCommentSubmission.setConditionExpression("${status == 'SUBMISSION_IN_PROGRESS'}");
		supplierCommentExclusiveGw.setDefaultFlow(seqSupplierCommentSubmission.getId());

		SequenceFlow seqSupplierCommentResubmission = new SequenceFlow(SUPPLIER_COMMENT_EXCLUSIVE_GW_ID,
				SUPPLIER_COMMENT_ID);
		seqSupplierCommentResubmission.setConditionExpression("${status == 'CORRECTION_INPROGRESS'}");
		subProcess.addFlowElement(seqSupplierCommentSubmission);
		subProcess.addFlowElement(seqSupplierCommentResubmission);

		subProcess.addFlowElement(new SequenceFlow(SUPPLIER_COMMENT_ID, END_EVENT_ID));
		return subProcess;
	}
}
