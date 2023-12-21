package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.activiti.listeners.ReviewingProcessFlowStartListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class ReviewerProcessFlowBuilder {

	private static final String REVIEWER_COMMENTS_ID = "reviewer-comments";
	private static final String REVIEWER_SUPPLIER_INVOLVEMENT_ID = "reviewer-supplier-involvement";
	private static final String REVIEWER_SUPPLIER_CLASSIFICATION_ID = "reviewer-supplier-classification";
	private static final String REVIEWER_SUPPORTING_EVIDENCE_ID = "reviewer-supporting-evidence";
	private static final String DEFAULT_ASSIGNEE = "kermit";
	private static final String REVIEWER_SUPPLIER_DETAILS_ID = "reviewer-supplier-details";

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
		reviewerSupplierDetailsTask.setId(REVIEWER_SUPPLIER_DETAILS_ID);
		reviewerSupplierDetailsTask.setAssignee(DEFAULT_ASSIGNEE);

		List<ActivitiListener> executionListeners = reviewerSupplierDetailsTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ReviewingProcessFlowStartListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask reviewerSupportingEvidenceTask = new UserTask();
		reviewerSupportingEvidenceTask.setName("Reviewer Supporting Evidence");
		reviewerSupportingEvidenceTask.setId(REVIEWER_SUPPORTING_EVIDENCE_ID);
		reviewerSupportingEvidenceTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask reviewerSupplierClassificationTask = new UserTask();
		reviewerSupplierClassificationTask.setName("Reviewer Supplier Classification");
		reviewerSupplierClassificationTask.setId(REVIEWER_SUPPLIER_CLASSIFICATION_ID);
		reviewerSupplierClassificationTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask reviewerSupplierInvolvementTask = new UserTask();
		reviewerSupplierInvolvementTask.setName("Reviewer Supplier Involvement");
		reviewerSupplierInvolvementTask.setId(REVIEWER_SUPPLIER_INVOLVEMENT_ID);
		reviewerSupplierInvolvementTask.setAssignee(DEFAULT_ASSIGNEE);

		UserTask reviewerCommentTask = new UserTask();
		reviewerCommentTask.setName("Reviewer Comments");
		reviewerCommentTask.setId(REVIEWER_COMMENTS_ID);
		reviewerCommentTask.setAssignee(DEFAULT_ASSIGNEE);

		EndEvent reviewerEndEvent = new EndEvent();
		reviewerEndEvent.setId("end-2");

		subProcess.addFlowElement(reviewerStartEvent);
		subProcess.addFlowElement(reviewerSupplierDetailsTask);
		subProcess.addFlowElement(reviewerSupportingEvidenceTask);
		subProcess.addFlowElement(reviewerSupplierClassificationTask);
		subProcess.addFlowElement(reviewerSupplierInvolvementTask);
		subProcess.addFlowElement(reviewerCommentTask);
		subProcess.addFlowElement(reviewerEndEvent);

		subProcess.addFlowElement(new SequenceFlow("start-2", REVIEWER_SUPPLIER_DETAILS_ID));
		subProcess.addFlowElement(new SequenceFlow(REVIEWER_SUPPLIER_DETAILS_ID, REVIEWER_SUPPORTING_EVIDENCE_ID));
		subProcess
				.addFlowElement(new SequenceFlow(REVIEWER_SUPPORTING_EVIDENCE_ID, REVIEWER_SUPPLIER_CLASSIFICATION_ID));
		subProcess.addFlowElement(
				new SequenceFlow(REVIEWER_SUPPLIER_CLASSIFICATION_ID, REVIEWER_SUPPLIER_INVOLVEMENT_ID));
		subProcess.addFlowElement(new SequenceFlow(REVIEWER_SUPPLIER_INVOLVEMENT_ID, REVIEWER_COMMENTS_ID));
		subProcess.addFlowElement(new SequenceFlow(REVIEWER_COMMENTS_ID, "end-2"));

		return subProcess;
	}
}
