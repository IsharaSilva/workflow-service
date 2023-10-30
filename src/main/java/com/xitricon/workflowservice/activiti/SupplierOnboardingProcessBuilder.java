package com.xitricon.workflowservice.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.activiti.listeners.ApprovingTaskEndListener;
import com.xitricon.workflowservice.activiti.listeners.FormFillingTaskEndListener;
import com.xitricon.workflowservice.activiti.listeners.ReviewingTaskEndListener;
import com.xitricon.workflowservice.util.CommonConstant;

public class SupplierOnboardingProcessBuilder {

	public static BpmnModel build() {
		BpmnModel model = new BpmnModel();
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		process.setName("Supplier Onboarding");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		UserTask formFillingTask = new UserTask();
		formFillingTask.setName("Requestor form filling");
		formFillingTask.setId("req-form-fill");
		formFillingTask.setAssignee("kermit");
		formFillingTask.setFormKey("supplier-form");

		List<ActivitiListener> executionListeners = formFillingTask.getExecutionListeners();
		ActivitiListener activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(FormFillingTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		ExtensionElement ext = new ExtensionElement();
		FormProperty prop = new FormProperty();
		prop.setName("supplierName");
		prop.setType("string");
		FormValue formValue = new FormValue();
		ArrayList<FormValue> values = new ArrayList<FormValue>();
		values.add(formValue);
		prop.setFormValues(values);

		ext.addAttribute(new ExtensionAttribute("sample ABC"));
		formFillingTask.addExtensionElement(ext);

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

		UserTask approvalTask = new UserTask();
		approvalTask.setName("Approval task");
		approvalTask.setId("approval");
		approvalTask.setAssignee("kermit");

		executionListeners = approvalTask.getExecutionListeners();
		activitiListener = new ActivitiListener();

		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
		activitiListener.setImplementation(ApprovingTaskEndListener.class.getCanonicalName());
		activitiListener.setEvent("end");
		executionListeners.add(activitiListener);

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(formFillingTask);
		process.addFlowElement(reviewingTask);
		process.addFlowElement(approvalTask);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", "req-form-fill"));
		process.addFlowElement(new SequenceFlow("req-form-fill", "form-review"));
		process.addFlowElement(new SequenceFlow("form-review", "approval"));
		process.addFlowElement(new SequenceFlow("approval", "end"));

		model.addProcess(process);

		return model;
	}

}
