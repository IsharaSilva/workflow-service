package com.xitricon.workflowservice.util;

public enum ActivitiTypes {
    FORMFILLING("form-filling");

    private String typeString;

    private ActivitiTypes(String typeString) {
        this.typeString = typeString;
    }
}
