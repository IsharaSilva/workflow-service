package com.xitricon.workflowservice.activiti;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;

import com.xitricon.workflowservice.activiti.listeners.ApprovingProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskOneEndListener;
import com.xitricon.workflowservice.activiti.listeners.RequestorProcessFlowEndListener;
import com.xitricon.workflowservice.activiti.listeners.ReviewerProcessFlowEndListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class SupplierOnboardingProcessWorkflow2Builder {

    private static final String APPROVING_SUB_PROCESS2_ID = "approving_sub_process2";
    private static final String APPROVING_SUB_PROCESS1_ID = "approving_sub_process1";
    private static final String REVIEWING_SUB_PROCESS_ID = "reviewing_sub_process";
    private static final String REQUESTOR_SUB_PROCESS_ID = "requestor_sub_process";

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
        subProcess.setId(REQUESTOR_SUB_PROCESS_ID);

        List<ActivitiListener> executionListeners = subProcess.getExecutionListeners();
        ActivitiListener activitiListener = new ActivitiListener();

        activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        activitiListener.setImplementation(RequestorProcessFlowEndListener.class.getCanonicalName());
        activitiListener.setEvent("end");
        executionListeners.add(activitiListener);

        SubProcess subProcess1 = ReviewerProcessFlowBuilder.build();
        subProcess1.setId(REVIEWING_SUB_PROCESS_ID);

        List<ActivitiListener> executionListeners1 = subProcess1.getExecutionListeners();
        ActivitiListener activitiListener1 = new ActivitiListener();

        activitiListener1.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        activitiListener1.setImplementation(ReviewerProcessFlowEndListener.class.getCanonicalName());
        activitiListener1.setEvent("end");
        executionListeners1.add(activitiListener1);

        SubProcess subProcess2 = ApproverProcessFlowOneBuilder.build();
        subProcess2.setId(APPROVING_SUB_PROCESS1_ID);

        List<ActivitiListener> executionListeners2 = subProcess2.getExecutionListeners();
        ActivitiListener activitiListener2 = new ActivitiListener();

        activitiListener2.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        activitiListener2.setImplementation(ApprovingTaskOneEndListener.class.getCanonicalName());
        activitiListener2.setEvent("end");
        executionListeners2.add(activitiListener2);

        SubProcess subProcess3 = ApproverProcessFlowTwoBuilder.build();
        subProcess3.setId(APPROVING_SUB_PROCESS2_ID);

        List<ActivitiListener> executionListeners3 = subProcess3.getExecutionListeners();
        ActivitiListener activitiListener3 = new ActivitiListener();

        activitiListener3.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        activitiListener3.setImplementation(ApprovingProcessFlowEndListener.class.getCanonicalName());
        activitiListener3.setEvent("end");
        executionListeners3.add(activitiListener3);

        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");

        process.addFlowElement(startEvent);
        process.addFlowElement(subProcess);
        process.addFlowElement(subProcess1);
        process.addFlowElement(subProcess2);
        process.addFlowElement(subProcess3);
        process.addFlowElement(endEvent);

        process.addFlowElement(new SequenceFlow("start", REQUESTOR_SUB_PROCESS_ID));
        process.addFlowElement(new SequenceFlow(REQUESTOR_SUB_PROCESS_ID, REVIEWING_SUB_PROCESS_ID));
        process.addFlowElement(new SequenceFlow(REVIEWING_SUB_PROCESS_ID, APPROVING_SUB_PROCESS1_ID));
        process.addFlowElement(new SequenceFlow(APPROVING_SUB_PROCESS1_ID, APPROVING_SUB_PROCESS2_ID));
        process.addFlowElement(new SequenceFlow(APPROVING_SUB_PROCESS2_ID, "end"));

        model.addProcess(process);

        return model;
    }
}