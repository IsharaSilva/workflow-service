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

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		SubProcess subProcess = RequestorProcessFlowBuilder.build();
		subProcess.setId("sub-process");

		List<ActivitiListener> executionListeners = subProcess.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(RequestorProcessFlowEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask reviewingTask = new UserTask();
		reviewingTask.setName("Form reviewing");
		reviewingTask.setId("form-review");
		reviewingTask.setAssignee("kermit");

		executionListeners = reviewingTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ReviewingTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approvalTaskFirst = new UserTask();
		approvalTaskFirst.setName("Approval task one");
		approvalTaskFirst.setId("approval1");
		approvalTaskFirst.setAssignee("kermit");

		executionListeners = approvalTaskFirst.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingTaskOneEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		UserTask approvalTaskSecond = new UserTask();
		approvalTaskSecond.setName("Approval task second");
		approvalTaskSecond.setId("approval2");
		approvalTaskSecond.setAssignee("kermit");

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

		process.addFlowElement(new SequenceFlow("start", "sub-process"));
		process.addFlowElement(new SequenceFlow("sub-process", "form-review"));
		process.addFlowElement(new SequenceFlow("form-review", "approval1"));
		process.addFlowElement(new SequenceFlow("approval1", "approval2"));
		process.addFlowElement(new SequenceFlow("approval2", "end"));

		model.addProcess(process);

		return model;
	}
}
