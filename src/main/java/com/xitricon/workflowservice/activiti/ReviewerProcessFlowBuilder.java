package com.xitricon.workflowservice.activiti;

import com.xitricon.workflowservice.activiti.listeners.ReviewingProcessFlowStartListener;
import com.xitricon.workflowservice.util.CommonConstant;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import java.util.List;

public class ReviewerProcessFlowBuilder {

	private ReviewerProcessFlowBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static SubProcess build() {
		SubProcess subProcess = new SubProcess();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID);
		subProcess.setName("Supplier Onboarding Sub Process Two");

		StartEvent reviewerStartEvent = new StartEvent();
		reviewerStartEvent.setId("start-2");

		UserTask reviewerSupplierDetailsTask = new UserTask();
		reviewerSupplierDetailsTask.setName("Reviewer Supplier Details");
		reviewerSupplierDetailsTask.setId("reviewer-supplier-details");
		reviewerSupplierDetailsTask.setAssignee("kermit");

		List<ActivitiListener> executionListeners = reviewerSupplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ReviewingProcessFlowStartListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask reviewerSupportingEvidenceTask = new UserTask();
		reviewerSupportingEvidenceTask.setName("Reviewer Supporting Evidence");
		reviewerSupportingEvidenceTask.setId("reviewer-supporting-evidence");
		reviewerSupportingEvidenceTask.setAssignee("kermit");

		UserTask reviewerSupplierClassificationTask = new UserTask();
		reviewerSupplierClassificationTask.setName("Reviewer Supplier Classification");
		reviewerSupplierClassificationTask.setId("reviewer-supplier-classification");
		reviewerSupplierClassificationTask.setAssignee("kermit");

		UserTask reviewerSupplierInvolvementTask = new UserTask();
		reviewerSupplierInvolvementTask.setName("Reviewer Supplier Involvement");
		reviewerSupplierInvolvementTask.setId("reviewer-supplier-involvement");
		reviewerSupplierInvolvementTask.setAssignee("kermit");

		UserTask reviewerCommentTask = new UserTask();
		reviewerCommentTask.setName("Reviewer Comments");
		reviewerCommentTask.setId("reviewer-comments");
		reviewerCommentTask.setAssignee("kermit");

		EndEvent reviewerEndEvent = new EndEvent();
		reviewerEndEvent.setId("end-2");

		subProcess.addFlowElement(reviewerStartEvent);
		subProcess.addFlowElement(reviewerSupplierDetailsTask);
		subProcess.addFlowElement(reviewerSupportingEvidenceTask);
		subProcess.addFlowElement(reviewerSupplierClassificationTask);
		subProcess.addFlowElement(reviewerSupplierInvolvementTask);
		subProcess.addFlowElement(reviewerCommentTask);
		subProcess.addFlowElement(reviewerEndEvent);

		subProcess.addFlowElement(new SequenceFlow("start-2", "reviewer-supplier-details"));
		subProcess.addFlowElement(new SequenceFlow("reviewer-supplier-details", "reviewer-supporting-evidence"));
		subProcess.addFlowElement(new SequenceFlow("reviewer-supporting-evidence", "reviewer-supplier-classification"));
		subProcess.addFlowElement(new SequenceFlow("reviewer-supplier-classification", "reviewer-supplier-involvement"));
		subProcess.addFlowElement(new SequenceFlow("reviewer-supplier-involvement", "reviewer-comments"));
		subProcess.addFlowElement(new SequenceFlow("reviewer-comments", "end-2"));

		return subProcess;
	}
}
