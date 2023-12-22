package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.RequestorProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.ReviewerProcessFlowEndListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class SupplierOnboardingProcessWorkflow1Builder {

	private static final String APPROVER_RESUBMISSION_EXCLUSIVE_GW_ID = "approverResubmissionExclusiveGw";
	private static final String REVIEWER_RESUBMISSION_EXCLUSIVE_GW_ID = "reviewerResubmissionExclusiveGw";

	private SupplierOnboardingProcessWorkflow1Builder() {
		throw new IllegalStateException("Utility class");
	}

	public static BpmnModel build() {
		BpmnModel model = new BpmnModel();
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ONE_ID);
		process.setName("Supplier Onboarding Process One");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		SubProcess subProcess = RequestorProcessFlowBuilder.build();
		subProcess.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID);

		List<ActivitiListener> executionListeners = subProcess.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(RequestorProcessFlowEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		SubProcess subProcess1 = ReviewerProcessFlowBuilder.build();
		subProcess1.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID);

		List<ActivitiListener> executionListeners1 = subProcess1.getExecutionListeners();
		ActivitiListener activitiListener1 = new ActivitiListener();

		activitiListener1.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener1.setImplementation(ReviewerProcessFlowEndListener.class.getCanonicalName());
		activitiListener1.setEvent("end");
		executionListeners1.add(activitiListener1);

		SubProcess subProcess2 = ApproverProcessFlowOneBuilder.build();
		subProcess2.setId(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID);

		List<ActivitiListener> executionListeners2 = subProcess2.getExecutionListeners();
		ActivitiListener activitiListener2 = new ActivitiListener();

		activitiListener2.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener2.setImplementation(ApprovingProcessFlowEndListener.class.getCanonicalName());
		activitiListener2.setEvent("end");
		executionListeners2.add(activitiListener2);

		ExclusiveGateway reviewerResubmissionExclusiveGw = new ExclusiveGateway();
		reviewerResubmissionExclusiveGw.setName("Reviewer Resubmission Exclusive Gateway");
		reviewerResubmissionExclusiveGw.setId(REVIEWER_RESUBMISSION_EXCLUSIVE_GW_ID);

		ExclusiveGateway approverResubmissionExclusiveGw = new ExclusiveGateway();
		approverResubmissionExclusiveGw.setName("Approver Resubmission Exclusive Gateway");
		approverResubmissionExclusiveGw.setId(APPROVER_RESUBMISSION_EXCLUSIVE_GW_ID);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(reviewerResubmissionExclusiveGw);
		process.addFlowElement(approverResubmissionExclusiveGw);
		process.addFlowElement(subProcess);
		process.addFlowElement(subProcess1);
		process.addFlowElement(subProcess2);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID));
		process.addFlowElement(new SequenceFlow(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID,
				CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID));

		process.addFlowElement(new SequenceFlow(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID,
				REVIEWER_RESUBMISSION_EXCLUSIVE_GW_ID));

		SequenceFlow seqReviewerSubmission = new SequenceFlow(REVIEWER_RESUBMISSION_EXCLUSIVE_GW_ID,
				CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID);
		seqReviewerSubmission.setConditionExpression("${resubmission == 'false'}");
		reviewerResubmissionExclusiveGw.setDefaultFlow(seqReviewerSubmission.getId());

		SequenceFlow seqReviewerReSubmission = new SequenceFlow(REVIEWER_RESUBMISSION_EXCLUSIVE_GW_ID,
				CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_ONE_ID);
		seqReviewerReSubmission.setConditionExpression("${resubmission == 'true'}");
		process.addFlowElement(seqReviewerSubmission);
		process.addFlowElement(seqReviewerReSubmission);

		process.addFlowElement(new SequenceFlow(CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_THREE_ID,
				APPROVER_RESUBMISSION_EXCLUSIVE_GW_ID));

		SequenceFlow seqApproverSubmission = new SequenceFlow(APPROVER_RESUBMISSION_EXCLUSIVE_GW_ID, "end");
		seqApproverSubmission.setConditionExpression("${resubmission == 'false'}");
		approverResubmissionExclusiveGw.setDefaultFlow(seqApproverSubmission.getId());

		SequenceFlow seqApproverReSubmission = new SequenceFlow(APPROVER_RESUBMISSION_EXCLUSIVE_GW_ID,
				CommonConstant.SUPPLIER_ONBOARDING_SUB_PROCESS_TWO_ID);
		seqApproverReSubmission.setConditionExpression("${resubmission == 'true'}");
		process.addFlowElement(seqApproverSubmission);
		process.addFlowElement(seqApproverReSubmission);

		model.addProcess(process);

		return model;
	}
}
