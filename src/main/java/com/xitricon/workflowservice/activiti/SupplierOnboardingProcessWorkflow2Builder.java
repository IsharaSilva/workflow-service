package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskOneEndListener;
import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskTwoEndListener;
import com.xitricon.workflowservice.activiti.listeners.RequestorProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.ReviewingTaskEndListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class SupplierOnboardingProcessWorkflow2Builder {

	private SupplierOnboardingProcessWorkflow2Builder() {
		throw new IllegalStateException("Utility class");
	}

	public static BpmnModel build() {
		BpmnModel model = new BpmnModel();
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_TWO_ID);
		process.setName("Supplier Onboarding Process Two");

		final String LISTENER_KERMIT = "kermit";

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		SubProcess subProcess = RequestorProcessFlowBuilder.build();
		subProcess.setId(CommonConstant.SUB_PROCESS_ID);

		List<ActivitiListener> executionListeners = subProcess.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(RequestorProcessFlowEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask reviewingTask = new UserTask();
		reviewingTask.setName("Form reviewing");
		reviewingTask.setId(CommonConstant.FORM_REVIEW_TASK_ID);
		reviewingTask.setAssignee(LISTENER_KERMIT);

		executionListeners = reviewingTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ReviewingTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approvalTaskFirst = new UserTask();
		approvalTaskFirst.setName("Approval task one");
		approvalTaskFirst.setId(CommonConstant.DUAL_APPROVAL_ONE_TASK_ID);
		approvalTaskFirst.setAssignee(LISTENER_KERMIT);

		executionListeners = approvalTaskFirst.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingTaskOneEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approvalTaskSecond = new UserTask();
		approvalTaskSecond.setName("Approval task second");
		approvalTaskSecond.setId(CommonConstant.DUAL_APPROVAL_TWO_TASK_ID);
		approvalTaskSecond.setAssignee(LISTENER_KERMIT);

		executionListeners = approvalTaskSecond.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingTaskTwoEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(subProcess);
		process.addFlowElement(reviewingTask);
		process.addFlowElement(approvalTaskFirst);
		process.addFlowElement(approvalTaskSecond);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", CommonConstant.SUB_PROCESS_ID));
		process.addFlowElement(new SequenceFlow(CommonConstant.SUB_PROCESS_ID, CommonConstant.FORM_REVIEW_TASK_ID));
		process.addFlowElement(new SequenceFlow(CommonConstant.FORM_REVIEW_TASK_ID, CommonConstant.DUAL_APPROVAL_ONE_TASK_ID));
		process.addFlowElement(new SequenceFlow(CommonConstant.DUAL_APPROVAL_ONE_TASK_ID, CommonConstant.DUAL_APPROVAL_TWO_TASK_ID));
		process.addFlowElement(new SequenceFlow(CommonConstant.DUAL_APPROVAL_TWO_TASK_ID, "end"));

		model.addProcess(process);

		return model;
	}
}
