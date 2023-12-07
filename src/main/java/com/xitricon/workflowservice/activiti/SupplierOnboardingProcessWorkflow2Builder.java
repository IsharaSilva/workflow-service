package com.xitricon.workflowservice.activiti;

import java.util.List;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskOneEndListener;
import com.xitricon.workflowservice.activiti.listeners.ReviewerProcessFlowEndListener;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;

import com.xitricon.workflowservice.activiti.listeners.RequestorProcessFlowEndListener;
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

		SubProcess subProcess_1 = ReviewerProcessFlowBuilder.build();
		subProcess_1.setId("sub-process-1");

		List<ActivitiListener> executionListeners_1 = subProcess_1.getExecutionListeners();
		ActivitiListener activitiListener_1 = new ActivitiListener();

		activitiListener_1.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener_1.setImplementation(ReviewerProcessFlowEndListener.class.getCanonicalName());
		activitiListener_1.setEvent("end");
		executionListeners_1.add(activitiListener_1);

		SubProcess subProcess_2 = ApproverProcessFlowOneBuilder.build();
		subProcess_2.setId("sub-process-2");

		List<ActivitiListener> executionListeners_2 = subProcess_2.getExecutionListeners();
		ActivitiListener activitiListener_2 = new ActivitiListener();

		activitiListener_2.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener_2.setImplementation(ApprovingTaskOneEndListener.class.getCanonicalName());
		activitiListener_2.setEvent("end");
		executionListeners_2.add(activitiListener_2);

		SubProcess subProcess_3 = ApproverProcessFlowTwoBuilder.build();
		subProcess_3.setId("sub-process-3");

		List<ActivitiListener> executionListeners_3 = subProcess_3.getExecutionListeners();
		ActivitiListener activitiListener_3 = new ActivitiListener();

		activitiListener_3.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener_3.setImplementation(ApprovingProcessFlowEndListener.class.getCanonicalName());
		activitiListener_3.setEvent("end");
		executionListeners_3.add(activitiListener_3);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(subProcess);
		process.addFlowElement(subProcess_1);
		process.addFlowElement(subProcess_2);
		process.addFlowElement(subProcess_3);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", "sub-process"));
		process.addFlowElement(new SequenceFlow("sub-process", "sub-process-1"));
		process.addFlowElement(new SequenceFlow("sub-process-1", "sub-process-2"));
		process.addFlowElement(new SequenceFlow("sub-process-2", "sub-process-3"));
		process.addFlowElement(new SequenceFlow("sub-process-3", "end"));

		model.addProcess(process);

		return model;
	}
}
