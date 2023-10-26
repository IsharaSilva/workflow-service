package com.xitricon.workflowservice.activiti;

import java.util.ArrayList;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

import com.xitricon.workflowservice.util.CommonConstant;

public class SupplierOnboardingProcessBuilder {

	public static BpmnModel build() {
		BpmnModel model = new BpmnModel();
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId(CommonConstant.SUPPLIER_ONBOARDING_PROCESS_ID);
		process.setName("Supplier Onboarding");

		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");

		UserTask userTaskSupplierDetails = new UserTask();
		userTaskSupplierDetails.setName("Supplier Details");
		userTaskSupplierDetails.setId("sid-supplier-details");
		userTaskSupplierDetails.setAssignee("kermit");
		userTaskSupplierDetails.setFormKey("supplier-form");

		ExtensionElement ext = new ExtensionElement();
		FormProperty prop = new FormProperty();
		prop.setName("supplierName");
		prop.setType("string");
		FormValue formValue = new FormValue();
		ArrayList<FormValue> values = new ArrayList<FormValue>();
		values.add(formValue);
		prop.setFormValues(values);

		ext.addAttribute(new ExtensionAttribute("sample ABC"));
		userTaskSupplierDetails.addExtensionElement(ext);

		UserTask userTaskSupportingEvidence = new UserTask();
		userTaskSupportingEvidence.setName("Supporting Evidence");
		userTaskSupportingEvidence.setId("sid-supporting-evidence");
		userTaskSupportingEvidence.setAssignee("kermit");

		UserTask userTaskSupplierClassification = new UserTask();
		userTaskSupplierClassification.setName("Supplier Classification");
		userTaskSupplierClassification.setId("sid-supplier-classification");
		userTaskSupplierClassification.setAssignee("kermit");

		UserTask userTaskSupplierInvolvement = new UserTask();
		userTaskSupplierInvolvement.setName("Supplier Involvement");
		userTaskSupplierInvolvement.setId("sid-supplier-involvement");
		userTaskSupplierInvolvement.setAssignee("kermit");

		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");

		process.addFlowElement(startEvent);
		process.addFlowElement(userTaskSupplierDetails);
		process.addFlowElement(userTaskSupportingEvidence);
		process.addFlowElement(userTaskSupplierClassification);
		process.addFlowElement(userTaskSupplierInvolvement);
		process.addFlowElement(endEvent);

		process.addFlowElement(new SequenceFlow("start", "sid-supplier-details"));
		process.addFlowElement(new SequenceFlow("sid-supplier-details", "sid-supporting-evidence"));
		process.addFlowElement(new SequenceFlow("sid-supporting-evidence", "sid-supplier-classification"));
		process.addFlowElement(new SequenceFlow("sid-supplier-classification", "sid-supplier-involvement"));
		process.addFlowElement(new SequenceFlow("sid-supplier-involvement", "end"));

		model.addProcess(process);

		return model;
	}

}
